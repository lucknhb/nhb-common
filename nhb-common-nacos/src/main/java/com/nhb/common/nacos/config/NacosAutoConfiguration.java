package com.nhb.common.nacos.config;

import com.nhb.common.core.factory.YamlPropertySourceFactory;
import com.nhb.common.nacos.condition.NotGatewayCondition;
import com.nhb.common.nacos.listeners.InstanceApplicationReadyEventListener;
import com.nhb.common.nacos.listeners.InstanceContextClosedEventListener;
import com.nhb.common.nacos.listeners.RouteApplicationReadyEventListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/6 9:44
 * @description: 路由模板自动配置启动
 */
@AutoConfiguration
@PropertySource(value = "classpath:nacos-default.yaml",factory = YamlPropertySourceFactory.class)
public class NacosAutoConfiguration {

    @Bean
    @Conditional(NotGatewayCondition.class)
    @ConditionalOnBooleanProperty(value = "spring.cloud.nacos.router.generate-enabled",havingValue = true,matchIfMissing = false)
    public RouteApplicationReadyEventListener routeApplicationReadyEventListener(Environment environment) {
        return new  RouteApplicationReadyEventListener(environment);
    }

    @Bean
    @ConditionalOnBooleanProperty(value = "spring.cloud.nacos.discovery.register-enabled",havingValue = false,matchIfMissing = true)
    public InstanceContextClosedEventListener instanceContextClosedEventListener() {
        return new InstanceContextClosedEventListener();
    }

    @Bean
    @ConditionalOnBooleanProperty(value = "spring.cloud.nacos.discovery.register-enabled",havingValue = false,matchIfMissing = true)
    public InstanceApplicationReadyEventListener instanceApplicationReadyEventListener() {
        return new InstanceApplicationReadyEventListener();
    }
}
