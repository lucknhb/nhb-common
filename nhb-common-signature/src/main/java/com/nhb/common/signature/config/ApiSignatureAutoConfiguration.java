package com.nhb.common.signature.config;

import com.nhb.common.signature.aspectj.ApiSignatureAspect;
import com.nhb.common.signature.core.ApiSignFilter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 17:05
 * @description
 */
@AutoConfiguration
public class ApiSignatureAutoConfiguration {

    @Bean
    public ApiSignatureAspect apiSignatureAspect() {
        return new ApiSignatureAspect();
    }

    @Bean
    public ApiSignFilter apiSignFilter() {
        return new ApiSignFilter();
    }
}
