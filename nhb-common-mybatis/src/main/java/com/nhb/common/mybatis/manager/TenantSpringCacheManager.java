package com.nhb.common.mybatis.manager;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.nhb.common.core.constant.GlobalConstants;
import com.nhb.common.mybatis.helper.TenantHelper;
import com.nhb.common.redis.manager.SpringCachePlusManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/23 15:47
 * @description: 重写 cacheName 处理方法 支持多租户
 */
@Slf4j
public class TenantSpringCacheManager extends SpringCachePlusManager {

    public TenantSpringCacheManager() {
    }

    @Override
    public Cache getCache(String name) {
        if (InterceptorIgnoreHelper.willIgnoreTenantLine("")) {
            return super.getCache(name);
        }
        if (StringUtils.contains(name, GlobalConstants.GLOBAL_REDIS_KEY)) {
            return super.getCache(name);
        }
        String tenantId = TenantHelper.getTenantId();
        if (StringUtils.isBlank(tenantId)) {
            log.error("Unable To Obtain Valid Tenant Id -> Null");
        }
        if (StringUtils.startsWith(name, tenantId)) {
            // 如果存在则直接返回
            return super.getCache(name);
        }
        return super.getCache(tenantId + ":" + name);
    }


}
