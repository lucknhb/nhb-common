package com.nhb.common.encrypt.annotation;

import java.lang.annotation.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 9:54
 * @description: 接口加密注解
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiEncrypt {
    /**
     * 请求加密忽略，默认不加密，为 true 时加密
     */
    boolean request() default false;

    /**
     * 响应加密忽略，默认不加密，为 true 时加密
     */
    boolean response() default false;
}
