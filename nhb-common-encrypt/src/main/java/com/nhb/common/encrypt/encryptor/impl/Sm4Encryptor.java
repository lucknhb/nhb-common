package com.nhb.common.encrypt.encryptor.impl;

import com.nhb.common.encrypt.core.EncryptContext;
import com.nhb.common.encrypt.encryptor.AbstractEncryptor;
import com.nhb.common.encrypt.enums.AlgorithmType;
import com.nhb.common.encrypt.enums.EncodeType;
import com.nhb.common.encrypt.utils.EncryptUtil;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 11:14
 * @description: 国密SM4算法实现
 */
public class Sm4Encryptor extends AbstractEncryptor {

    private final EncryptContext context;

    public Sm4Encryptor(EncryptContext context) {
        super(context);
        this.context = context;
    }

    /**
     * 获得当前算法
     */
    @Override
    public AlgorithmType algorithm() {
        return AlgorithmType.SM4;
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
            return EncryptUtil.encryptBySm4Hex(value, context.getPassword());
        } else {
            return EncryptUtil.encryptBySm4(value, context.getPassword());
        }
    }

    /**
     * 解密
     *
     * @param value      待加密字符串
     */
    @Override
    public String decrypt(String value) {
        return EncryptUtil.decryptBySm4(value, context.getPassword());
    }
}
