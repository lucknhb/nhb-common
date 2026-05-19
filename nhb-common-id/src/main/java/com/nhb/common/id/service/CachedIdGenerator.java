package com.nhb.common.id.service;

import com.nhb.common.id.buffer.BufferPaddingExecutor;
import com.nhb.common.id.buffer.RejectedPutBufferHandler;
import com.nhb.common.id.buffer.RejectedTakeBufferHandler;
import com.nhb.common.id.buffer.RingBuffer;
import com.nhb.common.id.core.WorkerIdAssigner;
import com.nhb.common.id.exception.IdGeneratorException;
import com.nhb.common.id.properties.IdGeneratorConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/19 9:52
 * @description: 基于环形缓冲区（RingBuffer）的高性能 ID 生成器实现<BR/>
 * <p>
 * 作用：<BR/>
 * 继承自 DefaultUidGenerator，通过预生成 ID 并缓存在 RingBuffer 中，<BR/>
 * 实现了异步填充、同步获取的高性能架构，解决了高并发下频繁进行时间戳获取和位运算的性能瓶颈。<BR/>
 * <p>
 * 核心机制：<BR/>
 * 1. RingBuffer：无锁化的环形数组，用于存储预生成的 ID。<BR/>
 * 2. BufferPaddingExecutor：负责在后台线程或定时任务中填充 ID。<BR/>
 * 3. CachedIdGenerator：对外提供 take() 接口，直接从缓冲区获取 ID。<BR/>
 */
@Slf4j
public class CachedIdGenerator extends DefaultIdGenerator implements DisposableBean {

    /**
     * 百分比值 (0-100)，当剩余ID低于此百分比时触发填充
     */
    private final int paddingFactor ;
    /**
     * 定时填充间隔（秒），若为空则使用惰性填充（仅在阈值触发时填充）
     */
    private final Long scheduleInterval;

    /**
     * 默认的缓冲区扩容幂次
     * RingBuffer 的大小计算公式：(MaxSequence + 1) << boostPower
     * 默认值为 3，意味着缓冲区大小是序列号范围的 2^3 = 8 倍。
     * <p>
     * 毫秒级下 MaxSequence = 4095 (12位)，因此默认缓冲区大小 = 4096 * 8 = 32768
     */
    private static final int DEFAULT_BOOST_POWER = 3;

    /**
     * boostPower: 缓冲区扩容因子（2的幂次）
     */
    private int boostPower = DEFAULT_BOOST_POWER;

    /**
     * 当缓冲区满且无法放入新ID时的处理策略（生产者侧）
     */
    private RejectedPutBufferHandler rejectedPutBufferHandler;
    /**
     * 当缓冲区空且无法获取ID时的处理策略（消费者侧）
     */
    private RejectedTakeBufferHandler rejectedTakeBufferHandler;
    /**
     * ringBuffer: 环形缓冲区，存储 Long 类型的 ID
     */
    private RingBuffer ringBuffer;
    /**
     * 缓冲区填充执行器，负责调用 nextIdsForOneMillisecond 填充数据
     */
    private BufferPaddingExecutor bufferPaddingExecutor;

    public CachedIdGenerator(WorkerIdAssigner workerIdAssigner, IdGeneratorConfigProperties  idGeneratorConfigProperties) {
        super(workerIdAssigner, idGeneratorConfigProperties);
        this.paddingFactor = idGeneratorConfigProperties.getPaddingFactor();
        this.scheduleInterval = idGeneratorConfigProperties.getScheduleInterval();
    }

    /**
     * Spring 生命周期回调方法
     * 在 Bean 属性设置完成后调用
     * <p>
     * 执行流程：
     * 1. 调用父类方法初始化 WorkerId 和 BitsAllocator（位分配器）
     * 2. 初始化 RingBuffer 和填充执行器
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 1. 初始化父类组件 (WorkerIdAssigner, BitsAllocator)
        super.afterPropertiesSet();
        // 2. 初始化环形缓冲区
        this.initRingBuffer();
        log.info("Initialized RingBuffer successfully.");
    }

    /**
     * 获取唯一ID (核心业务方法)
     * <p>
     * 作用：从 RingBuffer 中取出一个 UID
     * 线程安全性：由 RingBuffer 内部的 CAS 操作保证线程安全
     *
     * @return 64位 Long 型 UID
     */
    @Override
    public long getID() {
        try {
            // 从环形缓冲区获取一个ID
            return ringBuffer.take();
        } catch (Exception e) {
            log.error("Generate unique id exception. ", e);
            throw new IdGeneratorException(e);
        }
    }

    /**
     * 解析 UID (反向解析)
     * <p>
     * 作用：将 64 位 ID 解析为时间、机器ID、序列号等信息
     * 注意：此处直接委托给父类 DefaultUidGenerator 处理，因为解析逻辑与生成逻辑无关
     *
     * @param uid 64位ID
     * @return JSON格式的解析结果字符串
     */
    @Override
    public String parseID(long uid) {
        return super.parseID(uid);
    }

    /**
     * Spring 销毁回调方法
     * 在 Bean 销毁时关闭填充执行器的线程池
     */
    @Override
    public void destroy() throws Exception {
        bufferPaddingExecutor.shutdown();
    }

    /**
     * 生成指定毫秒内所有的 ID 列表 (用于填充缓冲区)
     * <p>
     * 作用：在某一毫秒内，生成从序列号 0 到 MaxSequence 的所有 UID
     * 优化点：利用了序列号连续的特性，避免了重复的时间戳获取和位运算。
     * <p>
     * 计算逻辑：
     * 1. 计算该毫秒内的第一个 UID (序列号为0)
     * 2. 后续 UID = 第一个 UID + 偏移量 (offset)
     * <p>
     * 毫秒级特性：
     * - 12位序列号，MaxSequence = 4095
     * - 单毫秒可生成 4096 个 UID
     * - 单机 QPS 可达 4,096,000/s
     *
     * @param currentMillisecond 当前时间戳（毫秒）
     * @return UID 列表，大小为 MaxSequence + 1 (通常是 4096 个)
     */
    protected List<Long> nextIdsForOneMillisecond(long currentMillisecond) {
        // 1. 计算列表大小 (即该毫秒内能生成的最大ID数量)
        int listSize = (int) bitsAllocator.getMaxSequence() + 1;
        List<Long> uidList = new ArrayList<>(listSize);

        // 2. 分配该毫秒内的第一个 UID (序列号为0)
        // 注意：此处的时间差是相对于纪元时间 (epochMilliseconds) 的
        long firstSeqUid = bitsAllocator.allocate(currentMillisecond - epochMilliseconds, workerId, 0L);

        // 3. 利用连续性，通过加法生成后续 UID (性能极高，无位运算)
        for (int offset = 0; offset < listSize; offset++) {
            uidList.add(firstSeqUid + offset);
        }

        return uidList;
    }

    /**
     * 初始化环形缓冲区和填充执行器
     * <p>
     * 作用：构建 RingBuffer 实例和 BufferPaddingExecutor 实例，
     * 并建立它们之间的关联，最后启动填充线程。
     * <p>
     * 毫秒级配置：
     * - MaxSequence = 4095 (12位)
     * - 默认缓冲区大小 = 4096 * 8 = 32768
     * - 填充因子默认 50%
     */
    private void initRingBuffer() {
        // 1. 计算环形缓冲区大小
        // 公式：(MaxSequence + 1) << boostPower
        // 例如：MaxSequence=4095 (12位), boostPower=3 -> Size = 4096 * 8 = 32768
        int bufferSize = ((int) bitsAllocator.getMaxSequence() + 1) << boostPower;
        // 2. 创建 RingBuffer 实例
        this.ringBuffer = new RingBuffer(bufferSize, paddingFactor);
        log.info("Initialized ring buffer size:{}, paddingFactor:{}", bufferSize, paddingFactor);
        // 3. 创建填充执行器
        // 使用 Lambda 表达式引用 nextIdsForOneMillisecond 方法
        // usingSchedule: 是否使用定时填充 (true=定时, false=阈值触发)
        boolean usingSchedule = (scheduleInterval != null && scheduleInterval > 0);
        this.bufferPaddingExecutor = new BufferPaddingExecutor(ringBuffer, this::nextIdsForOneMillisecond, usingSchedule);
        if (usingSchedule) {
            bufferPaddingExecutor.setScheduleInterval(scheduleInterval);
        }
        log.info("Initialized BufferPaddingExecutor. Using schedule:{}, interval:{}", usingSchedule, scheduleInterval);
        // 4. 将拒绝策略设置到 RingBuffer 中
        this.ringBuffer.setBufferPaddingExecutor(bufferPaddingExecutor);
        if (rejectedPutBufferHandler != null) {
            this.ringBuffer.setRejectedPutHandler(rejectedPutBufferHandler);
        }
        if (rejectedTakeBufferHandler != null) {
            this.ringBuffer.setRejectedTakeHandler(rejectedTakeBufferHandler);
        }

        // 5. 首次填充：启动时立即将缓冲区填满
        bufferPaddingExecutor.paddingBuffer();
        // 6. 启动后台填充线程 (如果是定时模式，则启动定时器；如果是阈值模式，则启动守护线程)
        bufferPaddingExecutor.start();
    }

    /**
     * 设置缓冲区扩容因子
     *
     * @param boostPower 2的幂次，必须为正整数
     */
    public void setBoostPower(int boostPower) {
        Assert.isTrue(boostPower > 0, "Boost power must be positive!");
        this.boostPower = boostPower;
    }

    /**
     * 设置拒绝放入策略
     *
     * @param rejectedPutBufferHandler 当缓冲区满时的处理策略
     */
    public void setRejectedPutBufferHandler(RejectedPutBufferHandler rejectedPutBufferHandler) {
        Assert.notNull(rejectedPutBufferHandler, "RejectedPutBufferHandler can't be null!");
        this.rejectedPutBufferHandler = rejectedPutBufferHandler;
    }

    /**
     * 设置拒绝获取策略
     *
     * @param rejectedTakeBufferHandler 当缓冲区空时的处理策略
     */
    public void setRejectedTakeBufferHandler(RejectedTakeBufferHandler rejectedTakeBufferHandler) {
        Assert.notNull(rejectedTakeBufferHandler, "RejectedTakeBufferHandler can't be null!");
        this.rejectedTakeBufferHandler = rejectedTakeBufferHandler;
    }
}
