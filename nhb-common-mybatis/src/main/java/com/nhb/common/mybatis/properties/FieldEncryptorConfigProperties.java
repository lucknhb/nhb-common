package com.nhb.common.mybatis.properties;

import com.nhb.common.encrypt.enums.AlgorithmType;
import com.nhb.common.encrypt.enums.EncodeType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/23 14:17
 * @description:
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = FieldEncryptorConfigProperties.PREFIX)
public class FieldEncryptorConfigProperties {
    public static final String PREFIX = "mybatis-plus.field-encryptor";

    /**
     * 过滤开关
     */
    private Boolean enabled = false;

    /**
     * 默认算法
     */
    private AlgorithmType algorithm;

    /**
     * 安全秘钥
     */
    private String password;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * 编码方式，base64/hex
     */
    private EncodeType encode;

}
