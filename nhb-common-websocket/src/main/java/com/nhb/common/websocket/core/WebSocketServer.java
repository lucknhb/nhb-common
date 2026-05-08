package com.nhb.common.websocket.core;

import com.nhb.common.websocket.auth.WebSocketAuthService;
import com.nhb.common.websocket.properties.WebSocketConfigProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/8 14:29
 * @description:
 */
@Slf4j
@RequiredArgsConstructor
public class WebSocketServer {
    private final WebSocketConfigProperties webSocketConfigProperties;
    private final WebSocketAuthService authService;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    /**
     * 启动服务
     */
    public void start() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketChannelInitializer(webSocketConfigProperties, authService))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(webSocketConfigProperties.getPort()).sync();
            serverChannel = f.channel();
            log.info("WebSocket Server Start Port[{}]", webSocketConfigProperties.getPort());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("WebSocket Server Start Happen Exception", e);
        }
    }

    /**
     * 关闭服务
     */
    public void stop() {
        if (serverChannel != null) {
            serverChannel.close();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        log.info("WebSocket Server Stop Port[{}]", webSocketConfigProperties.getPort());
    }
}
