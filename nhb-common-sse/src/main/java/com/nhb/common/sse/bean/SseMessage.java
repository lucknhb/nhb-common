package com.nhb.common.sse.bean;

import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.sse.enums.SseMessageLevel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
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

    @Data
    public static class SseMessageDetail implements Serializable {
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
}
