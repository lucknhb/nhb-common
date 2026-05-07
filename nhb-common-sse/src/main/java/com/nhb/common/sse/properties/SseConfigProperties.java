package com.nhb.common.sse.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 8:39
 * @description: SSE 配置项
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = SseConfigProperties.PREFIX)
public class SseConfigProperties {
    public static final String PREFIX = "sse";

    /**
     * 请求地址
     */
    private String path;

    /**
     * 心跳间隔时间 S
     */
    private Integer heartbeatInterval;

    /**
     * Redis中订阅发布主题
     */
    private String sseTopic;

}
