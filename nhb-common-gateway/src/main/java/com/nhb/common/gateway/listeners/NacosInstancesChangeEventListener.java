package com.nhb.common.gateway.listeners;

import com.alibaba.nacos.client.naming.event.InstancesChangeEvent;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.notify.listener.Subscriber;
import com.alibaba.nacos.common.utils.JacksonUtils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cloud.loadbalancer.cache.LoadBalancerCacheManager;
import org.springframework.cloud.loadbalancer.core.CachingServiceInstanceListSupplier;


/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/18 16:00
 * @description: 通过Nacos实现平滑上下线 补缺网关本地缓存实例信息更新不及时问题
 */
@Slf4j
public class NacosInstancesChangeEventListener extends Subscriber<InstancesChangeEvent> {
    @Resource
    private LoadBalancerCacheManager caffeineLoadBalancerCacheManager;

    @PostConstruct
    public void init() {
        // 注册当前自定义的订阅者以获取通知
        NotifyCenter.registerSubscriber(this);
    }

    /**
     * 事件监听处理
     *
     * @param event {@link Event}
     */
    @Override
    public void onEvent(InstancesChangeEvent event) {
        log.info("Spring Gateway Receive Service Instance Reflush Event ：{}, Start Reflush.", JacksonUtils.toJson(event));
        Cache cache = caffeineLoadBalancerCacheManager.getCache(CachingServiceInstanceListSupplier.SERVICE_INSTANCE_CACHE_NAME);
        if (cache != null) {
            cache.evict(event.getServiceName());
        }
        log.info("Spring Gateway Service Instance Reflush Finish");
    }


    @Override
    public Class<? extends Event> subscribeType() {
        return InstancesChangeEvent.class;
    }
}
