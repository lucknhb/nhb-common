package com.nhb.common.idempotent.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/3 13:52
 * @description: 防止重复提交
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiRepeatSubmit {
    /**
     * 间隔时间(ms)，小于此时间视为重复提交
     */
    long interval() default 5000;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * 提示消息 支持国际化 格式为 {code}
     */
    String message() default "{repeat.submit.message}";
}
