package com.nhb.common.gateway.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.nhb.common.gateway.filter.ForwardAuthFilter;
import com.nhb.common.gateway.filter.WebCorsFilter;
import com.nhb.common.gateway.filter.WebI18nFilter;
import com.nhb.common.gateway.handler.GatewayExceptionHandler;
import com.nhb.common.gateway.listeners.NacosInstancesChangeEventListener;
import com.nhb.common.gateway.listeners.NacosRouteDefinitionLocatorListener;
import com.nhb.common.gateway.properties.NacosRouteConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 16:29
 * @description:
 */
@AutoConfiguration
@EnableConfigurationProperties(NacosRouteConfigProperties.class)
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
    public GatewayExceptionHandler gatewayExceptionHandler() {
        return new GatewayExceptionHandler();
    }

    //TODO 可测试不配置的话 下游服务是否能获取到 以及当前环境是否自动获取
    @Bean
    public WebI18nFilter webI18nFilter() {
        return new WebI18nFilter();
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

}
