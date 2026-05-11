package com.nhb.common.websocket.ssl;

import cn.hutool.core.text.StrPool;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.websocket.properties.WebSocketConfigProperties;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/11 8:41
 * @description: SSL服务
 */
@Slf4j
public class SslContextFactory {
    private static SslContext sslContext = null;

    /**
     * 创建SSL上下文
     *
     * @param webSocketConfigProperties 配置文件
     * @param resourceLoader            加载器
     * @return SSL上下文
     * @throws Exception 异常信息
     */
    public static SslContext create(WebSocketConfigProperties webSocketConfigProperties,
                                    ResourceLoader resourceLoader) throws Exception {
        WebSocketConfigProperties.Ssl ssl = webSocketConfigProperties.getSsl();
        if (!Boolean.TRUE.equals(ssl.getEnabled())) {
            return null;
        }

        // ========== 1. 优先使用外部指定的证书 ==========
        if (StringUtil.isNotBlank(ssl.getCertFilePath()) && StringUtil.isNotBlank(ssl.getKeyFilePath())) {
            return createFromFiles(webSocketConfigProperties, resourceLoader);
        }

        // ========== 2. 自动生成自签名证书 ==========
        if (Boolean.TRUE.equals(ssl.getEnabled())) {
            log.warn("Automatically Generate Self-Signed Certificates For Dev/Test Environments Only, Do Not Use Them In Production Environments!");
            return createFromGenerated(webSocketConfigProperties);
        }

        throw new IllegalStateException(
                "WSS Enabled, But The Certificate File Is Not Specified, And [websocket.ssl.auto-generate-enabled] Is Not Enabled");
    }

    /**
     * 外部证书
     *
     * @param webSocketConfigProperties 配置项
     * @param resourceLoader            加载器
     * @return SSL上下文
     * @throws Exception 异常消息
     */
    private static SslContext createFromFiles(WebSocketConfigProperties webSocketConfigProperties,
                                              ResourceLoader resourceLoader) throws Exception {
        WebSocketConfigProperties.Ssl ssl = webSocketConfigProperties.getSsl();
        Resource certResource = resourceLoader.getResource(ssl.getCertFilePath());
        Resource keyResource = resourceLoader.getResource(ssl.getKeyFilePath());
        try (InputStream certIs = certResource.getInputStream();
             InputStream keyIs = keyResource.getInputStream()) {
            SslContextBuilder builder = SslContextBuilder.forServer(certIs, keyIs, ssl.getKeyPassword());
            applySettings(builder, ssl);
            return builder.build();
        }
    }

    /**
     * 自动生成证书
     *
     * @param webSocketConfigProperties 配置文件
     * @return SSL上下文
     * @throws Exception 异常信息
     */
    private static SslContext createFromGenerated(WebSocketConfigProperties webSocketConfigProperties) throws Exception {
        //重复使用已存在的证书信息
        if (Objects.nonNull(sslContext)) {
            return sslContext;
        }
        WebSocketConfigProperties.Ssl ssl = webSocketConfigProperties.getSsl();
        Assert.hasLength(ssl.getGenerateCertDomain(), "SSL Certificate Domain Is Not Supported");
        // 使用临时目录（应用重启后自动删除）
        SelfSignedCertificate ssc = new SelfSignedCertificate(ssl.getGenerateCertDomain());
        SslContextBuilder builder = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey());
        applySettings(builder, ssl);
        //用于重复使用已创建好的SSL上下文
        sslContext = builder.build();
        return sslContext;
    }

    /**
     * 公共设置
     *
     * @param builder 上下文构造器
     * @param ssl     ssl配置项
     */
    private static void applySettings(SslContextBuilder builder, WebSocketConfigProperties.Ssl ssl) {
        // SSL 提供者
        String provider = ssl.getProvider();
        if (SslProvider.OPENSSL.name().equalsIgnoreCase(provider)) {
            builder.sslProvider(SslProvider.OPENSSL);
        } else {
            builder.sslProvider(SslProvider.JDK);
        }
        // 协议版本
        if (StringUtil.isNotBlank(ssl.getProtocols())) {
            builder.protocols(ssl.getProtocols().split(StrPool.COMMA));
        }
    }
}
