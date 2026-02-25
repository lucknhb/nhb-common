package com.nhb.common.sensitive.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.nhb.common.sensitive.serializer.SensitiveSerializer;
import com.nhb.common.sensitive.strategy.SensitiveStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/25 15:49
 * @description: 脱敏注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerializer.class)
public @interface Sensitive {
    SensitiveStrategy strategy();

    /**
     * 角色标识符 多个角色满足一个即可
     */
    String[] roleKeys() default {};

    /**
     * 权限标识符 多个权限满足一个即可
     */
    String[] permissions() default {};
}
