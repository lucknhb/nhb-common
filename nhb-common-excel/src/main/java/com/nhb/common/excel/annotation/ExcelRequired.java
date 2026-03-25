package com.nhb.common.excel.annotation;

import org.apache.poi.ss.usermodel.IndexedColors;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 16:03
 * @description: 是否必填
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelRequired {
    /**
     * 字体颜色
     */
    IndexedColors fontColor() default IndexedColors.RED;
}
