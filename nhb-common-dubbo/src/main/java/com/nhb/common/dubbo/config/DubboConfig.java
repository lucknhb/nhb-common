package com.nhb.common.dubbo.config;

import com.nhb.common.dubbo.handler.DubboExceptionHandler;
import com.nhb.common.dubbo.properties.DubboCustomProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/27 14:49
 * @description:
 */
@AutoConfiguration
@EnableConfigurationProperties(DubboCustomProperties.class)
public class DubboConfig {

    /**
     * 异常处理器
     */
    @Bean
    public DubboExceptionHandler dubboExceptionHandler() {
        return new DubboExceptionHandler();
    }
}
