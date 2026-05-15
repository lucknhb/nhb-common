package com.nhb.common.websocket.holder;

import io.netty.channel.Channel;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 16:33
 * @description: WebSocketSession 用于保存当前实例中用户会话
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebSocketChannelHolder {
    private static final Map<Long, Set<Channel>> USER_CHANNEL = new ConcurrentHashMap<>();

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
}
