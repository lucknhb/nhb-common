package com.nhb.common.encrypt.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 11:20
 * @description: 接口加解密配置
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = ApiEncryptProperties.PREFIX)
public class ApiEncryptProperties {
    public static final String PREFIX = "api-encrypt";

    /**
     * 请求时数据解密标识头
     */
    private String apiEncryptHeaderFlag;

    /**
     * 响应加密公钥
     */
    private String publicKey;

    /**
     * 请求解密私钥
     */
    private String privateKey;


}
