package com.nhb.common.websocket.enums;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/7 18:14
 * @description: 消息等级
 */
public enum WebSocketMessageLevel {
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
