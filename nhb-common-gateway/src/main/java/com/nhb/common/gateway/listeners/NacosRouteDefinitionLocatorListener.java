package com.nhb.common.gateway.listeners;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.gateway.properties.NacosRouteConfigProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.util.DigestUtils;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/6 10:20
 * @description: 通过NACOS实现自动路由加载
 */
@Slf4j
@RequiredArgsConstructor
public class NacosRouteDefinitionLocatorListener implements RouteDefinitionLocator {
    private final NacosConfigManager nacosConfigManager;
    private final NacosRouteConfigProperties nacosRouteConfigProperties;

    /**
     * 具体路由信息
     */
    private volatile List<RouteDefinition> routeDefinitions;

    /**
     * 通过MD5判断是否有东西更新
     */
    private volatile String lastMd5 = "";

    @PostConstruct
    public void init() throws Exception {
        loadRoutes();
        addListener();
    }

    /**
     * 从 Nacos 加载路由配置
     */
    private void loadRoutes() throws Exception {
        ConfigService configService = nacosConfigManager.getConfigService();
        String group = nacosConfigManager.getNacosConfigProperties().getGroup();
        String config = configService.getConfig(nacosRouteConfigProperties.getDataId(), group, 5000);
        if (StrUtil.isBlank(config)) {
            routeDefinitions = new ArrayList<>();
            log.warn("No route configuration found in Nacos");
        } else {
            lastMd5 = DigestUtils.md5DigestAsHex(config.getBytes());
            routeDefinitions = JacksonUtil.toList(config, RouteDefinition.class);
            log.info("Loaded {} routes from Nacos", routeDefinitions.size());
        }
    }

    /**
     * 添加 Nacos 监听器，配置变化时刷新路由
     */
    private void addListener() throws Exception {
        ConfigService configService = nacosConfigManager.getConfigService();
        String group = nacosConfigManager.getNacosConfigProperties().getGroup();
        configService.addListener(nacosRouteConfigProperties.getDataId(), group, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                String currentMd5 = DigestUtils.md5DigestAsHex(configInfo.getBytes());
                //不相等时 则进行更新
                if (lastMd5.equals(currentMd5)) {
                    return;
                }
                log.info("Nacos route configuration changed, reloading...");
                try {
                    routeDefinitions = JacksonUtil.toList(configInfo, RouteDefinition.class);
                    //解析成功更新 MD5
                    lastMd5 = currentMd5;
                    // 发布事件刷新路由
                    SpringContextUtil.publishEvent(new RefreshRoutesEvent(this));
                } catch (Exception e) {
                    log.error("Failed to parse route config", e);
                }
            }
        });
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routeDefinitions);
    }

}
