package com.nhb.common.sse.core;

import com.nhb.common.sse.auth.SseAuthService;
import com.nhb.common.sse.properties.SseConfigProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/13 10:39
 * @description:
 */
@Slf4j
@RequiredArgsConstructor
public class SseServer {
    private final SseConfigProperties sseConfigProperties;
    private final SseAuthService sseAuthService;
    private final ResourceLoader resourceLoader;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public void start() throws InterruptedException {
        bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("SSE-BOSS"));
        workerGroup = new NioEventLoopGroup(0, new DefaultThreadFactory("SSE-WORKER"));
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new SseServerInitializer(sseConfigProperties, sseAuthService, resourceLoader))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture f = b.bind(sseConfigProperties.getPort()).sync();
        serverChannel = f.channel();
        log.info("SSE Server Started On Port[{}]", sseConfigProperties.getPort());
    }

    public void stop() {
        if (serverChannel != null) {
            serverChannel.close().awaitUninterruptibly();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("SSE Server stopped");
    }
}
