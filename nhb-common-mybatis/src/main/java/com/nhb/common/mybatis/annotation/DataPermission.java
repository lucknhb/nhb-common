package com.nhb.common.mybatis.annotation;

import com.nhb.common.mybatis.enums.JoinType;

import java.lang.annotation.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/19 16:08
 * @description: 数据权限组注解，用于标记数据权限配置数组
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {
    /**
     * 数据权限配置数组，用于指定数据权限的占位符关键字和替换值
     *
     * @return 数据权限配置数组
     */
    DataColumn[] value();

    /**
     * 权限拼接标识符(用于指定连接语句的sql符号)
     * 默认查询使用OR 其他使用AND
     */
    JoinType joinType();
}
