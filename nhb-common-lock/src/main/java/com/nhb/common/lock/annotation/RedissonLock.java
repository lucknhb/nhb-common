package com.nhb.common.lock.annotation;


import com.nhb.common.lock.executor.RedissonLockExecutor;

import java.lang.annotation.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 15:10
 * @description: 基于 {@link org.redisson.Redisson}实现的分布式锁
 */
@Lock(executor = RedissonLockExecutor.class)
@Target(value = {ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RedissonLock {
}
