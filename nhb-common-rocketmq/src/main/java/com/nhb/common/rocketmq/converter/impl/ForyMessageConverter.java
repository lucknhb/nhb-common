package com.nhb.common.rocketmq.converter.impl;

import com.nhb.common.fory.factory.ForyFactory;
import com.nhb.common.rocketmq.converter.MessageConverter;
import lombok.RequiredArgsConstructor;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 9:50
 * @description: Fory信息转换器
 */
@RequiredArgsConstructor
public class ForyMessageConverter implements MessageConverter {
    private final ForyFactory foryFactory;
    /**
     * 将消息转换为字节数组
     *
     * @param body 消息体
     * @return 字节数组
     * @throws Exception 异常信息
     */
    @Override
    public byte[] toByte(Object body) throws Exception {
        foryFactory.register(body.getClass());
        return foryFactory.serialize(body);
    }

    /**
     * 将字节数组转成指定类型
     *
     * @param body       字节数组
     * @param targetType 目标类型
     * @return 最终对象
     * @throws Exception 异常信息
     */
    @Override
    public <T> T fromMessage(byte[] body, Class<T> targetType) throws Exception {
//        foryFactory.register(targetType);
        return foryFactory.deserialize(body,targetType);
    }
}
