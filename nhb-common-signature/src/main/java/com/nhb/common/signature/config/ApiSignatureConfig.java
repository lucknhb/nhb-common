package com.nhb.common.signature.config;

import com.nhb.common.signature.aop.ApiSignatureAop;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 17:05
 * @description
 */
@AutoConfiguration
public class ApiSignatureConfig {

    @Bean
    public ApiSignatureAop apiSignatureAop() {
        return new ApiSignatureAop();
    }
}
