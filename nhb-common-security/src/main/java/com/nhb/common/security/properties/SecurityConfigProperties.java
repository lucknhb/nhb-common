package com.nhb.common.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 16:27
 * @description: 白名单 既不需要进行登录直接可访问
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = SecurityConfigProperties.PREFIX)
public class SecurityConfigProperties {
    public static final String PREFIX = "security.ignore";
    /**
     * 放行白名单配置，网关不校验此处的白名单
     */
    private List<String> paths = new ArrayList<>();
}
