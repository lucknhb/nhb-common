package com.nhb.common.rocketmq.processor;


import com.nhb.common.rocketmq.annotation.RocketMQConsumer;
import com.nhb.common.rocketmq.register.RocketMQConsumerRegistry;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 10:08
 * @description: 扫描Bean中带@RocketMQConsumer注解的方法
 */
@RequiredArgsConstructor
public class ConsumerAnnotationBeanPostProcessor implements ApplicationListener<ContextRefreshedEvent> {
    private final ApplicationContext applicationContext;
    private final RocketMQConsumerRegistry rocketMQConsumerRegistry;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 扫描所有Bean，查找带@RocketMQConsumer注解的方法
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Method[] methods = bean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                RocketMQConsumer consumer = method.getAnnotation(RocketMQConsumer.class);
                if (Objects.nonNull(consumer)) {
                    rocketMQConsumerRegistry.registerConsumerMethod(bean, method, consumer);
                }
            }
        }
        // 启动所有消费者
        try {
            rocketMQConsumerRegistry.startConsumers();
        } catch (MQClientException e) {
            throw new RuntimeException("Failed to start RocketMQ consumers", e);
        }
    }
}
