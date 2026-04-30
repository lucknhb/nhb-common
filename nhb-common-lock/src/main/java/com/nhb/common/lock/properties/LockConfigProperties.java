package com.nhb.common.lock.properties;

import com.nhb.common.lock.core.LockFailureStrategy;
import com.nhb.common.lock.core.LockKeyBuilder;
import com.nhb.common.lock.executor.LockExecutor;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 15:48
 * @description:
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = LockConfigProperties.PREFIX)
public class LockConfigProperties {

    public static final String PREFIX = "lock";

    /**
     * 锁key前缀
     */
    private String lockKeyPrefix = "lock";

    /**
     * 过期时间 单位：毫秒
     */
    private Long expire = 30000L;

    /**
     * 获取锁超时时间 单位：毫秒
     */
    private Long acquireTimeout = 3000L;

    /**
     * 获取锁失败时重试时间间隔 单位：毫秒
     */
    private Long retryInterval = 100L;

    /**
     * 默认执行器，不设置默认取容器第一个
     */
    private Class<? extends LockExecutor> primaryExecutor;
    /**
     * 默认失败策略，不设置存在多个时默认根据PriorityOrdered、Ordered排序规则选择|注入顺序选择
     */
    private Class<? extends LockFailureStrategy> primaryFailureStrategy;
    /**
     * 默认key生成策略，不设置存在多个时默认根据PriorityOrdered、Ordered排序规则选择|注入顺序选择
     */
    private Class<? extends LockKeyBuilder> primaryKeyBuilder;
}
