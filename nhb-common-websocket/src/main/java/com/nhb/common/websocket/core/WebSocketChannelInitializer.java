package com.nhb.common.websocket.core;

import com.nhb.common.websocket.auth.WebSocketAuthService;
import com.nhb.common.websocket.handler.WebSocketAuthHandler;
import com.nhb.common.websocket.handler.WebSocketFrameHandler;
import com.nhb.common.websocket.properties.WebSocketConfigProperties;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/8 14:01
 * @description:
 */
@Slf4j
@RequiredArgsConstructor
public class WebSocketChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final WebSocketConfigProperties webSocketConfigProperties;
    private final WebSocketAuthService webSocketAuthService;

    @Override
    protected void initChannel(SocketChannel ch) {
        CorsConfigBuilder corsConfigBuilder = CorsConfigBuilder.forOrigins(webSocketConfigProperties.getAllowedOrigins().toArray(new String[0]))
                .allowedRequestHeaders(webSocketConfigProperties.getAllowedHeaders().toArray(new String[0]))
                .allowedRequestMethods(HttpMethod.DELETE, HttpMethod.POST, HttpMethod.PUT, HttpMethod.GET);
        CorsConfig corsConfig = null;
        if (Boolean.TRUE.equals(webSocketConfigProperties.getAllowCredentials())) {
            corsConfig = corsConfigBuilder.allowCredentials().build();
        } else {
            corsConfig = corsConfigBuilder.build();
        }

        ch.pipeline().addLast(new IdleStateHandler(
                        webSocketConfigProperties.getReaderIdleTimeSeconds(),
                        webSocketConfigProperties.getWriterIdleTimeSeconds(),
                        webSocketConfigProperties.getAllIdleTimeSeconds()))
                .addLast(new HttpServerCodec())
                .addLast(new ChunkedWriteHandler())
                .addLast(new HttpObjectAggregator(webSocketConfigProperties.getMaxFrameSize()))
                .addLast(new CorsHandler(corsConfig))
                .addLast(new WebSocketAuthHandler(webSocketAuthService))
                .addLast(new WebSocketServerProtocolHandler(webSocketConfigProperties.getPath(), null, true, webSocketConfigProperties.getMaxFrameSize()))
                .addLast(new WebSocketFrameHandler());
    }
}
