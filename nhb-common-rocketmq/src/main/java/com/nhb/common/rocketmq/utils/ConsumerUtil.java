package com.nhb.common.rocketmq.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.common.ServiceState;

import java.lang.reflect.Field;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/13 15:59
 * @description:
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsumerUtil {
    /**
     * 获取当前PUSH Consumer 状态
     * @param consumer
     * @return
     */
    public static ServiceState getConsumerState(DefaultMQPushConsumer consumer) {
        try {
            // 1. 获取 defaultMQPushConsumerImpl 字段
            Field implField = DefaultMQPushConsumer.class.getDeclaredField("defaultMQPushConsumerImpl");
            implField.setAccessible(true);
            Object impl = implField.get(consumer);

            // 2. 获取 serviceState 字段
            Field stateField = impl.getClass().getDeclaredField("serviceState");
            stateField.setAccessible(true);
            return(ServiceState) stateField.get(impl);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    /**
     * 判断是否已启动
     * @param consumer
     * @return
     */
    public static boolean isStarted(DefaultMQPushConsumer consumer) {
        ServiceState state = getConsumerState(consumer);
        return ServiceState.RUNNING.equals(state);
    }
}
