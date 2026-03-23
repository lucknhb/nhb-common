package com.nhb.common.mybatis.handler;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.nhb.common.core.constant.GlobalConstants;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.mybatis.helper.TenantHelper;
import com.nhb.common.redis.handler.KeyPrefixHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/23 16:00
 * @description: 多租户redis缓存key前缀处理
 */
@Slf4j
public class TenantKeyPrefixHandler extends KeyPrefixHandler {

    public TenantKeyPrefixHandler(String keyPrefix) {
        super(keyPrefix);
    }

    /**
     * 增加前缀
     */
    @Override
    public String map(String name) {
        if (StringUtil.isBlank(name)) {
            return null;
        }
        try {
            if (InterceptorIgnoreHelper.willIgnoreTenantLine("")) {
                return super.map(name);
            }
        } catch (NoClassDefFoundError ignore) {
            // 有些服务不需要mp导致类不存在 忽略即可
        }
        if (StringUtil.contains(name, GlobalConstants.GLOBAL_REDIS_KEY)) {
            return super.map(name);
        }
        String tenantId = TenantHelper.getTenantId();
        if (StringUtil.isBlank(tenantId)) {
            log.warn("Unable To Obtain Valid Tenant Id -> Null");
            return super.map(name);
        }
        if (StringUtil.startsWith(name, tenantId)) {
            // 如果存在则直接返回
            return super.map(name);
        }
        return super.map(tenantId + ":" + name);
    }

    /**
     * 去除前缀
     */
    @Override
    public String unmap(String name) {
        String unmap = super.unmap(name);
        if (StringUtil.isBlank(unmap)) {
            return null;
        }
        try {
            if (InterceptorIgnoreHelper.willIgnoreTenantLine("")) {
                return unmap;
            }
        } catch (NoClassDefFoundError ignore) {
            // 有些服务不需要mp导致类不存在 忽略即可
        }
        if (StringUtil.contains(name, GlobalConstants.GLOBAL_REDIS_KEY)) {
            return unmap;
        }
        String tenantId = TenantHelper.getTenantId();
        if (StringUtil.isBlank(tenantId)) {
            log.warn("Unable To Obtain Valid Tenant Id");
            return unmap;
        }
        if (StringUtil.startsWith(unmap, tenantId)) {
            // 如果存在则删除
            return unmap.substring((tenantId + ":").length());
        }
        return unmap;
    }
}
