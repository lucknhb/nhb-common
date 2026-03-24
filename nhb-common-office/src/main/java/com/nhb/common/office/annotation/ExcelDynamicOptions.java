package com.nhb.common.office.annotation;

import com.nhb.common.office.core.ExcelOptionsProvider;

import java.lang.annotation.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 16:01
 * @description: Excel动态下拉选项注解
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelDynamicOptions {
    /**
     * 提供者类全限定名
     * 实现 {@link ExcelOptionsProvider}实现类接口
     */
    Class<? extends ExcelOptionsProvider> providerClass();
}
