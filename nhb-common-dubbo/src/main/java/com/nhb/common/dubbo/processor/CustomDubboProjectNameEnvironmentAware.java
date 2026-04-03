package com.nhb.common.dubbo.processor;

import com.nhb.common.core.utils.StringUtil;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/3 17:38
 * @description:  设置dubbo的项目名称 可在Nacos 服务管理-订阅者列表中 应用名 体现
 */
public class CustomDubboProjectNameEnvironmentAware implements EnvironmentAware {

    @Override
    public void setEnvironment(Environment environment) {
        // 如果系统属性 project.name 未设置，则使用 spring.application.name 的值
        if (StringUtil.isBlank(System.getProperty("project.name"))) {
            String applicationName = environment.getProperty("spring.application.name");
            System.setProperty("project.name", applicationName);
        }
    }
}
