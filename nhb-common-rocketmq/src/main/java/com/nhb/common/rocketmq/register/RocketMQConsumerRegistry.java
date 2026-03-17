package com.nhb.common.rocketmq.register;

import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.rocketmq.annotation.RocketMQConsumer;
import com.nhb.common.rocketmq.converter.MessageConverter;
import com.nhb.common.rocketmq.core.ConsumerMethod;
import com.nhb.common.rocketmq.core.ConsumerMethodGroup;
import com.nhb.common.rocketmq.enums.ConsumeMode;
import com.nhb.common.rocketmq.factory.RocketMQFactory;
import com.nhb.common.rocketmq.properties.RocketMQConfigProperties;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 9:58
 * @description: 根据注解生成消费者的注册器
 */
@Slf4j
@RequiredArgsConstructor
public class RocketMQConsumerRegistry {
    /**
     * 消息转换器
     */
    private final MessageConverter messageConverter;
    /**
     * 消费者配置信息
     */
    private final RocketMQConfigProperties.ConsumerConfig consumerConfig;
    /**
     * 消费方法
     */
    private final Map<String, ConsumerMethodGroup> groupMap = new HashMap<>();
    /**
     * 存储消费者
     */
    private final Map<String, DefaultMQPushConsumer> consumers = new ConcurrentHashMap<>();

    /**
     * 注册被@RocketMQConsumer注解的方法
     * @param bean          容器中Bean对象
     * @param method        被@RocketMQConsumer注解的方法
     * @param annotation    @RocketMQConsumer 注解
     */
    public void registerConsumerMethod(Object bean, Method method, RocketMQConsumer annotation) {
        //当没有设置消费者组时，使用配置中的默认消费者组
        String key = annotation.topic() + ":" + StringUtil.defaultIfBlank(annotation.consumerGroup(),consumerConfig.getConsumerGroup());
        ConsumerMethodGroup group = groupMap.computeIfAbsent(key, k -> new ConsumerMethodGroup(annotation.topic(), StringUtil.defaultIfBlank(annotation.consumerGroup(),consumerConfig.getConsumerGroup())));
        group.addMethod(bean, method, annotation);
    }

    /**
     * 启动所有消费者（在ContextRefreshedEvent中调用）
     * @throws MQClientException  异常信息
     */
    public void startConsumers() throws MQClientException {
        for (ConsumerMethodGroup group : groupMap.values()) {
            createAndStartConsumerForGroup(group);
        }
    }

    /**
     * 注册并且启动消费者
     * @param group       实际注解方法
     * @throws MQClientException   异常信息
     */
    private void createAndStartConsumerForGroup(ConsumerMethodGroup group) throws MQClientException {
        //根据注解上的信息 生成消费者
        DefaultMQPushConsumer pushConsumer = RocketMQFactory.createPushConsumer(consumerConfig);
        String consumerGroup = group.getConsumerGroup();
        //如果注解上消费者组不为空则使用注解上的值
        if (StringUtil.isNotBlank(consumerGroup)) {
            pushConsumer.setConsumerGroup(consumerGroup);
        }
        String topic = group.getTopic();
        // 构建订阅tag表达式
        String tags;
        if (group.getWildcardMethod() != null) {
            tags = "*";  // 通配符覆盖所有
        } else {
            tags = String.join(" || ", group.getTagMethods().keySet());
        }
        pushConsumer.subscribe(topic, tags);
        // 根据消费模式设置监听器
        if (group.getConsumeMode() == ConsumeMode.CONCURRENTLY) {
            pushConsumer.registerMessageListener((MessageListenerConcurrently) (messages, context) -> {
                try {
                    dispatchMessage(group, messages.getFirst());
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                } catch (Exception e) {
                    log.error("CONCURRENTLY RocketMQ Handle Fail.The Error Message Is:{}",e.getMessage(), e);
                    // 可根据业务决定是否重试
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }
            });
        } else {
            pushConsumer.registerMessageListener((MessageListenerOrderly) (messages, context) -> {
                try {
                    dispatchMessage(group, messages.getFirst());
                    return ConsumeOrderlyStatus.SUCCESS;
                } catch (Exception e) {
                    log.error("ORDERLY RocketMQ Handle Fail.The Error Message Is:{}",e.getMessage(), e);
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                }
            });
        }
        pushConsumer.start();
        consumers.put(group.getTopic() + ":" + group.getConsumerGroup(), pushConsumer);
    }

    /**
     * 分发处理任务
     * @param group
     * @param msg
     * @throws Exception
     */
    private void dispatchMessage(ConsumerMethodGroup group, MessageExt msg) throws Exception {
        String tag = msg.getTags();
        ConsumerMethod method = group.getTagMethods().get(tag);
        if (method == null) {
            method = group.getWildcardMethod();  // 尝试用通配方法
        }
        if (method == null) {
            // 无匹配方法，记录日志并直接返回（消费成功，避免阻塞）
            log.warn("No Consumer Method Found For Tag: {}, Topic: {}, Group: {}", tag, group.getTopic(), group.getConsumerGroup());
            return;
        }
        invokeMethod(method, msg);
    }

    /**
     * 执行处理方法
     * @param method       需要执行的方法
     * @param msg          rocketMQ推送多来的数据信息
     * @throws Exception
     */
    private void invokeMethod(ConsumerMethod method, MessageExt msg) throws Exception {
        Method targetMethod = method.getMethod();
        Object bean = method.getBean();
        Object[] args = buildArguments(targetMethod, msg);
        targetMethod.invoke(bean, args);
    }

    /**
     * 转换参数
     * @param method
     * @param messageExt
     * @return
     * @throws Exception
     */
    private Object[] buildArguments(Method method, MessageExt messageExt) throws Exception {
        // 获取方法参数类型
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        // 遍历参数，根据类型注入
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            if (MessageExt.class.isAssignableFrom(paramType)) {
                args[i] = messageExt;
            } else if (byte[].class.isAssignableFrom(paramType)) {
                args[i] = messageExt.getBody();
            } else if (String.class.isAssignableFrom(paramType)) {
                args[i] = new String(messageExt.getBody(), StandardCharsets.UTF_8);
            } else {
                //只转换第一个非元数据类型，且仅当只有一个这样的参数
                args[i] = messageConverter.fromMessage(messageExt.getBody(), paramType);
            }
        }
        return args;
    }

    /**
     * 销毁所有消费者
     */
    @PreDestroy
    public void destroy() {
        for (DefaultMQPushConsumer pushConsumer : consumers.values()) {
            log.info("Strat ShutDown The Consumer :{}", pushConsumer);
            pushConsumer.shutdown();
            log.info("End ShutDown The Consumer :{}", pushConsumer);
        }
    }
}
