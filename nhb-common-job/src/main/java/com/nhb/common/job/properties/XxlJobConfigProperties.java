package com.nhb.common.job.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/9 16:05
 * @description: XXL_JOB配置项
 */
@Data
@ConfigurationProperties(prefix = XxlJobConfigProperties.PREFIX)
public class XxlJobConfigProperties {
    /**
     * 配置前缀
     */
    public static final String PREFIX = "xxl.job";
    /**
     * 是否自动注册执行器<BR/>
     * 默认为 true
     */
    private Boolean enabled = true;
    /**
     * 调度器相关配置
     */
    private XxlJobAdminConfigProperties admin = new XxlJobAdminConfigProperties();
    /**
     * 执行器相关配置
     */
    private XxlJobExecutorConfigProperties executor = new XxlJobExecutorConfigProperties();

}
