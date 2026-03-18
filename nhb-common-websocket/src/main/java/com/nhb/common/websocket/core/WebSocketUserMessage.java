package com.nhb.common.websocket.core;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/18 9:43
 * @description: 发送对象及消息体
 */
@Data
public class WebSocketUserMessage implements Serializable {
    /**
     * 发送的消息
     */
    private String message;
    /**
     * 需要发送的用户ID
     */
    private List<Long> userIds;
}
