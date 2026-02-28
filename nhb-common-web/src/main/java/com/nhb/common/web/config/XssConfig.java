package com.nhb.common.web.config;

import com.nhb.common.web.filter.XssFilter;
import com.nhb.common.web.properties.XssProperties;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/28 16:11
 * @description: xss配置
 */
@AutoConfiguration
@EnableConfigurationProperties(XssProperties.class)
@ConditionalOnBooleanProperty(prefix = XssProperties.PREFIX, value = "enabled", havingValue = true)
public class XssConfig {

    @Bean
    @FilterRegistration(
            name = "xssFilter",
            urlPatterns = "/*",
            order = FilterRegistrationBean.HIGHEST_PRECEDENCE + 1,
            dispatcherTypes = DispatcherType.REQUEST
    )
    public XssFilter xssFilter() {
        return new XssFilter();
    }
}
