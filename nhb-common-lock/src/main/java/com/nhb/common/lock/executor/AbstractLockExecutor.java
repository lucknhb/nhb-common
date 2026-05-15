package com.nhb.common.lock.executor;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 15:27
 * @description: 锁执行器抽象类
 */
public abstract class AbstractLockExecutor<T> implements LockExecutor<T> {

    /**
     * 获取锁实例
     * @param locked          是否已锁
     * @param lockInstance    锁实例
     * @return                返回锁实例
     */
    protected T obtainLockInstance(boolean locked, T lockInstance) {
        return locked ? lockInstance : null;
    }
}
