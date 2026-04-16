package com.nhb.common.rocketmq.core;

import com.nhb.common.rocketmq.annotation.RocketMQConsumer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 11:13
 * @description:  被RocketMQConsumer注解的方法
 */
@Data
@AllArgsConstructor
public class ConsumerMethod {
    private Object bean;
    private Method method;
    private RocketMQConsumer annotation;
}
