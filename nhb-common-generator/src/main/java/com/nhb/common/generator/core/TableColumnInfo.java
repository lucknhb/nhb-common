package com.nhb.common.generator.core;

import lombok.Data;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/30 9:04
 * @description: 表字段信息
 */
@Data
public class TableColumnInfo {
    /**
     * 列名称
     */
    private String columnName;

    /**
     * 列描述
     */
    private String columnComment;

    /**
     * 列类型
     */
    private String columnType;

    /**
     * JAVA类型
     */
    private String javaType;

    /**
     * JAVA字段名
     */
    private String javaField;

    /**
     * 是否主键
     */
    private Boolean primaryKeyFlag;

    /**
     * 是否自增
     */
    private Boolean incrementFlag;

    /**
     * 是否必填
     */
    private Boolean requiredFlag;
}
