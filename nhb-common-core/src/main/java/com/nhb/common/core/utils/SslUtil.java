package com.nhb.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/28 11:40
 * @description: SSL 工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SslUtil {
    /**
     * ssl上下文.
     * @return ssl上下文
     */
    public static SSLContext sslContext() throws NoSuchAlgorithmException, KeyManagementException {
        // X.509是密码学里公钥证书的格式标准，作为证书标准
        X509TrustManager disabledTrustManager = DisableValidationTrustManager.INSTANCE;
        // 信任库
        TrustManager[] trustManagers = new TrustManager[] { disabledTrustManager };
        // 怎么选择加密协议，请看 ProtocolVersion
        // 为什么能找到对应的加密协议 请查看 SSLContextSpi
        SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
        sslContext.init(null, trustManagers, new java.security.SecureRandom());
        return sslContext;
    }

    /**
     * 忽略证书认证
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static void ignoreSSLTrust() throws NoSuchAlgorithmException, KeyManagementException {
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext().getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
    }

    public static class DisableValidationTrustManager implements X509TrustManager {

        public static final X509TrustManager INSTANCE = new DisableValidationTrustManager();

        public DisableValidationTrustManager() {
        }

        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
        }

        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }
}
