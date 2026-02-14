package com.nhb.common.core.validate.json;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/13 16:59
 * @description: JSON 类型枚举
 */
public enum JsonType {
    /**
     * JSON 对象，例如 {"a":1}
     */
    OBJECT,

    /**
     * JSON 数组，例如 [1,2,3]
     */
    ARRAY,

    /**
     * 任意 JSON 类型，对象或数组都可以
     */
    ANY
}
