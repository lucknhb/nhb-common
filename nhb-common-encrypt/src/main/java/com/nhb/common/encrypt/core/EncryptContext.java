package com.nhb.common.encrypt.core;

import com.nhb.common.encrypt.enums.AlgorithmType;
import com.nhb.common.encrypt.enums.EncodeType;
import lombok.Data;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 10:54
 * @description: 加密上下文 用于encryptor传递必要的参数
 */
@Data
public class EncryptContext {
    /**
     * 默认算法
     */
    private AlgorithmType algorithm;

    /**
     * 安全秘钥 Base64编码后的秘钥
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
