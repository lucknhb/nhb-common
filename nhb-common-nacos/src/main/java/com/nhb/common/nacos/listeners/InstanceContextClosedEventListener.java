package com.nhb.common.nacos.listeners;

import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/19 11:19
 * @description: 在应用关闭前，从 Nacos 注销当前实例，避免新流量进入
 */
@Slf4j
public class InstanceContextClosedEventListener implements ApplicationListener<ContextClosedEvent> {
    @Resource
    private NacosServiceRegistry nacosServiceRegistry;
    @Resource
    private NacosRegistration nacosRegistration;

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            // 主动从 Nacos 注销实例
            nacosServiceRegistry.deregister(nacosRegistration);
            log.info("Instance Deregistered From Nacos Successfully");
        } catch (Exception e) {
            log.error("Deregister Nacos Instance Failed: {}", e.getMessage(), e);
        }
    }
}
