package com.nhb.common.id.buffer;

import com.nhb.common.id.core.PaddedAtomicLong;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 15:50
 * @description: 基于数组实现的环形缓冲区 (Ring Buffer)<BR/>
 * <p>
 * 作用：高性能地存储和提供 ID<BR/>
 * 优化点：<BR/>
 * 1. 使用数组实现，利用 CPU 缓存行（Cache Line）提高读取性能。<BR/>
 * 2. 使用 PaddedAtomicLong 修饰 'tail' 和 'cursor'，防止“伪共享”（False Sharing）带来的性能损耗。<BR/>
 * <p>
 * 核心组件：<BR/>
 * <li><b>slots:</b> 存储 UID 的槽位数组。<BR/>
 * <li><b>flags:</b> 状态标记数组，与 slots 对应，标识槽位是可生产(CAN_PUT)还是可消费(CAN_TAKE)。<BR/>
 * <li><b>tail:</b> 尾指针，指向最后一个已生产位置（生产者写入位置）。<BR/>
 * <li><b>cursor:</b> 头指针/游标，指向下一个可消费位置（消费者读取位置）。
 */
@Slf4j
@Data
public class RingBuffer {

    // 初始化指针的起始位置
    private static final int START_POINT = -1;
    // 槽位状态：可放入（可生产）
    private static final long CAN_PUT_FLAG = 0L;
    // 槽位状态：可取出（可消费）
    private static final long CAN_TAKE_FLAG = 1L;
    // 默认填充阈值百分比：当剩余可用 ID 少于总量的 50% 时触发异步填充
    public static final int DEFAULT_PADDING_PERCENT = 50;

    /**
     * 环形缓冲区容量
     */
    private final int bufferSize;
    /**
     * 用于通过位运算计算索引的掩码，要求 bufferSize 必须是 2 的幂，即 index = sequence & indexMask
     */
    private final long indexMask;
    /**
     * 存储 UID 的槽位数组
     */
    private final long[] slots;
    /**
     * 状态标记数组，用于控制槽位的读写状态，防止读写冲突
     */
    private final PaddedAtomicLong[] flags;
    /**
     * 尾指针：指向最后一个写入的位置（生产序列）
     * 使用 PaddedAtomicLong 防止伪共享
     */
    private final AtomicLong tail = new PaddedAtomicLong(START_POINT);
    /**
     * 游标：指向下一个要读取的位置（消费序列）
     * 使用 PaddedAtomicLong 防止伪共享
     */
    private final AtomicLong cursor = new PaddedAtomicLong(START_POINT);
    /**
     * 触发缓冲区填充的阈值（剩余 ID 数量低于此值将触发填充）
     */
    private final int paddingThreshold;

    /**
     * 拒绝策略处理器：当缓冲区空时，处理 take 请求
     */
    private RejectedPutBufferHandler rejectedPutHandler = this::discardPutBuffer;
    /**
     * 拒绝策略处理器：当缓冲区空时，处理 take 请求
     */
    private RejectedTakeBufferHandler rejectedTakeHandler = this::exceptionRejectedTakeBuffer;

    /**
     * 缓冲区填充执行器，用于异步加载新的 ID 数据
     */
    private BufferPaddingExecutor bufferPaddingExecutor;

    /**
     * 构造函数（使用默认填充百分比 50%）
     *
     * @param bufferSize 缓冲区大小，必须是正数且为 2 的幂
     */
    public RingBuffer(int bufferSize) {
        this(bufferSize, DEFAULT_PADDING_PERCENT);
    }

    /**
     * 全参构造函数
     *
     * @param bufferSize    缓冲区大小，必须是正数且为 2 的幂
     * @param paddingFactor 填充因子，百分比 (0-100)。<BR/>
     *                      当剩余可用 ID 数量低于 (bufferSize * paddingFactor / 100) 时，触发异步填充。
     */
    public RingBuffer(int bufferSize, int paddingFactor) {
        // 校验：大小必须为正，且必须是 2 的幂（保证位运算取模的正确性）
        Assert.isTrue(bufferSize > 0L, "RingBuffer Size Must Be Positive");
        Assert.isTrue(Integer.bitCount(bufferSize) == 1, "RingBuffer Size Must Be A Power Of 2");
        Assert.isTrue(paddingFactor > 0 && paddingFactor < 100, "RingBuffer Size Must Be Positive");

        this.bufferSize = bufferSize;
        this.indexMask = bufferSize - 1;
        this.slots = new long[bufferSize];
        this.flags = initFlags(bufferSize);

        this.paddingThreshold = bufferSize * paddingFactor / 100;
    }


    /**
     * 向缓冲区放入一个 ID (生产操作) <BR/>
     * 注意：该方法使用 synchronized 关键字保证原子性。<BR/>
     * 原因：虽然我们有预生成的 ID 列表，但在放入缓冲区这一瞬间，需要保证“填充槽位”和“移动指针”是原子的。<BR/>
     *
     * @param id 待放入的 ID
     * @return true 表示放入成功，false 表示缓冲区已满（将执行拒绝策略）
     */
    public synchronized boolean put(long id) {
        long currentTail = tail.get();
        long currentCursor = cursor.get();

        // 1. 检查缓冲区是否已满：tail 追上了 cursor (留出一个空位作为满的判断)
        // 如果距离等于 bufferSize - 1，说明满了
        long distance = currentTail - (currentCursor == START_POINT ? 0 : currentCursor);
        if (distance == bufferSize - 1) {
            rejectedPutHandler.rejectPutBuffer(this, id);
            return false;
        }

        // 2. 检查下一个槽位的状态是否为“可放入”
        int nextTailIndex = calSlotIndex(currentTail + 1);
        if (flags[nextTailIndex].get() != CAN_PUT_FLAG) {
            rejectedPutHandler.rejectPutBuffer(this, id);
            return false;
        }

        // 3. 执行放入操作（原子块内）
        // 3.1 将 ID 放入槽位
        slots[nextTailIndex] = id;
        // 3.2 修改槽位状态为“可取”
        flags[nextTailIndex].set(CAN_TAKE_FLAG);
        // 3.3 移动尾指针 (发布操作)
        tail.incrementAndGet();
        // 原子性保证：上述操作由 synchronized 保证。这意味着，消费者(take)在 tail 更新之前，
        // 是无法消费到我们刚刚放入的这个 UID 的，从而保证了数据安全。
        return true;
    }

    /**
     * 从缓冲区获取一个 ID (消费操作) <BR/>
     * 特点：这是一个无锁（Lock-Free）操作，性能极高。<BR/>
     * 逻辑：通过原子操作移动 cursor，获取下一个可用 ID。
     *
     * @return 获取到的 ID
     * @throws IllegalStateException 如果发生异常（如 cursor 回退）
     */
    public long take() {
        // 1. 获取当前 cursor，并尝试原子更新为 nextCursor
        // 如果 cursor 等于 tail（生产者位置），说明没有数据了，保持原样；否则 +1
        long currentCursor = cursor.get();
        long nextCursor = cursor.updateAndGet(old -> old == tail.get() ? old : old + 1);
        // 安全校验：cursor 不应该回退
        Assert.isTrue(nextCursor >= currentCursor, "Cursor Can't Move Back");
        // 2. 触发异步填充机制
        // 如果当前剩余数量 (tail - nextCursor) 小于阈值，且填充线程未在运行，则触发异步填充
        long currentTail = tail.get();
        if (currentTail - nextCursor < paddingThreshold) {
            log.info("Reach the padding threshold:{}. tail:{}, cursor:{}, rest:{}", paddingThreshold, currentTail,
                    nextCursor, currentTail - nextCursor);
            bufferPaddingExecutor.asyncPadding();
        }

        // 3. 检查是否真的有数据可取
        // 如果 nextCursor 没有变化（即 updateAndGet 时发现 tail == old），说明缓冲区为空
        if (nextCursor == currentCursor) {
            rejectedTakeHandler.rejectTakeBuffer(this);
        }

        // 4. 获取数据
        int nextCursorIndex = calSlotIndex(nextCursor);
        Assert.isTrue(flags[nextCursorIndex].get() == CAN_TAKE_FLAG, "Cursor not in can take status");

        // 4.1 校验状态必须是“可取”
        long uid = slots[nextCursorIndex];
        // 4.3 设置槽位状态为“可放”，以便生产者复用该位置
        // 注意：这里不能交换顺序！必须先取值，再改状态。
        // 如果先改状态为 CAN_PUT，生产者可能立即覆盖该槽位写入新值，
        // 导致消费者在绕环一圈后再次读取时，读到重复的 ID。
        flags[nextCursorIndex].set(CAN_PUT_FLAG);
        return uid;
    }


    /**
     * 计算槽位索引（利用位运算替代取模，提高性能）
     * 条件：bufferSize 必须是 2 的幂
     */
    protected int calSlotIndex(long sequence) {
        return (int) (sequence & indexMask);
    }

    /**
     * 拒绝策略：丢弃放入操作（日志记录）
     */
    protected void discardPutBuffer(RingBuffer ringBuffer, long uid) {
        log.warn("Rejected putting buffer for id:{}. {}", uid, ringBuffer);
    }

    /**
     * 拒绝策略：抛出异常（当取数据失败时）
     */
    protected void exceptionRejectedTakeBuffer(RingBuffer ringBuffer) {
        log.warn("Rejected take buffer. {}", ringBuffer);
        throw new RuntimeException("Rejected take buffer. " + ringBuffer);
    }

    /**
     * 初始化 flags 数组，所有槽位初始状态均为“可放入”
     */
    private PaddedAtomicLong[] initFlags(int bufferSize) {
        PaddedAtomicLong[] flags = new PaddedAtomicLong[bufferSize];
        for (int i = 0; i < bufferSize; i++) {
            flags[i] = new PaddedAtomicLong(CAN_PUT_FLAG);
        }
        return flags;
    }

    public long getTail() {
        return tail.get();
    }

    public long getCursor() {
        return cursor.get();
    }
}
