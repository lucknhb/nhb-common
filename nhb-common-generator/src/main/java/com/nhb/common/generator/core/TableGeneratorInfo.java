package com.nhb.common.generator.core;

import lombok.Data;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/27 17:04
 * @description: 实体类生成表结构信息
 */
@Data
public class TableGeneratorInfo {
    /**
     * 实体类包名
     */
    private String packageName;

    private String tableName;

    private String entityName;

    private String author;

    private String dateTime;
}
