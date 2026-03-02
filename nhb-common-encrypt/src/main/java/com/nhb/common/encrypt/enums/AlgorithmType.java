package com.nhb.common.encrypt.enums;

import com.nhb.common.encrypt.encryptor.AbstractEncryptor;
import com.nhb.common.encrypt.encryptor.impl.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 10:52
 * @description: 算法类型
 */
@Getter
@AllArgsConstructor
public enum AlgorithmType {
    /**
     * 默认走yml配置
     */
    DEFAULT(null),

    /**
     * BASE64
     */
    BASE64(Base64Encryptor.class),

    /**
     * AES
     */
    AES(AesEncryptor.class),

    /**
     * RSA
     */
    RSA(RsaEncryptor.class),

    /**
     * SM2
     */
    SM2(Sm2Encryptor.class),

    /**
     * SM4
     */
    SM4(Sm4Encryptor.class);

    private final Class<? extends AbstractEncryptor> clazz;
}
