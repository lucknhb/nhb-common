package com.nhb.common.dubbo.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/27 14:44
 * @description: dubbo自定义配置项
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = "dubbo.custom")
public class DubboCustomProperties {
    /**
     * 是否打印出调用日志 默认为true
     */
    private boolean logEnabled = true;
}
