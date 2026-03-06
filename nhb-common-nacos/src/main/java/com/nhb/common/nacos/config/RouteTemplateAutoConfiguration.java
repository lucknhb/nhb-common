package com.nhb.common.nacos.config;

import com.nhb.common.nacos.listeners.RouteApplicationReadyEventListener;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/6 9:44
 * @description: 路由模板自动配置启动
 */
@AutoConfiguration
public class RouteTemplateAutoConfiguration {

    @Bean
    public RouteApplicationReadyEventListener routeApplicationReadyEventListener(Environment environment) {
        return new  RouteApplicationReadyEventListener(environment);
    }
}
