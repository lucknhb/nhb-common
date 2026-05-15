package com.nhb.common.websocket.handler;

import com.nhb.common.websocket.auth.WebSocketAuthService;
import com.nhb.common.websocket.constant.WebSocketConstants;
import com.nhb.common.websocket.holder.WebSocketChannelHolder;
import com.nhb.common.websocket.properties.WebSocketConfigProperties;
import com.nhb.common.websocket.utils.WebSocketUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AttributeKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/8 10:42
 * @description: netty环境下认证处理
 */
@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthHandler extends ChannelInboundHandlerAdapter {
    public static final AttributeKey<Long> USER_ID_KEY = AttributeKey.valueOf(WebSocketConstants.USER_ID);
    private final WebSocketAuthService webSocketAuthService;
    private final WebSocketConfigProperties webSocketConfigProperties;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest httpRequest) {
            try {
                Long userId = webSocketAuthService.authenticate(ctx.channel(), httpRequest);
                if (Objects.isNull(userId)) {
                    // 认证失败，返回403并关闭连接
                    FullHttpResponse response = new DefaultFullHttpResponse(
                            httpRequest.protocolVersion(), HttpResponseStatus.FORBIDDEN);
                    ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                    return;
                }
                // 将userId绑定到Channel属性
                ctx.channel().attr(USER_ID_KEY).set(userId);
                WebSocketChannelHolder.addChannel(userId, ctx.channel());
                log.info("Client Connect WebSocket: UserId[{}], channelId[{}]", userId, ctx.channel().id());
                // 移除自身，避免后续帧被拦截
                ctx.pipeline().remove(this);
                //推送离线时未读消息
                WebSocketUtil.sendOfflineMessage(userId);
                //请求路径 剔除参数风险
                httpRequest.setUri(webSocketConfigProperties.getPath());
                // 传递给下一个处理器（WebSocket协议升级）
                super.channelRead(ctx, msg);
            } catch (Exception e) {
                log.error("User Auth Error", e);
                throw new RuntimeException(e);
            }

        } else {
            // 非HTTP请求直接透传
            super.channelRead(ctx, msg);
        }
    }
}
