package com.nhb.common.redis.manager;

import com.nhb.common.core.utils.SpringContextUtil;
import org.springframework.cache.Cache;

import java.util.concurrent.Callable;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/26 16:19
 * @description:  本地缓存扩展
 */
public class CaffeineCacheDecorator implements Cache {
    private static final com.github.benmanes.caffeine.cache.Cache<Object, Object>
            CAFFEINE = SpringContextUtil.getBean("caffeine");

    private final String name;
    private final Cache cache;

    public CaffeineCacheDecorator(String name, Cache cache) {
        this.name = name;
        this.cache = cache;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    public String getUniqueKey(Object key) {
        return name + ":" + key;
    }

    @Override
    public Cache.ValueWrapper get(Object key) {
        Object o = CAFFEINE.get(getUniqueKey(key), k -> cache.get(key));
        return (Cache.ValueWrapper) o;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Class<T> type) {
        Object o = CAFFEINE.get(getUniqueKey(key), k -> cache.get(key, type));
        return (T) o;
    }

    @Override
    public void put(Object key, Object value) {
        CAFFEINE.invalidate(getUniqueKey(key));
        cache.put(key, value);
    }

    @Override
    public Cache.ValueWrapper putIfAbsent(Object key, Object value) {
        CAFFEINE.invalidate(getUniqueKey(key));
        return cache.putIfAbsent(key, value);
    }

    @Override
    public void evict(Object key) {
        evictIfPresent(key);
    }

    @Override
    public boolean evictIfPresent(Object key) {
        boolean b = cache.evictIfPresent(key);
        if (b) {
            CAFFEINE.invalidate(getUniqueKey(key));
        }
        return b;
    }

    @Override
    public void clear() {
        CAFFEINE.invalidateAll();
        cache.clear();
    }

    @Override
    public boolean invalidate() {
        return cache.invalidate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object o = CAFFEINE.get(getUniqueKey(key), k -> cache.get(key, valueLoader));
        return (T) o;
    }

}
