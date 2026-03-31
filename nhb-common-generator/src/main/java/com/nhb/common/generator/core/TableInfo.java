package com.nhb.common.generator.core;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/30 9:04
 * @description: 表信息
 */
@Data
public class TableInfo {
    /**
     * 生成者
     */
    private String author;

    /**
     * 创建时间
     */
    private String dateTime;

    /**
     * 表名称
     */
    private String tableName;

    /**
     * 实体类名称(首字母大写)
     */
    private String entityName;

    /**
     * 生成包路径
     */
    private String packageName;

    /**
     * 生成功能描述
     */
    private String functionInfo;

    /**
     * 租户标识
     */
    private Boolean tenantFlag = false;

    /**
     * 表列信息
     */
    private List<TableColumnInfo> columns = new ArrayList<>();

    /**
     * 需要导入的包路劲
     */
    private Set<String> imports =  new HashSet<>();

}
