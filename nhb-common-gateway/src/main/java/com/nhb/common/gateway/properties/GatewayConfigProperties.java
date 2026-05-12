package com.nhb.common.gateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/12 9:44
 * @description:
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = GatewayConfigProperties.PREFIX)
public class GatewayConfigProperties {
    public static final String PREFIX = "spring.cloud.gateway";
    /**
     * SSL安全证书信息
     */
    private Ssl ssl = new Ssl();

    @Data
    public static class Ssl{
        /**
         * 是否自动生成证书
         */
        private Boolean autoGenerateCertEnabled = false;
        /**
         * 使用哪种 SSL 提供者：JDK 或 OPENSSL
         */
        private String provider;
        /**
         * 支持的 SSL 协议版本，多个用逗号分隔
         */
        private String protocols;
        /**
         * 证书域名
         */
        private String generateCertDomain;
    }

}
