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
@ConfigurationProperties(prefix = DubboCustomProperties.PREFIX)
public class DubboCustomProperties {
    public static final String PREFIX = "dubbo.custom";
    /**
     * 是否打印出调用日志 默认为true
     */
    private Boolean logEnabled = true;
    /**
     * 是否设置项目名称 可在Nacos 订阅列表中 应用名  默认开启
     */
    private Boolean projectNameEnabled = true;
    /**
     * 全局异常处理是否开启
     */
    private Boolean globalErrorEnabled = true;
    /**
     * 统一返回指定异常信息
     */
    private String failMessage = "服务处理异常,请联系管理员";
}
