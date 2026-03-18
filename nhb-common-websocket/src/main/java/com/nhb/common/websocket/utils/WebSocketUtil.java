package com.nhb.common.websocket.utils;

import cn.hutool.core.collection.CollUtil;
import com.nhb.common.redis.utils.RedissonUtil;
import com.nhb.common.websocket.constant.WebSocketConstants;
import com.nhb.common.websocket.core.WebSocketUserMessage;
import com.nhb.common.websocket.holder.WebSocketSessionHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/18 9:10
 * @description:
 */
@Slf4j
public class WebSocketUtil {

    /**
     * 获取会话中的用户ID
     * @param session  当前会话
     * @return         当前会话的用户ID
     */
    public static Long getUserId(WebSocketSession session){
        return (Long) session.getAttributes().get(WebSocketConstants.USER_ID);
    }

    /**
     * 向指定的WebSocket会话发送文本消息
     *
     * @param session WebSocket会话
     * @param message 要发送的文本消息内容
     */
    public static void sendMessage(WebSocketSession session, String message) {
        sendMessage(session, new TextMessage(message));
    }

    /**
     * 向指定的WebSocket会话发送WebSocket消息对象
     *
     * @param session WebSocket会话
     * @param message 要发送的WebSocket消息对象
     */
    private static void sendMessage(WebSocketSession session, WebSocketMessage<?> message) {
        if (session == null || !session.isOpen()) {
            log.warn("[send] session会话已经关闭");
        } else {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                log.error("[send] session({}) 发送消息({}) 异常", session, message, e);
            }
        }
    }

    /**
     * 向指定的WebSocket会话发送消息
     *
     * @param userId 要发送消息的用户id
     * @param message    要发送的消息内容
     */
    public static void sendMessage(Long userId, String message) {
        WebSocketSession session = WebSocketSessionHolder.getSession(userId);
        sendMessage(session, message);
    }

    /**
     * 订阅WebSocket消息主题，并提供一个消费者函数来处理接收到的消息
     *
     * @param consumer 处理WebSocket消息的消费者函数
     */
    public static void subscribeMessage(Consumer<WebSocketUserMessage> consumer) {
        RedissonUtil.subscribe(WebSocketConstants.WEB_SOCKET_TOPIC, WebSocketUserMessage.class, consumer);
    }

    /**
     * 发布WebSocket订阅消息
     *
     * @param webSocketMessage 要发布的WebSocket消息对象
     */
    public static void publishMessage(WebSocketUserMessage webSocketMessage) {
        List<Long> userIds = new ArrayList<>();
        // 当前服务内session,直接发送消息
        for (Long userId : webSocketMessage.getUserIds()) {
            if (WebSocketSessionHolder.existSession(userId)) {
                sendMessage(userId, webSocketMessage.getMessage());
                continue;
            }
            userIds.add(userId);
        }
        // 不在线的用户进行数据订阅 其他实例中可能存在该用户
        if (CollUtil.isNotEmpty(userIds)) {
            WebSocketUserMessage broadcastMessage = new WebSocketUserMessage();
            broadcastMessage.setMessage(webSocketMessage.getMessage());
            broadcastMessage.setUserIds(userIds);
            RedissonUtil.publish(WebSocketConstants.WEB_SOCKET_TOPIC, broadcastMessage,
                    consumer -> log.info("WebSocket Send TopicSubscription Message Topic:{} Session Keys:{} Message:{}",
                    WebSocketConstants.WEB_SOCKET_TOPIC, userIds, webSocketMessage.getMessage()));
        }
    }

    /**
     * 向所有的WebSocket会话发布订阅的消息(群发)
     *
     * @param message 要发布的消息内容
     */
    public static void publishAll(String message) {
        WebSocketUserMessage broadcastMessage = new WebSocketUserMessage();
        broadcastMessage.setMessage(message);
        RedissonUtil.publish(WebSocketConstants.WEB_SOCKET_TOPIC, broadcastMessage,
                consumer -> log.info("WebSocket Send TopicSubscription Message Topic:{} Message:{}", WebSocketConstants.WEB_SOCKET_TOPIC, message));
    }

    /**
     * 向指定的WebSocket会话发送Pong消息
     *
     * @param session 要发送Pong消息的WebSocket会话
     */
    public static void sendPongMessage(WebSocketSession session) {
        sendMessage(session, new PongMessage());
    }

    /**
     * 存储离线信息至缓存中
     * @param userId     用户ID
     * @param message   具体信息
     */
    public static void saveOfflineMessage(Long userId,String message) {
        //需要先将未推送数据存起来 推送后删除
        String key = WebSocketConstants.WEB_SOCKET_OFFLINE_MESSAGE + userId;
        RList<String> offlineMessage = RedissonUtil.getClient().getList(key);
        offlineMessage.add(message);
        //设置3天超时时间
        offlineMessage.expire(Duration.ofDays(3));
    }

    /**
     * 发送离线时未读消息
     * @param userId  需要推送的用户ID
     */
    public static void sendOfflineMessage(Long userId){
        // 从 Redis 中拉取该用户的所有离线消息并推送
        String offlineKey = WebSocketConstants.WEB_SOCKET_OFFLINE_MESSAGE + userId;
        RList<String> offlineList = RedissonUtil.getClient().getList(offlineKey);
        // 获取所有消息
        List<String> messages = offlineList.readAll();
        if (!messages.isEmpty()) {
            // 推送所有消息给用户（可以按顺序推送）
            for (String message : messages) {
                sendMessage(userId, message);
            }
            // 推送后删除离线列表
            offlineList.delete();
        }
    }
}
