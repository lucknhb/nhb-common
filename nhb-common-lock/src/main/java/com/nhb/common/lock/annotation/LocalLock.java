package com.nhb.common.lock.annotation;

import com.nhb.common.lock.executor.LocalLockExecutor;

import java.lang.annotation.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 15:10
 * @description: 基于{@link LocalLockExecutor}实现的本地锁，仅适用于单机环境
 */
@Lock(executor = LocalLockExecutor.class)
@Target(value = {ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface LocalLock {
}
