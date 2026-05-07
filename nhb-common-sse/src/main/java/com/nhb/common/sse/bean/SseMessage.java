package com.nhb.common.sse.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 8:51
 * @description: 消息体
 */
@Data
public class SseMessage implements Serializable {
    /**
     * 需要推送到的session key 列表
     */
    private List<Long> userIds;

    /**
     * 详细信息
     */
    private SseMessageDetail data;
}
