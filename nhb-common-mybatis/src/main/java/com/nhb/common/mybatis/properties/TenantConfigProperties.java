package com.nhb.common.mybatis.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/23 15:42
 * @description:
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = TenantConfigProperties.PREFIX)
public class TenantConfigProperties {
    public static final String PREFIX = "mybatis-plus.tenant";
    /**
     * 是否启用
     */
    private Boolean enabled = false;

    /**
     * 排除表名
     */
    private List<String> excludeTables;

}
