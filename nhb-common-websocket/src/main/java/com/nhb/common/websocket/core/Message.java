package com.nhb.common.websocket.core;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/18 11:01
 * @description: 基础信息类
 */
@Data
public abstract class Message {
    /**
     * 消息ID
     */
    private String id;
    /**
     * 消息类型
     */
    private String type;
    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;
}
