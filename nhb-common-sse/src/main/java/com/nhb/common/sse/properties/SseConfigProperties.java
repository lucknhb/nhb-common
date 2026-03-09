package com.nhb.common.sse.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 8:39
 * @description: SSE 配置项
 */
@ConfigurationProperties(prefix = SseConfigProperties.PREFIX)
public class SseConfigProperties {
    public static final String PREFIX = "sse";
    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 请求地址
     */
    public String path;

}
