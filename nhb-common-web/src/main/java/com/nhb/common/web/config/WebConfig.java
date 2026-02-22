package com.nhb.common.web.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/22 10:48
 * @description web共性配置
 */
@Configuration
public class WebConfig {

    /**
     * 设置中国时区
     * @return 设置时区后的配置项
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.timeZone(TimeZone.getTimeZone("Asia/Shanghai"));
    }
}
