package com.nhb.common.limiter.annotation;

import com.nhb.common.limiter.enums.LimitType;
import org.redisson.api.RateType;

import java.lang.annotation.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 10:25
 * @description: 限流注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiRateLimiter {
    /**
     * 限流key,支持使用Spring el表达式来动态获取方法上的参数值
     * 格式类似于  #code.id #{#code}
     */
    String key() default "";

    /**
     * 限流时间,单位秒
     */
    int time() default 60;

    /**
     * 限流次数
     */
    int count() default 100;

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.DEFAULT;

    /**
     * OVERALL : 作用在所有的RRateLimiter实例 <p/>
     *
     * PER_CLIENT : 作用在同一个Redisson实例创建的 RRateLimiter上面<p/>
     *
     *
     */
    RateType rateType() default RateType.OVERALL;

    /**
     * 提示消息 支持国际化 格式为 {code}
     */
    String message() default "{api.limiter.message}";

    /**
     * 限流策略超时时间 默认一天(策略存活时间 会清除已存在的策略数据)
     */
    int timeOut() default 86400;
}
