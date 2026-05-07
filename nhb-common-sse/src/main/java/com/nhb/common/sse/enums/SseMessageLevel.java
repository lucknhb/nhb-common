package com.nhb.common.sse.enums;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/7 15:09
 * @description: 消息等级
 */
public enum SseMessageLevel {
    /**
     * 普通消息
     */
    INFO,
    /**
     * 警告消息/紧急
     */
    WARNING,
    /**
     * 异常消息/十分紧急
     */
    ERROR;
}
