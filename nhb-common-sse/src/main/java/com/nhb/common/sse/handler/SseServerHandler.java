package com.nhb.common.sse.handler;

import com.nhb.common.sse.auth.SseAuthService;
import com.nhb.common.sse.core.SseChannelHolder;
import com.nhb.common.sse.properties.SseConfigProperties;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/13 9:05
 * @description: 进行SSE用户认证连接处理
 */
@Slf4j
@RequiredArgsConstructor
public class SseServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private final SseConfigProperties sseConfigProperties;
    private final SseAuthService sseAuthService;


    /**
     * 处理读取
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest httpRequest) throws Exception {
        // 只处理 GET 请求，且路径匹配
        QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
        if (!(HttpMethod.GET.equals(httpRequest.method()) && decoder.path().equals(sseConfigProperties.getPath()))) {
            // 不符合匹配规则 返回未找到资源
            FullHttpResponse response = new DefaultFullHttpResponse(
                    httpRequest.protocolVersion(), HttpResponseStatus.NOT_FOUND);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        //进行认证
        Long userId = sseAuthService.authenticate(ctx.channel(), httpRequest);
        if (Objects.isNull(userId)) {
            // 认证书失败
            FullHttpResponse response = new DefaultFullHttpResponse(
                    httpRequest.protocolVersion(), HttpResponseStatus.FORBIDDEN);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            return;
        }
        // 关联用户
        ctx.channel().attr(SseChannelHolder.USER_ID_KEY).set(userId);
        // 存储连接
        SseChannelHolder.addChannel(userId, ctx.channel());
        //允许多用户使用同一账号
        log.info("SSE Connected: UserId[{}]", userId);
        // 响应 SSE 握手
        HttpResponse response = new DefaultHttpResponse(httpRequest.protocolVersion(), HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/event-stream; charset=UTF-8");
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "no-cache");
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        response.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        ctx.writeAndFlush(response);
        // 发送初始注释行（保持连接）
        ctx.writeAndFlush(new DefaultHttpContent(Unpooled.copiedBuffer(":\n\n", StandardCharsets.UTF_8)));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Long userId = ctx.channel().attr(SseChannelHolder.USER_ID_KEY).get();
        if (userId != null) {
            SseChannelHolder.removeChannel(userId,ctx.channel());
            log.info("SSE Disconnected UserId[{}]", userId);
        }
        ctx.fireChannelInactive();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent e) {
            if (IdleState.WRITER_IDLE.equals(e.state())) {
                // 发送 SSE 心跳注释行，维持连接
                ctx.writeAndFlush(new DefaultHttpContent(Unpooled.copiedBuffer(":\n\n", StandardCharsets.UTF_8)));
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("SSE Exception Caught", cause);
        Long userId = ctx.channel().attr(SseChannelHolder.USER_ID_KEY).get();
        if (userId != null) {
            SseChannelHolder.removeChannel(userId,ctx.channel());
            log.info("SSE Exception To Disconnect For UserId[{}]", userId);
        }
        ctx.close();
    }
}
