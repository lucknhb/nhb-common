package com.nhb.common.sse.utils;

import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.sse.bean.SseMessage;
import com.nhb.common.sse.core.SseChannelHolder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 8:56
 * @description: SSE 信息发送工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SseMessageUtil {
    private static final SseChannelHolder MANAGER = SpringContextUtil.getBean(SseChannelHolder.class);

    /**
     * 向指定的SSE会话发送消息
     *
     * @param userId  要发送消息的用户id
     * @param data 要发送的消息内容
     */
    public static void sendMessage(Long userId, SseMessage.SseMessageDetail data) {
        MANAGER.sendMessage(userId, data);
    }

    /**
     * 本机全用户会话发送消息
     *
     * @param data 要发送的消息内容
     */
    public static void sendMessage(SseMessage.SseMessageDetail data) {
        MANAGER.sendMessage(data);
    }

    /**
     * 发布SSE订阅消息
     *
     * @param sseMessage 要发布的SSE消息对象
     */
    public static void publishMessage(SseMessage sseMessage) {
        MANAGER.publishMessage(sseMessage);
    }

    /**
     * 向所有的用户发布订阅的消息(群发)
     *
     * @param data 要发布的消息内容
     */
    public static void publishAll(SseMessage.SseMessageDetail data) {
        MANAGER.publishAll(data);
    }

}
