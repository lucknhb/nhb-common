package com.nhb.common.lock.core;

import org.aopalliance.intercept.MethodInvocation;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 16:12
 * @description:
 */
public interface LockKeyBuilder {
    /**
     * 构建key
     *
     * @param invocation     invocation
     * @param definitionKeys 定义
     * @return key
     */
    String buildKey(MethodInvocation invocation, String[] definitionKeys);
}
