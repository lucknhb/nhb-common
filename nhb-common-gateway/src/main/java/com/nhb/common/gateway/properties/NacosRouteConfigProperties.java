package com.nhb.common.gateway.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/6 13:37
 * @description:
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = NacosRouteConfigProperties.PREFIX)
public class NacosRouteConfigProperties {
    public static final String PREFIX = "spring.cloud.gateway.route";
    /**
     * 动态路由所配置的dataId
     */
    private String dataId = "router.json";
}
