package com.nhb.common.gateway.config;

import com.nhb.common.gateway.filter.ForwardAuthFilter;
import com.nhb.common.gateway.filter.WebCorsFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 16:29
 * @description:
 */
@AutoConfiguration
public class GatewayAutoConfiguration {

    @Bean
    public WebCorsFilter webCorsFilter() {
        return new WebCorsFilter();
    }

    @Bean
    public ForwardAuthFilter forwardAuthFilter(){
        return new ForwardAuthFilter();
    }


}
