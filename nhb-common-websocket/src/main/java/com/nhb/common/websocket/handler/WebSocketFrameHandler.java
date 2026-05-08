package com.nhb.common.websocket.handler;

import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.websocket.core.WebSocketReceiveMessage;
import com.nhb.common.websocket.holder.WebSocketChannelHolder;
import com.nhb.common.websocket.utils.WebSocketUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/8 11:11
 * @description: webSocket协议实际处理器
 */
@Slf4j
public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
    @Resource
    private ObjectProvider<WebSocketReceiveMessageHandler> objectProvider;

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent event) {
            if (event.state() == IdleState.READER_IDLE) {
                log.warn("WebSocket Read TimeOut Send [Ping] To Test Live");
                ctx.writeAndFlush(new PingWebSocketFrame());
            } else if (event.state() == IdleState.ALL_IDLE) {
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
        Long userId = ctx.channel().attr(WebSocketAuthHandler.USER_ID_KEY).get();
        //无userId的话 直接关闭连接
        if (Objects.isNull(userId)) {
            ctx.close();
        }
        if (frame instanceof TextWebSocketFrame textFrame) {
            String text = textFrame.text();
            log.debug("Receive UserId[{}] Message Is: {}", userId, text);
            WebSocketReceiveMessage webSocketReceiveMessage = JacksonUtil.parseObject(text, WebSocketReceiveMessage.class);
            Assert.notNull(objectProvider,"Not Found WebSocketReceiveMessageHandler");
            Set<WebSocketReceiveMessageHandler> webSocketReceiveMessageHandlers = objectProvider.orderedStream().collect(Collectors.toSet());
            for (WebSocketReceiveMessageHandler webSocketReceiveMessageHandler : webSocketReceiveMessageHandlers) {
                Assert.isTrue(Objects.nonNull(webSocketReceiveMessage) && StringUtil.isNotBlank(webSocketReceiveMessage.getType()),"Receive Message Type Is Null");
                if (webSocketReceiveMessageHandler.support(webSocketReceiveMessage.getType())) {
                    try {
                        //实际接收内容及处理该消息的逻辑
                        webSocketReceiveMessageHandler.handleMessage(ctx , webSocketReceiveMessage.getMessage());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else if (frame instanceof PingWebSocketFrame) {
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof PongWebSocketFrame) {
            log.info("Received pong frame [{}]", frame.content().retain());
        } else if (frame instanceof CloseWebSocketFrame) {
            WebSocketChannelHolder.removeChannel(userId, ctx.channel());
            ctx.close();
            log.error("Client DisConnect WebSocket UserId[{}]", userId);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Long userId = ctx.channel().attr(WebSocketAuthHandler.USER_ID_KEY).get();
        if (userId != null) {
            WebSocketChannelHolder.addChannel(userId, ctx.channel());
            log.info("Client Connect WebSocket: UserId[{}], channelId[{}]", userId, ctx.channel().id());
            //推送离线时未读消息
            WebSocketUtil.sendOfflineMessage(userId);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Long userId = ctx.channel().attr(WebSocketAuthHandler.USER_ID_KEY).get();
        if (Objects.nonNull(userId)) {
            WebSocketChannelHolder.removeChannel(userId, ctx.channel());
            log.error("Client DisConnect WebSocket UserId[{}], channelId[{}]", userId, ctx.channel().id());
        }
        // 继续传递事件（允许后续 Handler 处理）
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Long userId = ctx.channel().attr(WebSocketAuthHandler.USER_ID_KEY).get();
        if (Objects.nonNull(userId)) {
            WebSocketChannelHolder.removeChannel(userId, ctx.channel());
            log.error("Client DisConnect WebSocket UserId[{}], channelId[{}] With Exception", userId, ctx.channel().id(),cause);
        }
        ctx.close();
    }
}
