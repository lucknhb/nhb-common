package com.nhb.common.sse.bean;

import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.sse.enums.SseMessageLevel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/7 15:29
 * @description: 详细信息
 */
@Data
public class SseMessageDetail implements Serializable {
    /**
     * 消息等级
     */
    public SseMessageLevel messageLevel;
    /**
     * 需要发送的消息
     */
    public Object message;
    /**
     * 创建时间
     */
    public LocalDateTime createTime;

    @Override
    public String toString() {
        try {
            return JacksonUtil.toJsonString(this);
        } catch (Exception e) {
            return super.toString();
        }
    }
}
