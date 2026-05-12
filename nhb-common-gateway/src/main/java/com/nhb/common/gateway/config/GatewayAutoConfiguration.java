package com.nhb.common.gateway.config;

import cn.hutool.core.text.StrPool;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.nhb.common.core.factory.YamlPropertySourceFactory;
import com.nhb.common.gateway.filter.ForwardAuthFilter;
import com.nhb.common.gateway.filter.WebCorsFilter;
import com.nhb.common.gateway.filter.WebFluxCacheRequestFilter;
import com.nhb.common.gateway.handler.GatewayExceptionHandler;
import com.nhb.common.gateway.listeners.NacosInstancesChangeEventListener;
import com.nhb.common.gateway.listeners.NacosRouteDefinitionLocatorListener;
import com.nhb.common.gateway.properties.GatewayConfigProperties;
import com.nhb.common.gateway.properties.NacosRouteConfigProperties;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.server.Ssl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.Assert;

import javax.net.ssl.SSLException;
import java.security.cert.CertificateException;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 16:29
 * @description:
 */
@Slf4j
@AutoConfiguration
@PropertySource(value = "classpath:gateway-default.yaml", factory = YamlPropertySourceFactory.class)
@EnableConfigurationProperties({NacosRouteConfigProperties.class, GatewayConfigProperties.class})
public class GatewayAutoConfiguration {

    @Bean
    public WebCorsFilter webCorsFilter() {
        return new WebCorsFilter();
    }

    @Bean
    public ForwardAuthFilter forwardAuthFilter() {
        return new ForwardAuthFilter();
    }

    @Bean
    public WebFluxCacheRequestFilter webFluxCacheRequestFilter(){
        return new WebFluxCacheRequestFilter();
    }

    @Bean
    public GatewayExceptionHandler gatewayExceptionHandler() {
        return new GatewayExceptionHandler();
    }

    @Bean
    public NacosRouteDefinitionLocatorListener nacosRouteDefinitionLocatorListener(NacosConfigManager nacosConfigManager,
                                                                                   NacosRouteConfigProperties nacosRouteConfigProperties) {
        return new NacosRouteDefinitionLocatorListener(nacosConfigManager, nacosRouteConfigProperties);
    }

    @Bean
    public NacosInstancesChangeEventListener  nacosInstancesChangeEventListener() {
        return new NacosInstancesChangeEventListener();
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cloud.gateway.ssl.auto-generate-cert-enabled", havingValue = "true")
    public NettyServerCustomizer sslCustomizer(ServerProperties serverProperties,
                                                      GatewayConfigProperties gatewayConfigProperties) {
        Ssl ssl = serverProperties.getSsl();
        //SSL 必须开启
        Assert.isTrue(Objects.isNull(ssl) || !ssl.isEnabled(),"Please Setting [server.ssl.enabled=false] First");
        return httpServer -> {
            try {
                GatewayConfigProperties.Ssl gatewayConfigPropertiesSsl = gatewayConfigProperties.getSsl();
                Assert.hasLength(gatewayConfigPropertiesSsl.getGenerateCertDomain(), "SSL Certificate Domain Is Not Supported");
                SelfSignedCertificate ssc = new SelfSignedCertificate(gatewayConfigPropertiesSsl.getGenerateCertDomain());
                SslContextBuilder builder = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey());
                //使用传入的协议
                builder.protocols(gatewayConfigPropertiesSsl.getProtocols().split(StrPool.COMMA));
                //默认使用OPENSSL
                builder.sslProvider(SslProvider.OPENSSL);
                SslContext sslContext = builder.build();
                log.warn("Automatically Generate Self-Signed Certificates For Dev/Test Environments Only, Do Not Use Them In Production Environments!");
                return httpServer.secure(spec -> spec.sslContext(sslContext));
            } catch (CertificateException | SSLException e) {
                throw new RuntimeException("Failed to setup self-signed SSL context for Gateway", e);
            }
        };
    }

}
