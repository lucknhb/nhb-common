package com.nhb.common.rocketmq.converter.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.rocketmq.converter.MessageConverter;
import lombok.RequiredArgsConstructor;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 9:15
 * @description:
 */
@RequiredArgsConstructor
public class JacksonMessageConverter implements MessageConverter {

    private final ObjectMapper objectMapper;


    /**
     * 将消息转换为字节数组
     *
     * @param body 消息体
     * @return 字节数组
     * @throws Exception 异常信息
     */
    @Override
    public byte[] toByte(Object body) throws Exception {
        return JacksonUtil.toByte(body);
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
        // 如果targetType是泛型类型，需要处理
        return JacksonUtil.parseObject(body,targetType);
    }
}
