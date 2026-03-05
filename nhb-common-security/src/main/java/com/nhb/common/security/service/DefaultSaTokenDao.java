package com.nhb.common.security.service;

import cn.dev33.satoken.dao.auto.SaTokenDaoBySessionFollowObject;
import cn.dev33.satoken.util.SaFoxUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.nhb.common.redis.utils.RedissonUtil;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/5 10:22
 * @description:
 * Sa-Token持久层接口(使用框架自带RedissonUtil实现 协议统一)
 * <p>
 * 采用 caffeine + redis 多级缓存 优化并发查询效率
 * <p>
 * SaTokenDaoBySessionFollowObject 是 SaTokenDao 子集简化了session方法处理
 */
public class DefaultSaTokenDao implements SaTokenDaoBySessionFollowObject {

    private static final Cache<String, Object> CAFFEINE = Caffeine.newBuilder()
            // 设置最后一次写入或访问后经过固定时间过期
            .expireAfterWrite(5, TimeUnit.SECONDS)
            // 初始的缓存空间大小
            .initialCapacity(100)
            // 缓存的最大条数
            .maximumSize(1000)
            .build();

    /**
     * 获取Value，如无返空
     */
    @Override
    public String get(String key) {
        Object o = CAFFEINE.get(key, k -> RedissonUtil.getCacheObject(key));
        return (String) o;
    }

    /**
     * 写入Value，并设定存活时间 (单位: 秒)
     */
    @Override
    public void set(String key, String value, long timeout) {
        setValue(key, value, timeout);
    }

    /**
     * 写入Object，并设定存活时间 (单位: 秒)
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        setValue(key,object,timeout);
    }

    /**
     * 设置键值对缓存
     * @param key
     * @param value
     * @param timeout
     */
    private static void setValue(String key, Object value, long timeout) {
        if (timeout == 0 || timeout <= NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == NEVER_EXPIRE) {
            RedissonUtil.setCacheObject(key, value);
        } else {
            RedissonUtil.setCacheObject(key, value, Duration.ofSeconds(timeout));
        }
        CAFFEINE.invalidate(key);
    }

    /**
     * 修修改指定key-value键值对 (过期时间不变)
     */
    @Override
    public void update(String key, String value) {
        if (RedissonUtil.hasKey(key)) {
            RedissonUtil.setCacheObject(key, value, true);
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 删除Value
     */
    @Override
    public void delete(String key) {
        if (RedissonUtil.deleteObject(key)) {
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 获取Value的剩余存活时间 (单位: 秒)
     */
    @Override
    public long getTimeout(String key) {
        long timeout = RedissonUtil.getTimeToLive(key);
        // 加1的目的 解决sa-token使用秒 redis是毫秒导致1秒的精度问题 手动补偿
        return timeout < 0 ? timeout : timeout / 1000 + 1;
    }

    /**
     * 修改Value的剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        RedissonUtil.expire(key, Duration.ofSeconds(timeout));
    }


    /**
     * 获取Object，如无返空
     */
    @Override
    public Object getObject(String key) {
        Object o = CAFFEINE.get(key, k -> RedissonUtil.getCacheObject(key));
        return o;
    }

    /**
     * 获取 Object (指定反序列化类型)，如无返空
     *
     * @param key 键名称
     * @return object
     */
    @SuppressWarnings("unchecked cast")
    @Override
    public <T> T getObject(String key, Class<T> classType) {
        Object o = CAFFEINE.get(key, k -> RedissonUtil.getCacheObject(key));
        return (T) o;
    }

    /**
     * 更新Object (过期时间不变)
     */
    @Override
    public void updateObject(String key, Object object) {
        if (RedissonUtil.hasKey(key)) {
            RedissonUtil.setCacheObject(key, object, true);
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 删除Object
     */
    @Override
    public void deleteObject(String key) {
        if (RedissonUtil.deleteObject(key)) {
            CAFFEINE.invalidate(key);
        }
    }

    /**
     * 获取Object的剩余存活时间 (单位: 秒)
     */
    @Override
    public long getObjectTimeout(String key) {
        long timeout = RedissonUtil.getTimeToLive(key);
        // 加1的目的 解决sa-token使用秒 redis是毫秒导致1秒的精度问题 手动补偿
        return timeout < 0 ? timeout : timeout / 1000 + 1;
    }

    /**
     * 修改Object的剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        RedissonUtil.expire(key, Duration.ofSeconds(timeout));
    }

    /**
     * 搜索数据
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        String keyStr = prefix + "*" + keyword + "*";
        return (List<String>) CAFFEINE.get(keyStr, k -> {
            Collection<String> keys = RedissonUtil.keys(keyStr);
            List<String> list = new ArrayList<>(keys);
            return SaFoxUtil.searchList(list, start, size, sortType);
        });
    }
}
