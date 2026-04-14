package com.nhb.common.encrypt.utils;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.SmUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.SM2;
import cn.hutool.crypto.symmetric.AES;
import com.nhb.common.core.exception.ServiceException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 11:04
 * @description: 加解密工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EncryptUtil {
    /**
     * 对称加密时的秘钥
     */
    public static final String PASSWORD = "password";
    /**
     * 盐值/向量
     */
    public static final String SALT = "salt";

    /**
     * 公钥
     */
    public static final String PUBLIC_KEY = "publicKey";

    /**
     * 私钥
     */
    public static final String PRIVATE_KEY = "privateKey";

    /**
     * Base64加密
     *
     * @param data 待加密数据
     * @return 加密后字符串
     */
    public static String encryptByBase64(String data) {
        return Base64Encoder.encode(data, StandardCharsets.UTF_8);
    }

    /**
     * Base64加密
     *
     * @param data 待加密数据
     * @return 加密后字符串
     */
    public static String encryptByBase64(byte[] data) {
        return Base64Encoder.encode(data);
    }

    /**
     * Base64解密
     *
     * @param data 待解密数据
     * @return 解密后字符串
     */
    public static String decryptByBase64ToString(String data) {
        return Base64Decoder.decodeStr(data, StandardCharsets.UTF_8);
    }

    /**
     * Base64解密
     *
     * @param data 待解密数据
     * @return 解密后字符串
     */
    public static byte[] decryptByBase64ToByte(String data) {
        return Base64Decoder.decode(data);
    }

    /**
     * 生成一份 AES对称加密 秘钥和向量
     * @return 集合值
     */
    public static Map<String,byte[]> generateAesKey() {
        SecureRandom secureRandom = new SecureRandom();
        // AES-256 秘钥
        byte[] aesKey = new byte[32];
        secureRandom.nextBytes(aesKey);
        //iv 向量
        byte[] aesIv = new byte[12];
        secureRandom.nextBytes(aesIv);
        Map<String,byte[]> result = new HashMap<>();
        result.put(PASSWORD, aesKey);
        result.put(SALT, aesIv);
        return  result;
    }

    /**
     * 通过 :  拼接  RSA分别加密后的BASE64值
     * @param aesKey     AES秘钥
     * @param aesIv      AES向量
     * @param publicKey  RSA 公钥
     * @return
     */
    public static String aesByRsaBase64Header(byte[] aesKey, byte[] aesIv,String publicKey) {
        // Rsa 公钥加密 AES秘钥 结果值为base64编码后的值
        String encryptPassword = EncryptUtil.encryptByRsaBase64(aesKey, publicKey);
        //RSA 公钥加密  IV值 结果值为base64编码后的值
        String encryptIV = EncryptUtil.encryptByRsaBase64(aesIv, publicKey);
        //通过 : 拼接
        return encryptPassword + ":" + encryptIV;
    }

    /**
     * 解析出AES GCM模式下 key与iv值
     * @param headerValue  password与salt 先base64b编码后进行Rsa加密 然后 通过 : 拼接的数据
     * @param privateKey   RSA私钥
     * @return             AES秘钥及向量值
     */
    public static Map<String,byte[]> parseHeaderAesWithRsa(String headerValue, String privateKey){
        if (StrUtil.isBlank(headerValue)) {
            throw new ServiceException("无法获取秘钥");
        }
        if (StrUtil.isBlank(privateKey)) {
            throw new ServiceException("无法获取秘钥");
        }
        String[] split = headerValue.split(StrPool.COLON);
        if (split.length != 2) {
            throw new ServiceException("非合法秘钥,请核实");
        }
        //获取RSA加密后的秘钥(Base64编码后的值)/向量(Base64编码后的值) 并解密
        Map<String,byte[]> map = new HashMap<>();
        map.put(PASSWORD,decryptByRsaToByte(split[0], privateKey));
        map.put(SALT,decryptByRsaToByte(split[1], privateKey));
        return map;
    }


    /**
     * AES 使用GCM模式 加密
     *
     * @param data     待加密数据
     * @param aesPassword 秘钥字符串
     * @param salt     盐值/向量值
     * @return 加密后字符串, 采用Base64编码
     */
    public static String encryptByAesBase64(String data, byte[] aesPassword, byte[] salt) {
        if (Objects.isNull(aesPassword) || Objects.isNull(salt)) {
            throw new ServiceException("AES需要传入秘钥/随机向量值信息");
        }
        int[] array = {16, 24, 32};
        if (!ArrayUtil.contains(array, aesPassword.length)) {
            throw new ServiceException("AES秘钥长度要求为16位、24位、32位");
        }
        return aesGcm(aesPassword,salt).encryptBase64(data, StandardCharsets.UTF_8);
    }


    /**
     * AES加密
     *
     * @param data     待加密数据
     * @param aesPassword 秘钥字节数组
     * @param salt    盐值/向量值
     * @return 加密后字符串, 采用Hex编码
     */
    public static String encryptByAesHex(String data, byte[] aesPassword,byte[] salt) {
        if (Objects.isNull(aesPassword) || Objects.isNull(salt)) {
            throw new ServiceException("AES需要传入秘钥/随机向量值信息");
        }
        // aes算法的秘钥要求是16位、24位、32位
        int[] array = {16, 24, 32};
        if (!ArrayUtil.contains(array, aesPassword.length)) {
            throw new ServiceException("AES秘钥长度要求为16位、24位、32位");
        }
        return aesGcm(aesPassword,salt).encryptHex(data, StandardCharsets.UTF_8);
    }

    /**
     * AES解密
     *
     * @param data     待解密数据
     * @param aesPassword 秘钥
     * @param salt       向量值/盐值
     * @return 解密后字符串
     */
    public static String decryptByAes(String data, byte[] aesPassword,byte[] salt) {
        if (Objects.isNull(aesPassword) || Objects.isNull(salt)) {
            throw new ServiceException("AES需要传入秘钥/随机向量值信息");
        }
        // aes算法的秘钥要求是16位、24位、32位
        int[] array = {16, 24, 32};
        if (!ArrayUtil.contains(array, aesPassword.length)) {
            throw new ServiceException("AES秘钥长度要求为16位、24位、32位");
        }
        return aesGcm(aesPassword,salt).decryptStr(data, StandardCharsets.UTF_8);
    }

    /**
     * SM4加密（Base64编码）
     *
     * @param data     待加密数据
     * @param password 秘钥字符串
     * @return 加密后字符串, 采用Base64编码
     */
    public static String encryptBySm4(String data, byte[] password) {
        if (Objects.isNull(password)) {
            throw new ServiceException("SM4需要传入秘钥信息");
        }
        // sm4算法的秘钥要求是16位长度
        int sm4PasswordLength = 16;
        if (sm4PasswordLength != password.length) {
            throw new ServiceException("SM4秘钥长度要求为16位");
        }
        return SmUtil.sm4(password).encryptBase64(data, StandardCharsets.UTF_8);
    }

    /**
     * SM4加密（Hex编码）
     *
     * @param data     待加密数据
     * @param password 秘钥
     * @return 加密后字符串, 采用Hex编码
     */
    public static String encryptBySm4Hex(String data, byte[] password) {
        if (Objects.isNull(password)) {
            throw new ServiceException("SM4需要传入秘钥信息");
        }
        // sm4算法的秘钥要求是16位长度
        int sm4PasswordLength = 16;
        if (sm4PasswordLength != password.length) {
            throw new ServiceException("SM4秘钥长度要求为16位");
        }
        return SmUtil.sm4(password).encryptHex(data, StandardCharsets.UTF_8);
    }

    /**
     * sm4解密
     *
     * @param data     待解密数据（可以是Base64或Hex编码）
     * @param password 秘钥
     * @return 解密后字符串
     */
    public static String decryptBySm4(String data, byte[] password) {
        if (Objects.isNull(password)) {
            throw new ServiceException("SM4需要传入秘钥信息");
        }
        // sm4算法的秘钥要求是16位长度
        int sm4PasswordLength = 16;
        if (sm4PasswordLength != password.length) {
            throw new ServiceException("SM4秘钥长度要求为16位");
        }
        return SmUtil.sm4(password).decryptStr(data, StandardCharsets.UTF_8);
    }

    /**
     * 产生sm2加解密需要的公钥和私钥
     *
     * @return 公私钥Map
     */
    public static Map<String, String> generateSm2Key() {
        Map<String, String> keyMap = new HashMap<>(2);
        SM2 sm2 = SmUtil.sm2();
        keyMap.put(PRIVATE_KEY, sm2.getPrivateKeyBase64());
        keyMap.put(PUBLIC_KEY, sm2.getPublicKeyBase64());
        return keyMap;
    }

    /**
     * sm2公钥加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return 加密后字符串, 采用Base64编码
     */
    public static String encryptBySm2(String data, String publicKey) {
        if (StrUtil.isBlank(publicKey)) {
            throw new ServiceException("SM2需要传入公钥进行加密");
        }
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        return sm2.encryptBase64(data, StandardCharsets.UTF_8, KeyType.PublicKey);
    }

    /**
     * sm2公钥加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return 加密后字符串, 采用Hex编码
     */
    public static String encryptBySm2Hex(String data, String publicKey) {
        if (StrUtil.isBlank(publicKey)) {
            throw new ServiceException("SM2需要传入公钥进行加密");
        }
        SM2 sm2 = SmUtil.sm2(null, publicKey);
        return sm2.encryptHex(data, StandardCharsets.UTF_8, KeyType.PublicKey);
    }

    /**
     * sm2私钥解密
     *
     * @param data       待解密数据
     * @param privateKey 私钥
     * @return 解密后字符串
     */
    public static String decryptBySm2(String data, String privateKey) {
        if (StrUtil.isBlank(privateKey)) {
            throw new ServiceException("SM2需要传入私钥进行解密");
        }
        SM2 sm2 = SmUtil.sm2(privateKey, null);
        return sm2.decryptStr(data, KeyType.PrivateKey, StandardCharsets.UTF_8);
    }

    /**
     * 产生RSA加解密需要的公钥和私钥
     *
     * @return 公私钥Map
     */
    public static Map<String, String> generateRsaKey() {
        Map<String, String> keyMap = new HashMap<>(2);
        RSA rsa = rsaOaep(null, null);
        PrivateKey privateKey = rsa.getPrivateKey();
        PublicKey publicKey = rsa.getPublicKey();
        if (Objects.isNull(privateKey) || Objects.isNull(publicKey)) {
            throw new ServiceException("生成RSA秘钥对失败");
        }
        keyMap.put(PRIVATE_KEY, Base64Encoder.encode(privateKey.getEncoded()));
        keyMap.put(PUBLIC_KEY, Base64Encoder.encode(publicKey.getEncoded()));
        return keyMap;
    }

    /**
     * rsa公钥加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return 加密后字符串, 采用Base64编码
     */
    public static String encryptByRsaBase64(String data, String publicKey) {
        if (StrUtil.isBlank(publicKey)) {
            throw new ServiceException("RSA需要传入公钥进行加密");
        }
        RSA rsa = rsaOaep(null, publicKey);
        return rsa.encryptBase64(data, StandardCharsets.UTF_8, KeyType.PublicKey);
    }

    /**
     * rsa公钥加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return 加密后字符串, 采用Base64编码
     */
    public static String encryptByRsaBase64(byte[] data, String publicKey) {
        if (StrUtil.isBlank(publicKey)) {
            throw new ServiceException("RSA需要传入公钥进行加密");
        }
        RSA rsa = rsaOaep(null, publicKey);
        return rsa.encryptBase64(data, KeyType.PublicKey);
    }

    /**
     * rsa公钥加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return 加密后字符串, 采用Hex编码
     */
    public static String encryptByRsaHex(String data, String publicKey) {
        if (StrUtil.isBlank(publicKey)) {
            throw new ServiceException("RSA需要传入公钥进行加密");
        }
        RSA rsa = rsaOaep(null, publicKey);
        return rsa.encryptHex(data, StandardCharsets.UTF_8, KeyType.PublicKey);
    }

    /**
     * rsa公钥加密
     *
     * @param data      待加密数据
     * @param publicKey 公钥
     * @return 加密后字符串, 采用Hex编码
     */
    public static String encryptByRsaHex(byte[] data, String publicKey) {
        if (StrUtil.isBlank(publicKey)) {
            throw new ServiceException("RSA需要传入公钥进行加密");
        }
        RSA rsa = rsaOaep(null, publicKey);
        return rsa.encryptHex(data, KeyType.PublicKey);
    }

    /**
     * rsa私钥解密
     *
     * @param data       待解密数据(加密数据被BASE64/HEX二次编码)
     * @param privateKey 私钥
     * @return 解密后字符串
     */
    public static String decryptByRsa(String data, String privateKey) {
        if (StrUtil.isBlank(privateKey)) {
            throw new ServiceException("RSA需要传入私钥进行解密");
        }
        RSA rsa = rsaOaep(privateKey, null);
        return rsa.decryptStr(data, KeyType.PrivateKey, StandardCharsets.UTF_8);
    }

    /**
     * rsa私钥解密
     *
     * @param data       待解密数据(加密数据被BASE64/HEX二次编码)
     * @param privateKey 私钥
     * @return 解密后字符串
     */
    public static byte[] decryptByRsaToByte(String data, String privateKey) {
        if (StrUtil.isBlank(privateKey)) {
            throw new ServiceException("RSA需要传入私钥进行解密");
        }
        RSA rsa = rsaOaep(privateKey, null);
        return rsa.decrypt(data, KeyType.PrivateKey);
    }

    /**
     * md5加密
     *
     * @param data 待加密数据
     * @return 加密后字符串, 采用Hex编码
     */
    public static String encryptByMd5(String data) {
        return SecureUtil.md5(data);
    }

    /**
     * sha256加密
     *
     * @param data 待加密数据
     * @return 加密后字符串, 采用Hex编码
     */
    public static String encryptBySha256(String data) {
        return SecureUtil.sha256(data);
    }

    /**
     * sm3加密
     *
     * @param data 待加密数据
     * @return 加密后字符串, 采用Hex编码
     */
    public static String encryptBySm3(String data) {
        return SmUtil.sm3(data);
    }

    /**
     * 获取RSA/ECB/OAEPWithSHA-256AndMGF1Padding类型 RSA
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     * @return RSA实例
     */
    public static RSA rsaOaep(String privateKey, String publicKey) {
        // 指定使用 OAEPWithSHA-256 填充的 RSA 算法
        return new RSA("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", privateKey, publicKey);
    }

    /**
     * 获取GCM模式的AES对称加密方式
     * @param password  秘钥值
     * @param iv        盐值/向量值
     * @return          AES实例
     */
    public static AES aesGcm(byte[] password, byte[] iv) {
        return new AES("GCM", "NoPadding", password, iv);
    }
}
