package com.nhb.common.sse.core;

import cn.hutool.core.map.MapUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.redis.utils.RedissonUtil;
import com.nhb.common.sse.bean.SseMessage;
import com.nhb.common.sse.bean.SseMessageDetail;
import com.nhb.common.sse.properties.SseConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 8:49
 * @description: Server-Sent Events (SSE) 管理
 */
@Slf4j
public class SseEmitterManager {
    /**
     * 订阅的频道
     */
    private final SseConfigProperties sseConfigProperties;

    private final static Map<Long, Map<String, SseEmitter>> USER_TOKEN_EMITTERS = new ConcurrentHashMap<>();

    public SseEmitterManager(SseConfigProperties sseConfigProperties) {
        this.sseConfigProperties = sseConfigProperties;
        // 定时执行 SSE 心跳检测
        SpringContextUtil.getBean(ScheduledExecutorService.class)
                .scheduleWithFixedDelay(this::sseMonitor, sseConfigProperties.getHeartbeatInterval(), sseConfigProperties.getHeartbeatInterval(), TimeUnit.SECONDS);
    }

    /**
     * 建立与指定用户的 SSE 连接
     *
     * @param userId 用户的唯一标识符，用于区分不同用户的连接
     * @param token  用户的唯一令牌，用于识别具体的连接
     * @return 返回一个 SseEmitter 实例，客户端可以通过该实例接收 SSE 事件
     */
    public SseEmitter connect(Long userId, String token) {
        // 从 USER_TOKEN_EMITTERS 中获取或创建当前用户的 SseEmitter 映射表（ConcurrentHashMap）
        // 每个用户可以有多个 SSE 连接，通过 token 进行区分
        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
        // 关闭已存在的SseEmitter，防止超过最大连接数
        SseEmitter oldEmitter = emitters.remove(token);
        if (oldEmitter != null) {
            oldEmitter.complete();
        }

        // 创建一个新的 SseEmitter 实例，超时时间设置为一天 避免连接之后直接关闭浏览器导致连接停滞
        SseEmitter emitter = new SseEmitter(86400000L);

        emitters.put(token, emitter);

        // 当 emitter 完成、超时或发生错误时，从映射表中移除对应的 token
        emitter.onCompletion(() -> {
            SseEmitter remove = emitters.remove(token);
            if (remove != null) {
                remove.complete();
            }
        });
        emitter.onTimeout(() -> {
            SseEmitter remove = emitters.remove(token);
            if (remove != null) {
                remove.complete();
            }
        });
        emitter.onError((e) -> {
            SseEmitter remove = emitters.remove(token);
            if (remove != null) {
                remove.complete();
            }
        });

        try {
            // 向客户端发送一条连接成功的事件
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            // 如果发送消息失败，则从映射表中移除 emitter
            emitters.remove(token);
        }
        return emitter;
    }

    /**
     * 断开指定用户的 SSE 连接
     *
     * @param userId 用户的唯一标识符，用于区分不同用户的连接
     * @param token  用户的唯一令牌，用于识别具体的连接
     */
    public void disconnect(Long userId, String token) {
        if (userId == null || token == null) {
            return;
        }
        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.get(userId);
        if (MapUtil.isNotEmpty(emitters)) {
            try {
                SseEmitter sseEmitter = emitters.get(token);
                sseEmitter.send(SseEmitter.event().comment("disconnected"));
                sseEmitter.complete();
            } catch (Exception ignore) {
            }
            emitters.remove(token);
        } else {
            USER_TOKEN_EMITTERS.remove(userId);
        }
    }

    /**
     * SSE心跳检测，关闭无效连接
     */
    public void sseMonitor() {
        USER_TOKEN_EMITTERS.forEach((userId, map) ->
                map.entrySet().removeIf(e -> {
                    try {
                        e.getValue().send(SseEmitter.event().comment("PING"));
                        log.info("Send SSE PING To UserId[{}] Success", userId);
                        return false;
                    } catch (Exception ex) {
                        log.warn("Check heart fail，remove connect : userId={}, token={}", userId, e.getKey());
                        e.getValue().complete();
                        return true;
                    }
                })
        );
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
    public void sendMessage(Long userId, SseMessageDetail data) {
        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.get(userId);
        if (MapUtil.isNotEmpty(emitters)) {
            for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                try {
                    entry.getValue().send(SseEmitter.event()
                            .name("message")
                            .data(data));
                    log.info("Send SSE Message To UserId[{}] :{}", userId, data);
                } catch (Exception e) {
                    SseEmitter remove = emitters.remove(entry.getKey());
                    if (remove != null) {
                        remove.complete();
                    }
                }
            }
        } else {
            USER_TOKEN_EMITTERS.remove(userId);
        }
    }

    /**
     * 本机全用户会话发送消息
     *
     * @param data 要发送的消息内容
     */
    public void sendMessage(SseMessageDetail data) {
        for (Long userId : USER_TOKEN_EMITTERS.keySet()) {
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
    public void publishAll(SseMessageDetail data) {
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
        USER_TOKEN_EMITTERS.forEach((userId, emitters) -> emitters.values().forEach(SseEmitter::complete));
        USER_TOKEN_EMITTERS.clear();
        log.info("End Stop SSE Connect");
    }

}
