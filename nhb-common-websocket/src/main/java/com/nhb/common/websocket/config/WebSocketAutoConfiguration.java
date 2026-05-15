package com.nhb.common.websocket.config;

import com.nhb.common.core.factory.YamlPropertySourceFactory;
import com.nhb.common.security.config.SaTokenAutoConfiguration;
import com.nhb.common.websocket.auth.DefaultWebSocketAuthService;
import com.nhb.common.websocket.auth.WebSocketAuthService;
import com.nhb.common.websocket.core.WebSocketServer;
import com.nhb.common.websocket.properties.WebSocketConfigProperties;
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
 * @date 2026/3/17 15:33
 * @description:
 */
@AutoConfigureAfter({RedisAutoConfiguration.class, SaTokenAutoConfiguration.class})
@EnableConfigurationProperties(WebSocketConfigProperties.class)
@PropertySource(value = "classpath:websocket-default.yaml", factory = YamlPropertySourceFactory.class)
public class WebSocketAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(WebSocketAuthService.class)
    public DefaultWebSocketAuthService defaultWebSocketAuthService() {
        return new DefaultWebSocketAuthService();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WebSocketServer webSocketServer(WebSocketConfigProperties webSocketConfigProperties,
                                           WebSocketAuthService authService,
                                           ResourceLoader resourceLoader) {
        return new WebSocketServer(webSocketConfigProperties, authService, resourceLoader);
    }


}
