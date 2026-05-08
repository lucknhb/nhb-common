package com.nhb.common.websocket.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 15:32
 * @description:
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = WebSocketConfigProperties.PREFIX)
public class WebSocketConfigProperties {
    public static final String PREFIX = "websocket";
    /**
     * 路径
     */
    private String path;
    /**
     *  设置访问源地址
     */
    private String allowedOrigins;
}
