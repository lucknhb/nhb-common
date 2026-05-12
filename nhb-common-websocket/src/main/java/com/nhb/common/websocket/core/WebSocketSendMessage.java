package com.nhb.common.websocket.core;

import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.websocket.enums.WebSocketMessageLevel;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/18 9:43
 * @description: 发送对象及消息体
 */
@Data
public class WebSocketSendMessage implements Serializable {
    /**
     * 需要发送的用户ID
     */
    private List<Long> userIds;

    /**
     * 发送的消息
     */
    private MessageDetail data;

    @Data
    public static class MessageDetail implements Serializable {
        /**
         * 具体信息
         */
        private Object message;

        /**
         * 消息等级
         */
        private WebSocketMessageLevel  messageLevel;

        /**
         * 消息创建时间
         */
        private LocalDateTime createTime;

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
