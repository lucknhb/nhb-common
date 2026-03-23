package com.nhb.common.encrypt.core;

import cn.hutool.core.util.ReflectUtil;
import com.nhb.common.encrypt.encryptor.IEncryptor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 10:55
 * @description: API接口加解密管理类
 */
@Slf4j
@NoArgsConstructor
public class EncryptorManager {
    /**
     * TODO 加密头 可考虑剔除
     */
    private static final String ENCRYPT_HEADER = "ENC_";
    /**
     * 缓存加密器
     */
    Map<Integer, IEncryptor> encryptorMap = new ConcurrentHashMap<>();

    /**
     * 注册加密执行者到缓存
     *
     * @param encryptContext 加密执行者需要的相关配置参数
     */
    public IEncryptor registerAndGetEncryptor(EncryptContext encryptContext) {
        int key = encryptContext.hashCode();
        if (encryptorMap.containsKey(key)) {
            return encryptorMap.get(key);
        }
        IEncryptor encryptor = ReflectUtil.newInstance(encryptContext.getAlgorithm().getClazz(), encryptContext);
        encryptorMap.put(key, encryptor);
        return encryptor;
    }

    /**
     * 移除缓存中的加密执行者
     *
     * @param encryptContext 加密执行者需要的相关配置参数
     */
    public void removeEncryptor(EncryptContext encryptContext) {
        this.encryptorMap.remove(encryptContext.hashCode());
    }

    /**
     * 根据配置进行加密。会进行本地缓存对应的算法和对应的秘钥信息。
     *
     * @param value          待加密的值
     * @param encryptContext 加密相关的配置信息
     */
    public String encrypt(String value, EncryptContext encryptContext) {
        if (StringUtils.startsWith(value, ENCRYPT_HEADER)) {
            return value;
        }
        IEncryptor encryptor = this.registerAndGetEncryptor(encryptContext);
        String encrypt = encryptor.encrypt(value, encryptContext.getEncode());
        return ENCRYPT_HEADER + encrypt;
    }

    /**
     * 根据配置进行解密
     *
     * @param value          待解密的值
     * @param encryptContext 加密相关的配置信息
     */
    public String decrypt(String value, EncryptContext encryptContext) {
        if (!StringUtils.startsWith(value, ENCRYPT_HEADER)) {
            return value;
        }
        IEncryptor encryptor = this.registerAndGetEncryptor(encryptContext);
        String str = StringUtils.removeStart(value, ENCRYPT_HEADER);
        return encryptor.decrypt(str);
    }
}
