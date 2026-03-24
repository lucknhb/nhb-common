package com.nhb.common.translation.annotation;

import com.nhb.common.translation.core.TranslationInterface;

import java.lang.annotation.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 8:50
 * @description: 翻译类型注解 (标注到{@link TranslationInterface} 的实现类)
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface TranslationType {
    /**
     * 类型
     */
    String type();
}
