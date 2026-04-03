package com.nhb.common.dubbo.config;

import com.nhb.common.core.factory.YamlPropertySourceFactory;
import com.nhb.common.dubbo.handler.DubboExceptionHandler;
import com.nhb.common.dubbo.processor.CustomDubboBeanFactoryPostProcessor;
import com.nhb.common.dubbo.properties.DubboCustomProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/27 14:49
 * @description:
 */
@AutoConfiguration
@PropertySource(value = "classpath:dubbo-default.yaml",factory = YamlPropertySourceFactory.class)
@EnableConfigurationProperties(DubboCustomProperties.class)
public class DubboAutoConfiguration {

    @Bean
    public CustomDubboBeanFactoryPostProcessor customDubboBeanFactoryPostProcessor(){
        return new CustomDubboBeanFactoryPostProcessor();
    }

    /**
     * 异常处理器
     */
    @Bean
    public DubboExceptionHandler dubboExceptionHandler() {
        return new DubboExceptionHandler();
    }
}
