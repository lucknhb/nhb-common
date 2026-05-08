package com.nhb.common.websocket.config;

import cn.hutool.core.util.StrUtil;
import com.nhb.common.websocket.handler.WebSocketHandler;
import com.nhb.common.websocket.interceptor.WebSocketInterceptor;
import com.nhb.common.websocket.listener.WebSocketTopicListener;
import com.nhb.common.websocket.properties.WebSocketConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 15:33
 * @description:
 */
@EnableWebSocket
@AutoConfiguration
@EnableConfigurationProperties(WebSocketConfigProperties.class)
public class WebSocketAutoConfiguration {

    @Bean
    public WebSocketHandler webSocketHandler() {
        return new WebSocketHandler();
    }

    @Bean
    public WebSocketInterceptor webSocketInterceptor() {
        return new WebSocketInterceptor();
    }


    @Bean
    public WebSocketTopicListener webSocketTopicListener() {
        return new WebSocketTopicListener();
    }

    @Bean
    public WebSocketConfigurer webSocketConfigurer(WebSocketInterceptor webSocketInterceptor,
                                                   WebSocketHandler webSocketHandler,
                                                   WebSocketConfigProperties webSocketProperties) {
        // 如果WebSocket的路径为空，则设置默认路径为 "/ws"
        if (StrUtil.isBlank(webSocketProperties.getPath())) {
            webSocketProperties.setPath("/ws");
        }

        // 如果允许跨域访问的地址为空，则设置为 "*"，表示允许所有来源的跨域请求
        if (StrUtil.isBlank(webSocketProperties.getAllowedOrigins())) {
            webSocketProperties.setAllowedOrigins("*");
        }

        // 返回一个WebSocketConfigurer对象，用于配置WebSocket
        return registry -> registry
                // 添加WebSocket处理程序和拦截器到指定路径，设置允许的跨域来源
                .addHandler(webSocketHandler, webSocketProperties.getPath())
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins(webSocketProperties.getAllowedOrigins());
    }

}
