package com.nhb.common.websocket.utils;

import cn.hutool.core.collection.CollUtil;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.redis.utils.RedissonUtil;
import com.nhb.common.websocket.core.WebSocketSendMessage;
import com.nhb.common.websocket.handler.WebSocketAuthHandler;
import com.nhb.common.websocket.holder.WebSocketChannelHolder;
import com.nhb.common.websocket.properties.WebSocketConfigProperties;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/18 9:10
 * @description:
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebSocketUtil {
    private final static WebSocketConfigProperties WEB_SOCKET_CONFIG_PROPERTIES = SpringContextUtil.getBean(WebSocketConfigProperties.class);

    /**
     * 获取会话中的用户ID
     * @param channel  当前会话
     * @return         当前会话的用户ID
     */
    public static Long getUserId(Channel channel){
        return channel.attr(WebSocketAuthHandler.USER_ID_KEY).get();
    }

    /**
     * 向指定的WebSocket会话发送文本消息
     *
     * @param channel WebSocket会话
     * @param data   要发送的文本消息内容
     */
    public static void sendMessage(Channel channel, WebSocketSendMessage.MessageDetail data) {
        sendMessage(channel, new TextWebSocketFrame(Objects.requireNonNull(JacksonUtil.toJsonString(data))));
    }

    /**
     * 向指定的WebSocket会话发送WebSocket消息对象
     *
     * @param channel WebSocket会话
     * @param textWebSocketFrame 要发送的WebSocket消息对象
     */
    private static void sendMessage(Channel channel, TextWebSocketFrame textWebSocketFrame) {
        if (channel.isActive()) {
            channel.writeAndFlush(textWebSocketFrame);
        } else {
            Long userId = getUserId(channel);
            log.error("[Send] UserId[{}] Message:{} Exception[NOT ACTIVE]", userId, textWebSocketFrame.text());
        }
    }

    /**
     * 向指定的WebSocket会话发送消息
     *
     * @param userId 要发送消息的用户id
     * @param data    要发送的消息内容
     */
    public static void sendMessage(Long userId, WebSocketSendMessage.MessageDetail data) {
        Set<Channel> channels = WebSocketChannelHolder.getChannel(userId);
        //可能一个账号多人登录 会存在多个会话通道
        for (Channel channel : channels) {
            sendMessage(channel, data);
        }
    }

    /**
     * 订阅WebSocket消息主题，并提供一个消费者函数来处理接收到的消息
     *
     * @param consumer 处理WebSocket消息的消费者函数
     */
    public static void subscribeMessage(Consumer<WebSocketSendMessage> consumer) {
        RedissonUtil.subscribe(WEB_SOCKET_CONFIG_PROPERTIES.getClusterTopic(), WebSocketSendMessage.class, consumer);
    }

    /**
     * 发布WebSocket订阅消息
     *
     * @param webSocketMessage 要发布的WebSocket消息对象
     */
    public static void publishMessage(WebSocketSendMessage webSocketMessage) {
        List<Long> userIds = new ArrayList<>();
        // 当前服务内session,直接发送消息
        for (Long userId : webSocketMessage.getUserIds()) {
            if (WebSocketChannelHolder.existChannel(userId)) {
                sendMessage(userId, webSocketMessage.getData());
                continue;
            }
            userIds.add(userId);
        }
        // 不在线的用户进行数据订阅 其他实例中可能存在该用户
        if (CollUtil.isNotEmpty(userIds)) {
            WebSocketSendMessage broadcastMessage = new WebSocketSendMessage();
            broadcastMessage.setData(webSocketMessage.getData());
            broadcastMessage.setUserIds(userIds);
            RedissonUtil.publish(WEB_SOCKET_CONFIG_PROPERTIES.getClusterTopic(), broadcastMessage,
                    consumer -> log.info("WebSocket Send TopicSubscription WebSocketReceiveMessage Topic:{} Session Keys:{} WebSocketSendMessage:{}",
                            WEB_SOCKET_CONFIG_PROPERTIES.getClusterTopic(), userIds, webSocketMessage.getData()));
        }
    }

    /**
     * 向所有的WebSocket会话发布订阅的消息(群发)
     *
     * @param data 要发布的消息内容
     */
    public static void publishAll(WebSocketSendMessage.MessageDetail data) {
        WebSocketSendMessage broadcastMessage = new WebSocketSendMessage();
        broadcastMessage.setData(data);
        RedissonUtil.publish(WEB_SOCKET_CONFIG_PROPERTIES.getClusterTopic(), broadcastMessage,
                consumer -> log.info("WebSocket Send TopicSubscription WebSocketReceiveMessage Topic:{} WebSocketReceiveMessage:{}",WEB_SOCKET_CONFIG_PROPERTIES.getClusterTopic(), data));
    }

    /**
     * 存储离线信息至缓存中
     * @param userId     用户ID
     * @param data   具体信息
     */
    public static void saveOfflineMessage(Long userId,WebSocketSendMessage.MessageDetail data) {
        //需要先将未推送数据存起来 推送后删除
        String key = WEB_SOCKET_CONFIG_PROPERTIES.getOfflineMessageTopic() + userId;
        RList<WebSocketSendMessage.MessageDetail> offlineMessage = RedissonUtil.getClient().getList(key);
        offlineMessage.add(data);
        //设置3天超时时间
        offlineMessage.expire(Duration.ofDays(3));
    }

    /**
     * 发送离线时未读消息
     * @param userId  需要推送的用户ID
     */
    public static void sendOfflineMessage(Long userId){
        // 从 Redis 中拉取该用户的所有离线消息并推送
        String offlineKey = WEB_SOCKET_CONFIG_PROPERTIES.getOfflineMessageTopic() + userId;
        RList<WebSocketSendMessage.MessageDetail> offlineList = RedissonUtil.getClient().getList(offlineKey);
        // 获取所有消息
        List<WebSocketSendMessage.MessageDetail> messages = offlineList.readAll();
        if (!messages.isEmpty()) {
            // 推送所有消息给用户（可以按顺序推送）
            for (WebSocketSendMessage.MessageDetail message : messages) {
                sendMessage(userId, message);
            }
            // 推送后删除离线列表
            offlineList.delete();
        }
    }
}
