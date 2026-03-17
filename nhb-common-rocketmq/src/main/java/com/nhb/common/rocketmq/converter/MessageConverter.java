package com.nhb.common.rocketmq.converter;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 8:53
 * @description: 消息转换器
 */
public interface MessageConverter {

    /**
     * 将消息转换为字节数组
     * @param body        消息体
     * @return            字节数组
     * @throws Exception  异常信息
     */
    byte[] toByte(Object body) throws Exception;

    /**
     * 将字节数组转成指定类型
     * @param body       字节数组
     * @param targetType 目标类型
     * @return           最终对象
     * @throws Exception 异常信息
     */
    <T> T fromMessage(byte[] body, Class<T> targetType) throws Exception;
}
