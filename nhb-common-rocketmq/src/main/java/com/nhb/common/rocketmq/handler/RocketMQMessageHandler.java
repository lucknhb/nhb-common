package com.nhb.common.rocketmq.handler;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/13 16:51
 * @description: 消息处理器
 */
public interface RocketMQMessageHandler<T> {
    /**
     * 消息处理器
     * @param body       具体数据
     * @throws Exception
     */
    void handle(T body) throws Exception;
}
