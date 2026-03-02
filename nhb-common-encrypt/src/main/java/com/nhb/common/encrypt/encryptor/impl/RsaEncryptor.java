package com.nhb.common.encrypt.encryptor.impl;

import com.nhb.common.encrypt.core.EncryptContext;
import com.nhb.common.encrypt.encryptor.AbstractEncryptor;
import com.nhb.common.encrypt.enums.AlgorithmType;
import com.nhb.common.encrypt.enums.EncodeType;
import com.nhb.common.encrypt.utils.EncryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 11:12
 * @description: RSA算法实现
 */
@Slf4j
public class RsaEncryptor extends AbstractEncryptor {

    private final EncryptContext context;

    public RsaEncryptor(EncryptContext context) {
        super(context);
        String privateKey = context.getPrivateKey();
        String publicKey = context.getPublicKey();
        if (StringUtils.isAnyEmpty(privateKey, publicKey)) {
            throw new IllegalArgumentException("RSA公私钥均需要提供，公钥加密，私钥解密。");
        }
        this.context = context;
    }

    /**
     * 获得当前算法
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.RSA;
    }

    /**
     * 加密
     *
     * @param value      待加密字符串
     * @param encodeType 加密后的编码格式
     */
    @Override
    public String encrypt(String value, EncodeType encodeType) {
        if (encodeType == EncodeType.HEX) {
            return EncryptUtil.encryptByRsaHex(value, context.getPublicKey());
        } else {
            return EncryptUtil.encryptByRsa(value, context.getPublicKey());
        }
    }

    /**
     * 解密
     *
     * @param value      待加密字符串
     */
    @Override
    public String decrypt(String value) {
        return EncryptUtil.decryptByRsa(value, context.getPrivateKey());
    }
}
