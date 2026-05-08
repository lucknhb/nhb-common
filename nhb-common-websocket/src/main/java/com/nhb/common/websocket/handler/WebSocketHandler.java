package com.nhb.common.websocket.handler;

import cn.hutool.core.util.ObjectUtil;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.websocket.constant.WebSocketConstants;
import com.nhb.common.websocket.core.WebSocketReceiveMessage;
import com.nhb.common.websocket.holder.WebSocketSessionHolder;
import com.nhb.common.websocket.utils.WebSocketUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 15:37
 * @description:
 */
@Slf4j
@RequiredArgsConstructor
public class WebSocketHandler extends AbstractWebSocketHandler {

    @Resource
    private ObjectProvider<WebSocketReceiveMessageHandler> objectProvider;

    /**
     * 连接成功后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        Long userId = (Long) session.getAttributes().get(WebSocketConstants.USER_ID);
        //没有userId时为异常情况
        if (ObjectUtil.isNull(userId)) {
            session.close(CloseStatus.BAD_DATA);
            log.info("[Connect] Invalid Token Received. SessionId: {}", session.getId());
            return;
        }
        WebSocketSessionHolder.addSession(userId, new ConcurrentWebSocketSessionDecorator(session, 10 * 1000, 64000));
        log.info("[Connect] SessionId:{},UserId:{}", session.getId(), userId);
        //推送离线时未读消息
        WebSocketUtil.sendOfflineMessage(userId);
    }

    /**
     * 处理接收到的文本消息
     *
     * @param session WebSocket会话
     * @param message 接收到的文本消息
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String content = message.getPayload();
        log.info("[WebSocket] Receive Message:{}", content);
        WebSocketReceiveMessage webSocketReceiveMessage = JacksonUtil.parseObject(content, WebSocketReceiveMessage.class);
        Assert.notNull(objectProvider,"Not Found WebSocketReceiveMessageHandler");
        Set<WebSocketReceiveMessageHandler> webSocketReceiveMessageHandlers = objectProvider.orderedStream().collect(Collectors.toSet());
        for (WebSocketReceiveMessageHandler webSocketReceiveMessageHandler : webSocketReceiveMessageHandlers) {
            Assert.isTrue(Objects.nonNull(webSocketReceiveMessage) && StringUtil.isNotBlank(webSocketReceiveMessage.getType()),"Receive Message Type Is Null");
            if (webSocketReceiveMessageHandler.support(webSocketReceiveMessage.getType())) {
                webSocketReceiveMessageHandler.handleMessage(session, webSocketReceiveMessage.getMessage());
            }
        }
    }

    /**
     * 处理接收到的二进制消息
     *
     * @param session WebSocket会话
     * @param message 接收到的二进制消息
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
    }

    /**
     * 处理接收到的Pong消息（心跳监测）
     *
     * @param session WebSocket会话
     * @param message 接收到的Pong消息
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        WebSocketUtil.sendPongMessage(session);
    }

    /**
     * 处理WebSocket传输错误
     *
     * @param session   WebSocket会话
     * @param exception 发生的异常
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[Transport Error] SessionId: {} , Exception:{}", session.getId(), exception.getMessage());
    }

    /**
     * 在WebSocket连接关闭后执行清理操作
     *
     * @param session WebSocket会话
     * @param status  关闭状态信息
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = (Long) session.getAttributes().get(WebSocketConstants.USER_ID);
        if (ObjectUtil.isNull(userId)) {
            log.info("[Disconnect] Invalid Token Received. SessionId: {}", session.getId());
            return;
        }
        WebSocketSessionHolder.removeSession(userId);
        log.info("[Disconnect] SessionId: {},UserId:{},CloseStatus:{}", session.getId(), userId, status);
    }

    /**
     * 指示处理程序是否支持接收部分消息
     *
     * @return 如果支持接收部分消息，则返回true；否则返回false
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
