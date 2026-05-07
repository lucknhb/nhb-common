package com.nhb.common.sse.config;

import com.nhb.common.core.factory.YamlPropertySourceFactory;
import com.nhb.common.sse.controller.SseController;
import com.nhb.common.sse.core.SseEmitterManager;
import com.nhb.common.sse.listeners.SseTopicListener;
import com.nhb.common.sse.properties.SseConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 8:38
 * @description:
 */
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(SseConfigProperties.class)
@PropertySource(value = "classpath:sse-default.yaml",factory = YamlPropertySourceFactory.class)
@ConditionalOnBooleanProperty(prefix = SseConfigProperties.PREFIX ,name = "enabled" ,havingValue = true ,matchIfMissing = true)
public class SseAutoConfiguration {

    @Bean(destroyMethod = "shutdown")
    public SseEmitterManager sseEmitterManager(SseConfigProperties sseConfigProperties){
        return new SseEmitterManager(sseConfigProperties);
    }

    @Bean
    public SseController sseController(SseEmitterManager sseEmitterManager){
        return new SseController(sseEmitterManager);
    }

    @Bean
    public SseTopicListener sseTopicListener(SseEmitterManager sseEmitterManager){
        return new SseTopicListener(sseEmitterManager);
    }
}
