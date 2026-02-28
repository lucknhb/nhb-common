package com.nhb.common.core.utils;

import cn.hutool.core.thread.ThreadUtil;
import io.github.linpeilie.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/28 14:06
 * @description:
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadPoolUtil extends ThreadUtil {
    /**
     * 关闭线程池.
     *
     * @param executorService 执行器.
     * @param timeout         超时时间
     */
    public static void shutdown(ExecutorService executorService, int timeout) {
        log.info("Shutting down thread pool...");
        if (ObjectUtils.isNotNull(executorService) && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(timeout, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            } finally {
                log.info("Closing thread pool end...");
            }
        }
    }

    /**
     * 新建一个虚拟线程池
     */
    public static ExecutorService newVirtualTaskExecutor() {
        return Executors.newThreadPerTaskExecutor((r -> {
            Thread thread = new Thread(r);
            return Thread.ofVirtual()
                    .name("virtual-" + thread.getName())
                    .inheritInheritableThreadLocals(true)
                    .unstarted(r);
        }));
    }
}
