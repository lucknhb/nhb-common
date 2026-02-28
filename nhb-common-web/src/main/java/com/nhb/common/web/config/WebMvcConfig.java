package com.nhb.common.web.config;

import com.nhb.common.web.handler.GlobalExceptionHandler;
import com.nhb.common.web.handler.ResponseBodyHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.TimeZone;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/22 10:48
 * @description web共性配置
 */
@Slf4j
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class WebMvcConfig {

    /**
     * 设置中国时区
     * @return 设置时区后的配置项
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }

    /**
     * 请求响应拦截处理器
     */
    @Bean
    public ResponseBodyHandler responseBodyHandler() {
        return new ResponseBodyHandler();
    }

    /**
     * 全局异常拦截处理
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * 跨域配置
     * @return 配置项
     */
    @Bean
    @ConditionalOnMissingBean(CorsFilter.class)
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许所有域名跨域
        config.addAllowedOriginPattern(CorsConfiguration.ALL);
        // 允许证书
        config.setAllowCredentials(true);
        // 允许所有方法
        config.addAllowedMethod(CorsConfiguration.ALL);
        // 允许任何头
        config.addAllowedHeader(CorsConfiguration.ALL);
        // 每一个小时，异步请求都发起预检请求 => 发送两次请求 第一次OPTION 第二次GET/POT/PUT/DELETE
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", config);
        log.info("{} load cors end", Thread.currentThread().getName());
        return new CorsFilter(configurationSource);
    }
}
