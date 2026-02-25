package com.nhb.common.core.validate.enumd;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/14 8:39
 * @description: 枚举类型校验
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {EnumPatternValidator.class})
public @interface EnumPattern {
    /**
     * 需要校验的枚举类型
     */
    Class<? extends Enum<?>> type();

    /**
     * 枚举类型校验值字段名称
     * 需确保该字段实现了 getter 方法
     */
    String fieldName();

    String message() default "输入值不在枚举范围内";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
