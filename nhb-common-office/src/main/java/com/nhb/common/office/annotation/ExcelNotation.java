package com.nhb.common.office.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 16:02
 * @description: 批注
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelNotation {
    /**
     * 批注内容
     */
    String value() default "";
}
