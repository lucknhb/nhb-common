package com.nhb.common.sse.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

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
     * 端口
     */
    private int port;
    /**
     * 最大帧载荷
     */
    private int maxFrameSize;
    /**
     * Redis中订阅发布主题
     */
    private String sseTopic;
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
    /**
     * SSL配置
     */
    private Ssl ssl = new Ssl();

    /**
     * 内部类：SSL/TLS
     */
    @Data
    public static class Ssl {
        /**
         * 是否启用 WSS
         */
        private Boolean enabled = false;
        /**
         * 证书文件路径（支持 classpath: 或 file:）
         */
        private String certFilePath;
        /**
         * 私钥文件路径
         */
        private String keyFilePath;
        /**
         * 私钥密码（无密码则留空）
         */
        private String keyPassword;
        /**
         * 使用哪种 SSL 提供者：JDK 或 OPENSSL
         */
        private String provider = "OPENSSL";
        /**
         * 支持的 SSL 协议版本，多个用逗号分隔
         */
        private String protocols = "TLSv1.2,TLSv1.3";
        /**
         * 是否自动生成证书
         */
        private Boolean autoGenerateEnabled = false;
        /**
         * 证书域名
         */
        private String generateCertDomain = "localhost";
    }

}
