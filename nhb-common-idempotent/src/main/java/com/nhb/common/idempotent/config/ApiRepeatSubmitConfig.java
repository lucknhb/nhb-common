package com.nhb.common.idempotent.config;

import com.nhb.common.idempotent.aspectj.ApiRepeatSubmitAspect;
import com.nhb.common.redis.config.RedissonConfig;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 13:59
 * @description:
 */
@AutoConfigureAfter(RedissonConfig.class)
public class ApiRepeatSubmitConfig {

    @Bean
    public ApiRepeatSubmitAspect apiRepeatSubmitAspect(){
        return new ApiRepeatSubmitAspect();
    }
}
