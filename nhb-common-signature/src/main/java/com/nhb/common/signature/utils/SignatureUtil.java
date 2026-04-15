package com.nhb.common.signature.utils;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.google.common.collect.Maps;
import com.nhb.common.core.exception.ServiceException;
import com.nhb.common.core.utils.ConvertUtil;
import com.nhb.common.core.utils.ObjectSelfUtil;
import com.nhb.common.core.utils.StringUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/9 16:30
 * @description: 验签工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SignatureUtil {
    /**
     * 随机字符
     */
    public static final String NONCE = "nonce";
    /**
     * 签名  SHA256withRSA
     */
    public static final String SIGN = "sign";
    /**
     * 时间戳
     */
    public static final String TIMESTAMP = "timestamp";
    /**
     * 用户标识
     */
    public static final String CLIENT_ID = "clientId";
    /**
     * 公钥
     */
    public static final String PUBLIC_KEY = "publicKey";
    /**
     * 秘钥
     */
    public static final String PRIVATE_KEY = "privateKey";
    /**
     * &符
     */
    public static final String AND = "&";
    /**
     * =符
     */
    public static final String EQUALS = "=";

    /**
     * 非对称加密<BR/>
     * 生成 RSA/ECB/PKCS1Padding RSA 算法 秘钥对
     *
     * @return 秘钥对 值为BASE64编码后的值
     */
    public static Map<String, String> generateRsa() {
        //"RSA/ECB/PKCS1Padding"
        RSA rsa = SecureUtil.rsa();
        PrivateKey privateKey = rsa.getPrivateKey();
        PublicKey publicKey = rsa.getPublicKey();
        if (Objects.isNull(privateKey) || Objects.isNull(publicKey)) {
            throw new ServiceException("无法生成签名秘钥对");
        }
        Map<String, String> result = new HashMap<>();
        result.put(PUBLIC_KEY, Base64Encoder.encode(publicKey.getEncoded()));
        result.put(PRIVATE_KEY, Base64Encoder.encode(privateKey.getEncoded()));
        return result;
    }


    /**
     * 参数排序规则：所有参与签名的参数，必须按照参数名的ASCII码升序排列，确保客户端和服务端的拼接顺序完全一致。
     * 空值排除规则：参数值为null、空字符串的参数，必须排除在签名计算之外，避免因空值处理不一致导致的签名失败。
     * 编码统一规则：所有参数必须使用UTF-8编码，包括参数名、参数值、拼接字符串，避免中文、特殊字符导致的签名不一致。
     * 核心参数强制参与规则：appId、timestamp、nonce这三个核心参数，必须强制参与签名，防止攻击者篡改这些核心校验字段。
     * 拼接格式规范：必须使用key1=value1&key2=value2的格式拼接参数，禁止使用无分隔符的拼接，避免参数注入导致的签名绕过。
     *
     * @param map 构建参数
     * @return 生成的签名信息
     */
    public static String buildSignContent(Map<String, Object> map) {
        if (CollUtil.isEmpty(map)) {
            return StrUtil.EMPTY;
        }
        StringBuilder signContent = new StringBuilder();
        //键按照自然顺序
        TreeMap<String, Object> sortMap = Maps.newTreeMap();
        sortMap.putAll(map);
        for (Map.Entry<String, Object> entry : sortMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            //排除空值
            if (ObjectSelfUtil.isNull(value) || StringUtil.isBlank(ConvertUtil.toStr(value))) {
                continue;
            }
            String data = ConvertUtil.toStr(value);
            if (!signContent.isEmpty()) {
                signContent.append(AND);
            }
            signContent.append(key).append(EQUALS).append(data);
        }
        return signContent.toString();
    }

    /**
     * 使用私钥签名
     *
     * @param content    签名内容
     * @param privateKey 私钥
     * @return
     */
    public static String generateSignature(String content, String privateKey) {
        Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA, privateKey, null);
        byte[] signByte = sign.sign(content);
        return Base64Encoder.encode(signByte);
    }

    /**
     * 使用公钥验证签名
     *
     * @param originContent 未签名过BASE64的数据
     * @param signContent   签名后的BASE64数据
     * @param publicKey     公钥
     * @return
     */
    public static boolean verifySignature(String originContent, String signContent, String publicKey) {
        Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA, null, publicKey);
        return sign.verify(originContent.getBytes(StandardCharsets.UTF_8), Base64Decoder.decode(signContent));
    }


}
