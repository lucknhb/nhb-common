package com.nhb.common.sms.core;

import com.nhb.common.redis.utils.RedissonUtil;
import com.nhb.common.sms.constant.SmsConstants;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.api.dao.SmsDao;

import java.time.Duration;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/23 15:21
 * @description: 主要用于短信重试和拦截的缓存
 */
@Slf4j
public class DefaultSmsDao implements SmsDao {
    /**
     * 存储
     *
     * @param key       键
     * @param value     值
     * @param cacheTime 缓存时间（单位：秒)
     */
    @Override
    public void set(String key, Object value, long cacheTime) {
        RedissonUtil.setCacheObject(SmsConstants.GLOBAL_SMS_KEY + key, value, Duration.ofSeconds(cacheTime));
    }

    /**
     * 存储
     *
     * @param key   键
     * @param value 值
     */
    @Override
    public void set(String key, Object value) {
        RedissonUtil.setCacheObject(SmsConstants.GLOBAL_SMS_KEY + key, value, true);
    }

    /**
     * 读取
     *
     * @param key 键
     * @return 值
     */
    @Override
    public Object get(String key) {
        return RedissonUtil.getCacheObject(SmsConstants.GLOBAL_SMS_KEY + key);
    }

    /**
     * remove
     * <p> 根据key移除缓存
     *
     * @param key 缓存键
     * @return 被删除的value
     * @author :Wind
     */
    @Override
    public Object remove(String key) {
        return RedissonUtil.deleteObject(SmsConstants.GLOBAL_SMS_KEY + key);
    }

    /**
     * 清空
     */
    @Override
    public void clean() {
        RedissonUtil.deleteKeys(SmsConstants.GLOBAL_SMS_KEY + "*");
    }
}
