package com.nhb.common.encrypt.config;

import com.nhb.common.core.factory.YamlPropertySourceFactory;
import com.nhb.common.encrypt.filter.EncryptFilter;
import com.nhb.common.encrypt.listener.ApiEncryptCheckListener;
import com.nhb.common.encrypt.properties.ApiEncryptProperties;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 11:23
 * @description: 接口加解密自动配置
 */
@AutoConfiguration
@EnableConfigurationProperties(ApiEncryptProperties.class)
@PropertySource(value = "classpath:encrypt-default.yaml",factory = YamlPropertySourceFactory.class)
public class ApiEncryptAutoConfiguration {

    @Bean
    @FilterRegistration(
            name = "encryptFilter",
            urlPatterns = "/*",
            order = FilterRegistrationBean.HIGHEST_PRECEDENCE,
            dispatcherTypes = DispatcherType.REQUEST
    )
    public EncryptFilter cryptoFilter(ApiEncryptProperties properties) {
        return new EncryptFilter(properties);
    }

    @Bean
    public ApiEncryptCheckListener apiEncryptCheckListener(){
        return new ApiEncryptCheckListener();
    }


}
