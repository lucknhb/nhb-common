package com.nhb.common.sse.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.nhb.common.core.domain.ResultMessage;
import com.nhb.common.sse.core.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 8:42
 * @description: SSE 请求地址
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SseController {
    private final SseEmitterManager sseEmitterManager;

    /**
     * 建立 SSE 连接
     */
    @GetMapping(value = "${sse.path}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        String tokenValue = StpUtil.getTokenValue();
        long userId = StpUtil.getLoginIdAsLong();
        log.info("UserId[{}] Start Connect SSE", userId);
        return sseEmitterManager.connect(userId, tokenValue);
    }

    /**
     * 关闭 SSE 连接
     */
    @DeleteMapping(value = "${sse.path}/close")
    public ResultMessage<Void> closeConnect() {
        String tokenValue = StpUtil.getTokenValue();
        long userId = StpUtil.getLoginIdAsLong();
        sseEmitterManager.disconnect(userId, tokenValue);
        log.info("UserId[{}] Close Connect SSE", userId);
        return ResultMessage.ok();
    }

}
