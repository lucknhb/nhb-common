package com.nhb.common.office.annotation;

import java.lang.annotation.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 15:56
 * @description: excel 列单元格合并(合并列相同项)  需搭配 {@link CellMergeStrategy} 策略使用
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CellMerge {
    /**
     * col index
     */
    int index() default -1;

    /**
     * 合并需要依赖的其他字段名称
     */
    String[] mergeFields() default {};
}
