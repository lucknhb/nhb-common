package com.nhb.common.websocket.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 15:32
 * @description:
 */
@Data
@ConfigurationProperties(prefix = WebSocketConfigProperties.PREFIX)
public class WebSocketConfigProperties {
    public static final String PREFIX = "websocket";
    /**
     * 默认开启
     */
    private Boolean enabled = true;
    /**
     * 路径
     */
    private String path;
    /**
     *  设置访问源地址
     */
    private String allowedOrigins;
}
