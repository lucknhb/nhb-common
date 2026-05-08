package com.nhb.common.websocket.handler;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/18 10:52
 * @description: 消息处理器
 */
public interface WebSocketReceiveMessageHandler {

    /**
     * 是否支持当前请求类型
     * @param type             信息类型
     * @return                是否支持
     */
    boolean support(String type);

    /**
     * 实际处理实现 需自行转换需要处理的数据类型 该数据为原始数据(包含请求类型)
     * @param channelHandlerContext 当前会话通道上下文
     * @param receiveMessage   接收到的信息
     * @throws Exception       抛出的异常
     */
    void handleMessage(ChannelHandlerContext channelHandlerContext, String receiveMessage) throws Exception;
}
