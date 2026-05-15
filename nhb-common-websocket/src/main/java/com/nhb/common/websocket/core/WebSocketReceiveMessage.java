package com.nhb.common.websocket.core;

import lombok.Data;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/18 11:01
 * @description: 基础信息类
 */
@Data
public class WebSocketReceiveMessage {
    /**
     * 消息类型
     */
    private String type;

    /**
     * 接收到信息
     */
    private String message;
}
