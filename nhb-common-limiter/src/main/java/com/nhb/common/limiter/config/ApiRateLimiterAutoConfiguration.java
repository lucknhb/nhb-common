package com.nhb.common.limiter.config;

import com.nhb.common.limiter.aspectj.ApiRateLimiterAspect;
import com.nhb.common.redis.config.RedissonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 10:46
 * @description:
 */
@AutoConfigureAfter(RedissonAutoConfiguration.class)
public class ApiRateLimiterAutoConfiguration {

    @Bean
    public ApiRateLimiterAspect apiRateLimiterAspect() {
        return new ApiRateLimiterAspect();
    }
}
