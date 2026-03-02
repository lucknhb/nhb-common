package com.nhb.common.encrypt.encryptor;

import com.nhb.common.encrypt.enums.AlgorithmType;
import com.nhb.common.encrypt.enums.EncodeType;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 10:50
 * @description: 加解密接口
 */
public interface IEncryptor {
    /**
     * 获得当前算法
     */
    AlgorithmType algorithm();

    /**
     * 加密
     *
     * @param value      待加密字符串
     * @param encodeType 加密后的编码格式
     * @return 加密后的字符串
     */
    String encrypt(String value, EncodeType encodeType);

    /**
     * 解密
     *
     * @param value      待加密字符串
     * @return 解密后的字符串
     */
    String decrypt(String value);
}
