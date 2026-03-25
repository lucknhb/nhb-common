package com.nhb.common.excel.annotation;

import java.lang.annotation.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 16:01
 * @description: 枚举格式化
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelEnumFormat {
    /**
     * 字典枚举类型
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * 枚举类中对应的code属性名称，默认为code
     */
    String codeField() default "code";

    /**
     * 枚举类中对应的属性名称，默认为value
     */
    String valueField() default "value";
}
