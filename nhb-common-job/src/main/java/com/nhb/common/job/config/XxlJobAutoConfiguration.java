package com.nhb.common.job.config;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhb.common.job.properties.XxlJobAdminConfigProperties;
import com.nhb.common.job.properties.XxlJobConfigProperties;
import com.nhb.common.job.properties.XxlJobExecutorConfigProperties;
import com.nhb.common.job.runner.XxlJobExecutorAutoSaveRunner;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 14:48
 * @description:
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(XxlJobConfigProperties.class)
@ConditionalOnProperty(prefix = XxlJobConfigProperties.PREFIX, value = "enabled", havingValue = "true", matchIfMissing = true)
public class XxlJobAutoConfiguration {
    /**
     * SPRING 环境下注册该模块的执行器 并非 会直接在调度中心执行器管理中手动保存操作
     * @param xxlJobConfigProperties        配置文件
     * @param environment                   环境信息
     * @param inetUtils                     网络工具
     * @return                              执行器
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor(XxlJobConfigProperties xxlJobConfigProperties,
                                               Environment environment,
                                               InetUtils inetUtils) {
        log.debug(">>>>>>>>>>> XXL-JOB CLIENT CONFIG INIT FOR SPRING <<<<<<<<<<<");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        //调度中心相关配置
        XxlJobAdminConfigProperties xxlJobAdminProperties = xxlJobConfigProperties.getAdmin();
        String addresses = xxlJobAdminProperties.getAddress();
        Assert.notBlank(addresses,"ERROR: XXL_JOB ADDRESS IS EMPTY! PLEASE SETTING THE xxl.job.admin.addresses!");
        //去除空格
        xxlJobSpringExecutor.setAdminAddresses(addresses.trim());
        String accessToken = xxlJobAdminProperties.getAccessToken();
        Assert.notBlank(accessToken,"ERROR: XXL_JOB ACCESS_TOKEN IS EMPTY! PLEASE SETTING THE xxl.job.admin.accessToken OR xxl.job.admin.access-token!");
        xxlJobSpringExecutor.setAccessToken(accessToken);
        //执行器相关配置
        XxlJobExecutorConfigProperties xxlJobExecutorProperties = xxlJobConfigProperties.getExecutor();
        //注册的执行器名称 当没有手动配置时 则使用spring配置中的applicationName+环境类型来注册
        String appName = xxlJobExecutorProperties.getAppName();
        if (StrUtil.isBlank(appName)) {
            appName = environment.getProperty("spring.application.name", "");
            String profileType = environment.getProperty("spring.profiles.active", "");
            appName = StrUtil.isNotBlank(profileType) ? appName + "_" + profileType : appName;
        }
        Assert.notBlank(appName,"ERROR:XXL_JOB APP NAME IS EMPTY! PLEASE SETTING THE xxl.job.executor.appName OR xxl.job.executor.app-name!");
        xxlJobSpringExecutor.setAppname(appName);
        xxlJobExecutorProperties.setAppName(appName);
        xxlJobSpringExecutor.setAddress(xxlJobExecutorProperties.getAddress());
        log.info(">>>>>>>>>>>>> XXL_JOB EXECUTOR NAME IS [{}] <<<<<<<<<<<<", appName);
        String ip = xxlJobExecutorProperties.getIp();
        if (StrUtil.isBlank(ip)) {
            ip = inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
        }
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(xxlJobExecutorProperties.getPort());
        String logPath = xxlJobExecutorProperties.getLogPath();
        if (StrUtil.isBlank(logPath)) {
            logPath = environment.getProperty("LOGGING_PATH", "logs")
                    .concat("/").concat(appName).concat("/jobs");
        }
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(xxlJobExecutorProperties.getLogRetentionDays());
        return xxlJobSpringExecutor;
    }

    /**
     * 自动创建调度中心上的配置
     * @param xxlJobConfigProperties 配置文件
     * @return   自动注册及设置应用信息
     */
    @Bean
    @ConditionalOnExpression("!'${xxl.job.admin.user-name:}'.isEmpty() && !'${xxl.job.admin.password:}'.isEmpty()")
    public XxlJobExecutorAutoSaveRunner xxlJobExecutorAutoSaveRunner(XxlJobConfigProperties xxlJobConfigProperties,
                                                                     ObjectMapper objectMapper) {
        return new XxlJobExecutorAutoSaveRunner(xxlJobConfigProperties,objectMapper);
    }

}
