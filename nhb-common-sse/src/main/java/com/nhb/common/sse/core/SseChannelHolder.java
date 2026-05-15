package com.nhb.common.sse.core;

import cn.hutool.core.text.StrPool;
import com.nhb.common.redis.utils.RedissonUtil;
import com.nhb.common.sse.bean.SseMessage;
import com.nhb.common.sse.properties.SseConfigProperties;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 8:49
 * @description: Server-Sent Events (SSE) 管理
 */
@Slf4j
@RequiredArgsConstructor
public class SseChannelHolder {
    /**
     * 订阅的频道
     */
    private final SseConfigProperties sseConfigProperties;

    public final static AttributeKey<Long> USER_ID_KEY = AttributeKey.valueOf("USER_ID");

    private final static Map<Long, Set<Channel>> USER_CHANNEL = new ConcurrentHashMap<>();

    /**
     * 将WebSocket会话添加到用户会话Map中
     *
     * @param userId     用户ID
     * @param channel    要添加的Channel会话
     */
    public static void addChannel(Long userId, Channel channel) {
        USER_CHANNEL.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(channel);
    }

    /**
     * 从用户会话Map中移除指定会话键对应的WebSocket会话
     *
     * @param userId 用户ID
     * @param channel
     */
    public static void removeChannel(Long userId, Channel channel) {
        //获取当前用户通道
        Set<Channel> channels = USER_CHANNEL.get(userId);
        if (channels != null) {
            channels.remove(channel);
            //如果删除后集合空了则清楚Map中的数据
            if (channels.isEmpty()) {
                USER_CHANNEL.remove(userId);
            }
        }
    }

    /**
     * 根据会话键从用户会话Map中获取WebSocket会话
     *
     * @param userId 用户ID
     * @return 与给定会话键对应的WebSocket会话，如果不存在则返回null
     */
    public static Set<Channel> getChannel(Long userId) {
        return USER_CHANNEL.getOrDefault(userId, Set.of());
    }

    /**
     * 获取存储在用户会话Map中所有WebSocket会话的会话键集合
     *
     * @return 所有WebSocket会话的用户ID集合
     */
    public static Set<Long> getChannelUserIds() {
        return USER_CHANNEL.keySet();
    }

    /**
     * 检查给定的会话键是否存在于用户会话Map中
     *
     * @param userId 要检查的会话键
     * @return 如果存在对应的会话键，则返回true；否则返回false
     */
    public static Boolean existChannel(Long userId) {
        return USER_CHANNEL.containsKey(userId);
    }

    /**
     * 订阅SSE消息主题，并提供一个消费者函数来处理接收到的消息
     *
     * @param consumer 处理SSE消息的消费者函数
     */
    public void subscribeMessage(Consumer<SseMessage> consumer) {
        RedissonUtil.subscribe(this.sseConfigProperties.getSseTopic(), SseMessage.class, consumer);
    }

    /**
     * 向指定的用户会话发送消息
     *
     * @param userId 要发送消息的用户id
     * @param data   要发送的消息内容
     */
    public void sendMessage(Long userId, SseMessage.SseMessageDetail data) {
        Set<Channel> channels = USER_CHANNEL.getOrDefault(userId, Set.of());
        StringBuilder sb = new StringBuilder();
        sb.append("data:").append(data).append(StrPool.LF+StrPool.LF);
        for (Channel channel : channels) {
            if (channel.isActive()) {
                channel.writeAndFlush(Unpooled.copiedBuffer(sb.toString(), StandardCharsets.UTF_8));
            }
        }
    }

    /**
     * 本机全用户会话发送消息
     *
     * @param data 要发送的消息内容
     */
    public void sendMessage(SseMessage.SseMessageDetail data) {
        for (Long userId : USER_CHANNEL.keySet()) {
            sendMessage(userId, data);
        }
    }

    /**
     * 发布SSE订阅消息
     *
     * @param sseMessage 要发布的SSE消息对象
     */
    public void publishMessage(SseMessage sseMessage) {
        SseMessage broadcastMessage = new SseMessage();
        broadcastMessage.setData(sseMessage.getData());
        broadcastMessage.setUserIds(sseMessage.getUserIds());
        RedissonUtil.publish(this.sseConfigProperties.getSseTopic(), broadcastMessage, consumer -> log.info("SSE Send Topic:{} Session Keys:{} Message:{}",
                this.sseConfigProperties.getSseTopic(), sseMessage.getUserIds(), sseMessage.getData()));
    }

    /**
     * 向所有的用户发布订阅的消息(群发)
     *
     * @param data 要发布的消息内容
     */
    public void publishAll(SseMessage.SseMessageDetail data) {
        SseMessage broadcastMessage = new SseMessage();
        broadcastMessage.setData(data);
        RedissonUtil.publish(this.sseConfigProperties.getSseTopic(), broadcastMessage, consumer -> {
            log.info("SSE Send Topic:{} Message:{}", this.sseConfigProperties.getSseTopic(), data);
        });
    }

    /**
     * 关闭所有连接（优雅停机用）
     */
    public void shutdown() {
        log.info("Stopping SSE Connect");
        USER_CHANNEL.forEach((userId, channels) -> channels.forEach(Channel::close));
        USER_CHANNEL.clear();
        log.info("End Stop SSE Connect");
    }

}
