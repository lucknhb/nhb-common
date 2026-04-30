package com.nhb.common.lock.core;

import com.nhb.common.lock.executor.LockExecutor;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 16:12
 * @description: 锁信息
 */
@Data
@AllArgsConstructor
public class LockInfo {
    /**
     * 锁名称
     */
    private String lockKey;

    /**
     * 锁值
     */
    private String lockValue;

    /**
     * 过期时间
     */
    private Long expire;

    /**
     * 获取锁超时时间
     */
    private Long acquireTimeout;

    /**
     * 获取锁次数
     */
    private int acquireCount;

    /**
     * 锁实例
     */
    private Object lockInstance;

    /**
     * 锁执行器
     */
    private LockExecutor lockExecutor;
}
