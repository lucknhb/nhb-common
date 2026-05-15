package com.nhb.common.lock.core;

import java.lang.reflect.Method;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 16:13
 * @description:
 */
public interface LockFailureStrategy {
    /**
     * 锁失败事件
     */
    void onLockFailure(String key, Method method, Object[] arguments);
}
