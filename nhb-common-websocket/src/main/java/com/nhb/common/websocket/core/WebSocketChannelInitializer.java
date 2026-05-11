package com.nhb.common.websocket.core;

import com.nhb.common.websocket.auth.WebSocketAuthService;
import com.nhb.common.websocket.handler.WebSocketAuthHandler;
import com.nhb.common.websocket.handler.WebSocketFrameHandler;
import com.nhb.common.websocket.properties.WebSocketConfigProperties;
import com.nhb.common.websocket.ssl.SslContextFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;

import java.util.Objects;

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
    private  final ResourceLoader resourceLoader;

    /**
     * 初始化SSL上下文
     * @param resourceLoader  加载器
     * @return                SSL上下文
     */
    private SslContext initSslContext(ResourceLoader resourceLoader) {
        try {
            return SslContextFactory.create(webSocketConfigProperties, resourceLoader);
        } catch (Exception e) {
            throw new RuntimeException("SSL Context Initialization Fails And The WSS Service Cannot Be Started", e);
        }
    }

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
        //设置SSL功能
        SslContext sslContext = initSslContext(resourceLoader);
        if (Objects.nonNull(sslContext)) {
            ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
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
