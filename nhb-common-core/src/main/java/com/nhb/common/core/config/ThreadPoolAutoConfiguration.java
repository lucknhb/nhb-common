package com.nhb.common.core.config;

import com.nhb.common.core.utils.SpringContextUtil;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.util.concurrent.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/13 15:16
 * @description:
 */
@Slf4j
@Import(SpringContextUtil.class)
@AutoConfiguration
public class ThreadPoolAutoConfiguration {
    /**
     * 核心线程数 = cpu 核心数 * 2
     */
    private final int core = Runtime.getRuntime().availableProcessors() * 2;

    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 执行周期性或定时任务
     */
    @Bean(name = "scheduledExecutorService")
    protected ScheduledExecutorService scheduledExecutorService() {
        // daemon 必须为 true
        BasicThreadFactory.Builder builder = new BasicThreadFactory.Builder().daemon(true);
        if (SpringContextUtil.isVirtual()) {
            builder.namingPattern("virtual-schedule-pool-%d").wrappedFactory(new VirtualThreadTaskExecutor().getVirtualThreadFactory());
        } else {
            builder.namingPattern("schedule-pool-%d");
        }
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(core,
                builder.build(),
                new ThreadPoolExecutor.CallerRunsPolicy()) {
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                printException(r, t);
            }
        };
        this.scheduledExecutorService = scheduledThreadPoolExecutor;
        return scheduledThreadPoolExecutor;
    }

    /**
     * 销毁事件
     * 停止线程池
     * 先使用shutdown, 停止接收新任务并尝试完成所有已存在任务.
     * 如果超时, 则调用shutdownNow, 取消在workQueue中Pending的任务,并中断所有阻塞函数.
     * 如果仍然超時，則強制退出.
     * 另对在shutdown时线程本身被调用中断做了处理.
     */
    @PreDestroy
    public void destroy() {
        try {
            log.info("==== Close Executor Pool====");
            ScheduledExecutorService pool = scheduledExecutorService;
            if (pool != null && !pool.isShutdown()) {
                pool.shutdown();
                try {
                    if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                        pool.shutdownNow();
                        if (!pool.awaitTermination(120, TimeUnit.SECONDS)) {
                            log.info("Pool did not terminate");
                        }
                    }
                } catch (InterruptedException ie) {
                    pool.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 打印线程异常信息
     */
    public static void printException(Runnable r, Throwable t) {
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone()) {
                    future.get();
                }
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            log.error(t.getMessage(), t);
        }
    }
}
