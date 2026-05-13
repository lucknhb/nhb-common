package com.nhb.common.sse.core;

import com.nhb.common.sse.auth.SseAuthService;
import com.nhb.common.sse.handler.SseServerHandler;
import com.nhb.common.sse.properties.SseConfigProperties;
import com.nhb.common.sse.ssl.SslContextFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
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
 * @date 2026/5/13 10:33
 * @description:
 */
@Slf4j
@RequiredArgsConstructor
public class SseServerInitializer extends ChannelInitializer<SocketChannel> {
    private final SseConfigProperties sseConfigProperties;
    private final SseAuthService sseAuthService;

    private final ResourceLoader resourceLoader;

    /**
     * 初始化SSL上下文
     *
     * @param resourceLoader 加载器
     * @return SSL上下文
     */
    private SslContext initSslContext(ResourceLoader resourceLoader) {
        try {
            return SslContextFactory.create(sseConfigProperties, resourceLoader);
        } catch (Exception e) {
            throw new RuntimeException("SSL Context Initialization Fails And The SSE Service Cannot Be Started", e);
        }
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        CorsConfigBuilder corsConfigBuilder = CorsConfigBuilder.forOrigins(sseConfigProperties.getAllowedOrigins().toArray(new String[0]))
                .allowedRequestHeaders(sseConfigProperties.getAllowedHeaders().toArray(new String[0]))
                .allowedRequestMethods(HttpMethod.DELETE, HttpMethod.POST, HttpMethod.PUT, HttpMethod.GET);
        CorsConfig corsConfig = null;
        if (Boolean.TRUE.equals(sseConfigProperties.getAllowCredentials())) {
            corsConfig = corsConfigBuilder.allowCredentials().build();
        } else {
            corsConfig = corsConfigBuilder.build();
        }
        //设置SSL功能
        SslContext sslContext = initSslContext(resourceLoader);
        if (Objects.nonNull(sslContext)) {
            p.addLast(sslContext.newHandler(ch.alloc()));
        }
        // HTTP 编解码
        p.addLast(new HttpServerCodec());
        p.addLast(new HttpObjectAggregator(65536));
        p.addLast(new ChunkedWriteHandler());
        //CORS处理
        p.addLast(new CorsHandler(corsConfig));
        // 空闲检测
        p.addLast(new IdleStateHandler(0, 60, 0));
        // 自定义 SSE 处理器
        p.addLast(new SseServerHandler(sseConfigProperties, sseAuthService));
    }
}
