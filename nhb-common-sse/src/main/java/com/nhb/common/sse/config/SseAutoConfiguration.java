package com.nhb.common.sse.config;

import com.nhb.common.core.factory.YamlPropertySourceFactory;
import com.nhb.common.sse.auth.DefaultSseAuthService;
import com.nhb.common.sse.auth.SseAuthService;
import com.nhb.common.sse.core.SseChannelHolder;
import com.nhb.common.sse.core.SseServer;
import com.nhb.common.sse.listeners.SseTopicListener;
import com.nhb.common.sse.properties.SseConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ResourceLoader;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 8:38
 * @description:
 */
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties(SseConfigProperties.class)
@PropertySource(value = "classpath:sse-default.yaml", factory = YamlPropertySourceFactory.class)
public class SseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SseAuthService.class)
    public DefaultSseAuthService sseAuthService() {
        return new DefaultSseAuthService();
    }

    @Bean(initMethod = "start",destroyMethod = "stop")
    public SseServer sseServer(SseConfigProperties sseConfigProperties, SseAuthService sseAuthService, ResourceLoader resourceLoader) {
        return new SseServer(sseConfigProperties, sseAuthService, resourceLoader);
    }

    @Bean(destroyMethod = "shutdown")
    public SseChannelHolder sseChannelHolder(SseConfigProperties sseConfigProperties) {
        return new SseChannelHolder(sseConfigProperties);
    }

    @Bean
    public SseTopicListener sseTopicListener(SseChannelHolder sseChannelHolder) {
        return new SseTopicListener(sseChannelHolder);
    }
}
