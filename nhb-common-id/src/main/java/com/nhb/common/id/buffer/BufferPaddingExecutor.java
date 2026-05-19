package com.nhb.common.id.buffer;

import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.id.core.PaddedAtomicLong;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 15:34
 * @description: RingBuffer 填充执行器<BR/>
 * <p>
 * 负责管理线程池，将 UID 填充到 RingBuffer 中<BR/>
 * 支持两种填充模式：<BR/>
 * 1. 定时填充：后台线程按固定频率检查并填充<BR/>
 * 2. 立即填充：当 RingBuffer 剩余空间不足时，由业务线程触发紧急填充<BR/>
 */
@Slf4j
public class BufferPaddingExecutor {

    /**
     * 线程池名称常量
     */
    private static final String WORKER_NAME = "RingBuffer-Padding-Worker";
    private static final String SCHEDULE_NAME = "RingBuffer-Padding-Schedule";
    /**
     * 默认定时填充间隔：5 分钟（单位：秒）
     * 即使没有请求，后台线程也会每 5 分钟检查一次缓冲区是否需要填充。
     */
    // 5 minutes
    private static final long DEFAULT_SCHEDULE_INTERVAL = 5 * 60L;

    /**
     * 标记填充任务是否正在运行，防止并发重复填充
     */
    private final AtomicBoolean running;

    /**
     * 记录最后被消费的时间点（单位：毫秒）
     * 在 ID 生成算法中，通常会“借用”未来的时间，这里记录了借用到了哪一秒。
     */
    private final PaddedAtomicLong lastTime;

    /**
     * 核心数据结构：环形缓冲区
     */
    private final RingBuffer ringBuffer;
    /**
     * ID 提供者：负责实际生成一批 ID 的接口
     */
    private final BufferedIdProvider idProvider;

    /**
     * 业务线程池：用于执行异步填充任务（紧急填充）
     */
    private final ExecutorService bufferPadExecutors;
    /**
     * 定时调度线程池：用于执行定时填充任务
     */
    private final ScheduledExecutorService bufferPadSchedule;
    /**
     * 调度间隔时间（单位：秒）
     */
    @Setter
    private long scheduleInterval = DEFAULT_SCHEDULE_INTERVAL;

    /**
     * 构造函数（默认启用定时填充）
     */
    public BufferPaddingExecutor(RingBuffer ringBuffer, BufferedIdProvider idProvider) {
        this(ringBuffer, idProvider, true);
    }

    /**
     * 全参构造函数
     *
     * @param ringBuffer    环形缓冲区实例
     * @param idProvider    ID 提供者实例
     * @param usingSchedule 是否启用定时填充线程
     */
    public BufferPaddingExecutor(RingBuffer ringBuffer, BufferedIdProvider idProvider, boolean usingSchedule) {
        this.running = new AtomicBoolean(false);
        // 初始化 lastSecond 为当前时间的秒数
        // 【注意】这里使用了 PaddedAtomicLong 防止伪共享，提高并发性能
        this.lastTime = new PaddedAtomicLong(System.currentTimeMillis());
        this.ringBuffer = ringBuffer;
        this.idProvider = idProvider;
        //使用容器中的线程池
        int cores = Runtime.getRuntime().availableProcessors();
        bufferPadExecutors = SpringContextUtil.getBean(ScheduledExecutorService.class);
        // 根据配置决定是否初始化定时调度线程池
        if (usingSchedule) {
            bufferPadSchedule = SpringContextUtil.getBean(ScheduledExecutorService.class);
        } else {
            bufferPadSchedule = null;
        }
    }

    /**
     * 启动定时填充任务
     * 在应用启动时调用，开始后台的定时填充逻辑。
     */
    public void start() {
        if (bufferPadSchedule != null) {
            // 延迟 scheduleInterval 秒后开始执行，执行间隔为 scheduleInterval 秒
            bufferPadSchedule.scheduleWithFixedDelay(this::paddingBuffer, scheduleInterval, scheduleInterval, TimeUnit.SECONDS);
        }
    }

    /**
     * 关闭所有线程池资源
     */
    public void shutdown() {
        if (!bufferPadExecutors.isShutdown()) {
            bufferPadExecutors.shutdownNow();
        }

        if (bufferPadSchedule != null && !bufferPadSchedule.isShutdown()) {
            bufferPadSchedule.shutdownNow();
        }
    }

    /**
     * 检查填充任务是否正在运行
     */
    public boolean isRunning() {
        return running.get();
    }

    /**
     * 异步填充（由业务线程触发）
     * 当 RingBuffer 即将耗尽时，提交一个任务到线程池去填充，而不阻塞当前获取 ID 的线程。
     */
    public void asyncPadding() {
        bufferPadExecutors.submit(this::paddingBuffer);
    }


    /**
     * 核心填充逻辑
     * 1. 获取下一批 ID（基于时间单位）
     * 2. 循环放入 RingBuffer，直到填满或数据用完
     */
    public void paddingBuffer() {
        log.debug("Ready to padding buffer lastSecond:{}. {}", lastTime.get(), ringBuffer);
        // CAS 操作：确保同一时间只有一个线程在执行填充
        if (!running.compareAndSet(false, true)) {
            log.info("Padding buffer is still running. {}", ringBuffer);
            return;
        }
        boolean isFullRingBuffer = false;
        while (!isFullRingBuffer) {
            // 1. 获取下一个时间单位（毫秒）对应的一批 ID
            // 【关键点】这里调用了 incrementAndGet，时间单位是毫秒
            List<Long> uidList = idProvider.provide(lastTime.incrementAndGet());
            // 2. 将这批 UID 放入 RingBuffer
            for (Long uid : uidList) {
                // put 方法返回 false 代表 RingBuffer 已满
                isFullRingBuffer = !ringBuffer.put(uid);
                if (isFullRingBuffer) {
                    break;
                }
            }
        }

        // 填充结束，重置运行状态
        running.compareAndSet(true, false);
        log.debug("End to padding buffer lastSecond:{}. {}", lastTime.get(), ringBuffer);
    }
}
