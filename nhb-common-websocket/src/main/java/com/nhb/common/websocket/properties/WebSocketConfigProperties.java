package com.nhb.common.websocket.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 15:32
 * @description: webSocket配置项
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = WebSocketConfigProperties.PREFIX)
public class WebSocketConfigProperties {
    public static final String PREFIX = "websocket";
    /**
     * 端口
     */
    private int port;
    /**
     * 路径
     */
    private String path;
    /**
     * 读空闲超时秒（0禁用）
     */
    private int readerIdleTimeSeconds;
    /**
     * 写空闲超时秒
     */
    private int writerIdleTimeSeconds;
    /**
     * 全空闲超时秒
     */
    private int allIdleTimeSeconds;
    /**
     * 最大帧载荷
     */
    private int maxFrameSize;
    /**
     * Redisson主题名称
     */
    private String clusterTopic;
    /**
     * 离线用户信息主题名称
     */
    private String offlineMessageTopic;
    /**
     * 跨域请求是否允许携带或暴露凭证信息
     */
    private Boolean allowCredentials;
    /**
     * 设置访问源地址
     */
    private List<String> allowedOrigins;
    /**
     * 设置允许访问的请求头
     */
    private List<String> allowedHeaders;
}
