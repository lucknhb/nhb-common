package com.nhb.common.sse.listeners;

import cn.hutool.core.collection.CollUtil;
import com.nhb.common.sse.core.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 10:01
 * @description: SSE 主题订阅监听器
 */
@Slf4j
@RequiredArgsConstructor
public class SseTopicListener implements ApplicationRunner, Ordered {

    private final SseEmitterManager sseEmitterManager;

    /**
     * 在Spring Boot应用程序启动时初始化SSE主题订阅监听器
     *
     * @param args 应用程序参数
     * @throws Exception 初始化过程中可能抛出的异常
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        sseEmitterManager.subscribeMessage((message) -> {
            log.info("SSE Topic Subscribe Receive The Message. The Session Keys={} Message={}", message.getUserIds(), message.getData());
            // 如果key不为空就按照key发消息 如果为空就群发
            if (CollUtil.isNotEmpty(message.getUserIds())) {
                message.getUserIds().forEach(key -> sseEmitterManager.sendMessage(key, message.getData()));
            } else {
                sseEmitterManager.sendMessage(message.getData());
            }
        });
        log.info(">>>> SSE init topic subscribe listener success <<<<");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
