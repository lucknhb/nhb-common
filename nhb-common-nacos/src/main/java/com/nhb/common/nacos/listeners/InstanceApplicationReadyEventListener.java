package com.nhb.common.nacos.listeners;

import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/19 11:28
 * @description: 禁用自动注册实例 并且在ApplicationReadyEvent事件后手动注册
 */
@Slf4j
public class InstanceApplicationReadyEventListener implements ApplicationListener<ApplicationReadyEvent> {
    @Resource
    private NacosServiceRegistry nacosServiceRegistry;
    @Resource
    private NacosRegistration nacosRegistration;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            // 应用完全就绪后注册
            if (nacosRegistration.getPort() < 0){
                String port = event.getApplicationContext().getEnvironment().getProperty("local.server.port");
                nacosRegistration.setPort(Integer.parseInt(port));
            }
            nacosServiceRegistry.register(nacosRegistration);
            log.info("Nacos Instance Registered Successfully After Application Ready.");
        } catch (Exception e) {
            log.error("Register Nacos Instance Failed:{} ", e.getMessage(), e);
        }
    }
}
