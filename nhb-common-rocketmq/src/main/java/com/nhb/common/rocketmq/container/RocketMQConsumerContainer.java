package com.nhb.common.rocketmq.container;

import com.nhb.common.rocketmq.enums.ConsumerType;
import com.nhb.common.rocketmq.factory.RocketMQFactory;
import com.nhb.common.rocketmq.properties.RocketMQConfigProperties;
import com.nhb.common.rocketmq.utils.ConsumerUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.exception.MQClientException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/13 9:54
 * @description: 存储push/pull消费者容器
 */
@Slf4j
@Getter
public class RocketMQConsumerContainer {
    //PUSH模式消费者 一般情况下/默认情况下使用 PUSH模式
    private final ConcurrentHashMap<String, DefaultMQPushConsumer> pushConsumersMap = new ConcurrentHashMap<>();
    //PULL模式消费者
    private final ConcurrentHashMap<String, DefaultLitePullConsumer> pullConsumersMap = new ConcurrentHashMap<>();

    /**
     * 启动PUSH模式消费者
     *
     * @param pushConsumer PUSH模式消费者
     */
    private void start(DefaultMQPushConsumer pushConsumer) {
        try {
            pushConsumer.start();
        } catch (MQClientException e) {
            log.error("RocketMQ [PUSH] Consumer Start Fail", e);
        }
    }

    /**
     * 启动PULL模式消费者
     *
     * @param pullConsumer PULL模式消费者
     */
    private void start(DefaultLitePullConsumer pullConsumer) {
        try {
            pullConsumer.start();
        } catch (MQClientException e) {
            log.error("Rocketmq [PULL] Consumer Start Fail", e);
        }
    }

    /**
     * 启动所有消费者
     */
    public void startAll() {
        for (Map.Entry<String, DefaultLitePullConsumer> entry : pullConsumersMap.entrySet()) {
            start(entry.getValue());
            log.info("Start PULL Rocketmq Consumer:{}", entry.getValue());
        }
        for (Map.Entry<String, DefaultMQPushConsumer> entry : pushConsumersMap.entrySet()) {
            start(entry.getValue());
            log.info("Start PUSH Rocketmq Consumer:{}", entry.getValue());
        }
    }

    /**
     * 关闭所有消费者
     */
    public void doShutdownAll() {
        for (Map.Entry<String, DefaultLitePullConsumer> entry : pullConsumersMap.entrySet()) {
            entry.getValue().shutdown();
            log.info("Shutdown PULL Rocketmq Consumer:{}", entry.getKey());
        }
        for (Map.Entry<String, DefaultMQPushConsumer> entry : pushConsumersMap.entrySet()) {
            entry.getValue().shutdown();
            log.info("Shutdown PUSH Rocketmq Consumer:{}", entry.getKey());
        }
    }

    /**
     * 注册消费者
     *
     * @param consumerConfig  配置项
     * @param consumerType    消费者类型
     * @param messageListener 监听器
     */
    public void registerConsumer(RocketMQConfigProperties.ConsumerConfig consumerConfig, ConsumerType consumerType, MessageListener messageListener, String topic, String tags) {
        //TODO 使用消费者组进行区分是否范围太大？
        if (pushConsumersMap.containsKey(consumerConfig.getConsumerGroup()) || pullConsumersMap.containsKey(consumerConfig.getConsumerGroup())) {
            log.info("Consumer Group [{}] Already Exist", consumerConfig.getConsumerGroup());
            return;
        }
        if (Objects.requireNonNull(consumerType) == ConsumerType.PULL) {
            DefaultLitePullConsumer pullConsumer = RocketMQFactory.createLitePullConsumer(consumerConfig);
            try {
                pullConsumer.subscribe(topic, StringUtils.defaultIfBlank(tags,"*"));
            } catch (MQClientException e) {
                throw new RuntimeException(e);
            }
            pullConsumersMap.put(pullConsumer.getConsumerGroup(), pullConsumer);
            //未启动的话 顺带启动
            if (!pullConsumer.isRunning()) {
                start(pullConsumer);
            }
        } else {
            DefaultMQPushConsumer pushConsumer = RocketMQFactory.createPushConsumer(consumerConfig, messageListener);
            try {
                pushConsumer.subscribe(topic, StringUtils.defaultIfBlank(tags,"*"));
                pushConsumersMap.put(consumerConfig.getConsumerGroup(), pushConsumer);
                if (ConsumerUtil.isStarted(pushConsumer)) {
                    start(pushConsumer);
                }
            } catch (MQClientException e) {
                log.error(e.getErrorMessage(), e);
            }
        }
    }


}
