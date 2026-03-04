package com.nhb.common.limiter.enums;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.nhb.common.redis.utils.RedissonUtil;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 10:26
 * @description: 限流类型
 */
public enum LimitType {
    /**
     * 默认策略全局限流
     */
    DEFAULT {
        @Override
        public String resolve(HttpServletRequest request) {
            return StrUtil.EMPTY;
        }
    },

    /**
     * 根据请求者IP进行限流
     */
    IP {
        @Override
        public String resolve(HttpServletRequest request) {
            return JakartaServletUtil.getClientIP(request);
        }
    },

    /**
     * 实例限流(集群多后端实例)
     */
    CLUSTER {
        @Override
        public String resolve(HttpServletRequest request) {
            return RedissonUtil.getClient().getId();
        }
    };

    public abstract String resolve(HttpServletRequest request);
}
