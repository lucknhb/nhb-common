package com.nhb.common.signature;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import com.nhb.common.core.exception.ServiceException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/9 16:30
 * @description: 验签工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SignatureUtil {
    public static final String PUBLIC_KEY = "publicKey";
    public static final String PRIVATE_KEY = "privateKey";

    /**
     * 生成 RSA 签名算法 秘钥对
     * @return  秘钥对 秘钥对 秘钥对为BASE64编码后的值
     */
    public static Map<String,String> sha256WithRsa() {
        Sign sign = SecureUtil.sign(SignAlgorithm.SHA256withRSA);
        PrivateKey privateKey = sign.getPrivateKey();
        PublicKey publicKey = sign.getPublicKey();
        if (Objects.isNull(privateKey) || Objects.isNull(publicKey)) {
            throw new ServiceException("无法生成签名秘钥对");
        }
        Map<String,String> result = new HashMap<>();
        result.put(PUBLIC_KEY, Base64Encoder.encode(publicKey.getEncoded()));
        result.put(PRIVATE_KEY, Base64Encoder.encode(privateKey.getEncoded()));
        return result;
    }


}
