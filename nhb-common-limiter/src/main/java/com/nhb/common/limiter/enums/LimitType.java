package com.nhb.common.limiter.enums;

import cn.hutool.core.util.StrUtil;
import com.nhb.common.core.utils.ServletUtil;
import com.nhb.common.redis.utils.RedissonUtil;

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
        public String resolve() {
            return StrUtil.EMPTY;
        }
    },

    /**
     * 根据请求者IP进行限流
     */
    IP {
        @Override
        public String resolve() {
            return ServletUtil.getClientIP();
        }
    },

    /**
     * 实例限流(集群多后端实例)
     */
    CLUSTER {
        @Override
        public String resolve() {
            return RedissonUtil.getClient().getId();
        }
    };

    public abstract String resolve();
}
