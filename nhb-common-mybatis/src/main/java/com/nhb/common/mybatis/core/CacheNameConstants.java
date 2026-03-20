package com.nhb.common.mybatis.core;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/20 16:32
 * @description: 缓存名称
 */
public interface CacheNameConstants {
    /**
     * 自定义权限SQL拼接语句 缓存7天
     */
    String CUSTOM_DATA_SCOPE_SQL = "custom_data_scope_sql#7d";

    /**
     * 部门及其子部门缓存 缓存7天
     */
    String DATA_SCOPE_DEPT_AND_CHILD = "data_scope_dept_and_child#7d";
}
