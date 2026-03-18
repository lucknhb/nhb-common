package com.nhb.common.websocket.listener;

import cn.hutool.core.collection.CollUtil;
import com.nhb.common.websocket.holder.WebSocketSessionHolder;
import com.nhb.common.websocket.utils.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/18 10:39
 * @description: WebSocket 主题订阅监听器
 */
@Slf4j
public class WebSocketTopicListener implements ApplicationRunner, Ordered {
    /**
     * 在应用程序启动时初始化WebSocket主题订阅监听器
     *
     * @param args 应用程序参数
     * @throws Exception 初始化过程中可能抛出的异常
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 订阅WebSocket消息
        WebSocketUtil.subscribeMessage((message) -> {
            log.info("WebSocket Receive TopicSubscription UserIds={} Message={}", message.getUserIds(), message.getMessage());
            // 如果key不为空就按照key发消息 如果为空就群发
            if (CollUtil.isNotEmpty(message.getUserIds())) {
                message.getUserIds().forEach(userId -> {
                    if (WebSocketSessionHolder.existSession(userId)) {
                        WebSocketUtil.sendMessage(userId,message.getMessage());
                    }
                });
            } else {
                WebSocketSessionHolder.getSessionsAll().forEach(key -> WebSocketUtil.sendMessage(key, message.getMessage()));
            }
        });
        log.info("Initializing The WebSocket Topic Subscription Listener Is Successful");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
