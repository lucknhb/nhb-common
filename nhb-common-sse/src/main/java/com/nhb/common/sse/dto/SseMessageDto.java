package com.nhb.common.sse.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 8:51
 * @description: 消息
 */
@Data
public class SseMessageDto implements Serializable {
    /**
     * 需要推送到的session key 列表
     */
    private List<Long> userIds;

    /**
     * 需要发送的消息
     */
    private String message;
}
