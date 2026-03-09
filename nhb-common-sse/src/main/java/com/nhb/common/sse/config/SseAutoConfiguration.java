package com.nhb.common.sse.config;

import com.nhb.common.sse.core.SseEmitterManager;
import com.nhb.common.sse.listeners.SseTopicListener;
import com.nhb.common.sse.properties.SseConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 8:38
 * @description:
 */
@AutoConfiguration
@EnableConfigurationProperties(SseConfigProperties.class)
@ConditionalOnBooleanProperty(prefix = SseConfigProperties.PREFIX ,name = "enabled" ,havingValue = true ,matchIfMissing = true)
public class SseAutoConfiguration {

    @Bean
    public SseEmitterManager sseEmitterManager(){
        return new SseEmitterManager();
    }

    @Bean
    public SseTopicListener sseTopicListener(SseEmitterManager sseEmitterManager){
        return new SseTopicListener(sseEmitterManager);
    }
}
