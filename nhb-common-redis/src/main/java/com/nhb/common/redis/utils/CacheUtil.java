package com.nhb.common.redis.utils;

import com.nhb.common.core.utils.SpringContextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/26 16:56
 * @description: 缓存工具
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CacheUtil {
    private static final CacheManager CACHE_MANAGER = SpringContextUtil.getBean(CacheManager.class);

    /**
     * 获取缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存key
     */
    public static <T> T get(String cacheNames, Object key) {
        Cache.ValueWrapper wrapper = Objects.requireNonNull(CACHE_MANAGER.getCache(cacheNames)).get(key);
        return wrapper != null ? (T) wrapper.get() : null;
    }

    /**
     * 保存缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存key
     * @param value      缓存值
     */
    public static void put(String cacheNames, Object key, Object value) {
        Objects.requireNonNull(CACHE_MANAGER.getCache(cacheNames)).put(key, value);
    }

    /**
     * 删除缓存值
     *
     * @param cacheNames 缓存组名称
     * @param key        缓存key
     */
    public static void evict(String cacheNames, Object key) {
        Objects.requireNonNull(CACHE_MANAGER.getCache(cacheNames)).evict(key);
    }

    /**
     * 清空缓存值
     *
     * @param cacheNames 缓存组名称
     */
    public static void clear(String cacheNames) {
        Objects.requireNonNull(CACHE_MANAGER.getCache(cacheNames)).clear();
    }
}
