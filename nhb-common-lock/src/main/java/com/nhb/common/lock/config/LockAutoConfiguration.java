package com.nhb.common.lock.config;

import com.nhb.common.lock.aspectj.LockAnnotationAdvisor;
import com.nhb.common.lock.aspectj.LockInterceptor;
import com.nhb.common.lock.core.*;
import com.nhb.common.lock.executor.LockExecutor;
import com.nhb.common.lock.executor.RedissonLockExecutor;
import com.nhb.common.lock.properties.LockConfigProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 15:49
 * @description:
 */
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(LockConfigProperties.class)
public class LockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LockTemplate lockTemplate(List<LockExecutor> executors, LockConfigProperties lockConfigProperties) {
        LockTemplate lockTemplate = new LockTemplate();
        lockTemplate.setLockConfigProperties(lockConfigProperties);
        lockTemplate.setExecutors(executors);
        return lockTemplate;
    }

    @Bean
    @ConditionalOnMissingBean
    public LockKeyBuilder lockKeyBuilder(BeanFactory beanFactory) {
        return new DefaultLockKeyBuilder(beanFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public LockFailureStrategy lockFailureStrategy() {
        return new DefaultLockFailureStrategy();
    }

    @Bean
    @ConditionalOnMissingBean
    public LockInterceptor lockInterceptor(@Lazy LockTemplate lockTemplate, List<LockKeyBuilder> keyBuilders,
                                           List<LockFailureStrategy> failureStrategies, LockConfigProperties lockConfigProperties) {
        return new LockInterceptor(lockTemplate, keyBuilders, failureStrategies, lockConfigProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public LockAnnotationAdvisor lockAnnotationAdvisor(LockInterceptor lockInterceptor) {
        return new LockAnnotationAdvisor(lockInterceptor, Ordered.HIGHEST_PRECEDENCE);
    }

    @Bean
    @Order(100)
    @ConditionalOnClass(Redisson.class)
    public RedissonLockExecutor redissonLockExecutor(RedissonClient redissonClient) {
        return new RedissonLockExecutor(redissonClient);
    }

}
