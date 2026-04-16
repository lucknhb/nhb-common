package com.nhb.common.rocketmq.factory;

import com.nhb.common.rocketmq.listener.DefaultRocketMQTransactionListener;
import com.nhb.common.rocketmq.properties.RocketMQConfigProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageQueueListener;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.remoting.RPCHook;

import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/13 11:11
 * @description: RocketMQ工厂类
 */
@Slf4j
public class RocketMQFactory {

    /**
     * 设置生产者属性
     *
     * @param producer       生产者
     * @param producerConfig 属性
     * @param <T>            配置后生产者
     */
    private static <T extends DefaultMQProducer> void setProducerProperties(T producer, RocketMQConfigProperties.ProducerConfig producerConfig) {
        producer.setNamesrvAddr(producerConfig.getNameServerAddress());
        producer.setProducerGroup(producerConfig.getProducerGroup());
        producer.setInstanceName(producerConfig.getInstanceName());
        producer.setSendMsgTimeout(producerConfig.getSendMessageTimeout());
        producer.setSendMessageWithVIPChannel(producerConfig.getVipChannelEnabled());
        producer.setDefaultTopicQueueNums(producerConfig.getTopicQueueNumber());
        producer.setUseTLS(producerConfig.getUseTLS());
        producer.setUnitMode(producerConfig.getUnitMode());
        producer.setUnitName(producerConfig.getUnitName());
    }

    /**
     * DefaultMQProducer 生产者
     *
     * @param producerConfig 可用配置属性
     * @return 生产者
     */
    public static DefaultMQProducer createNormalProducer(RocketMQConfigProperties.ProducerConfig producerConfig, RPCHook rpcHook) {
        DefaultMQProducer defaultMQProducer = null;
        if (Objects.isNull(rpcHook)) {
            defaultMQProducer = new DefaultMQProducer(producerConfig.getProducerGroup());
        } else {
            defaultMQProducer = new DefaultMQProducer(producerConfig.getProducerGroup(), rpcHook);
        }

        setProducerProperties(defaultMQProducer, producerConfig);
        return defaultMQProducer;
    }

    /**
     * 创建事务类型生产者
     *
     * @param producerConfig      可用配置属性
     * @param transactionListener 事务监听器
     * @return 事务生产者
     */
    public static TransactionMQProducer createTransactionProducer(RocketMQConfigProperties.ProducerConfig producerConfig, TransactionListener transactionListener) {
        TransactionMQProducer transactionMQProducer = new TransactionMQProducer(producerConfig.getProducerGroup());
        setProducerProperties(transactionMQProducer, producerConfig);
        if (transactionListener == null) {
            transactionMQProducer.setTransactionListener(new DefaultRocketMQTransactionListener());
        }
        return transactionMQProducer;
    }

    /**
     * 创建PULL消费者
     *
     * @param consumerConfig PULL 消费者
     * @return
     */
    public static DefaultLitePullConsumer createLitePullConsumer(RocketMQConfigProperties.ConsumerConfig consumerConfig) {
        DefaultLitePullConsumer pullConsumer = new DefaultLitePullConsumer();
        pullConsumer.setConsumerGroup(consumerConfig.getConsumerGroup());
        pullConsumer.setInstanceName(consumerConfig.getInstanceName());
        if (consumerConfig.getConsumerPullTimeoutMillis() > 0) {
            pullConsumer.setConsumerPullTimeoutMillis(consumerConfig.getConsumerPullTimeoutMillis());
        }
        pullConsumer.setMessageModel(consumerConfig.getConsumeMessageModel());
        pullConsumer.setUnitMode(consumerConfig.getUnitMode());
        if (StringUtils.isNotBlank(consumerConfig.getUnitName())) {
            pullConsumer.setUnitName(consumerConfig.getUnitName());
        }
        pullConsumer.setVipChannelEnabled(consumerConfig.getVipChannelEnabled());
        pullConsumer.setNamesrvAddr(consumerConfig.getNameServerAddress());
        return pullConsumer;
    }

    /**
     * 创建PULL消费者
     *
     * @param consumerConfig       PULL 消费者
     * @param messageQueueListener 监听器
     * @return
     */
    public static DefaultLitePullConsumer createLitePullConsumer(RocketMQConfigProperties.ConsumerConfig consumerConfig, MessageQueueListener messageQueueListener) {
        DefaultLitePullConsumer pullConsumer = createLitePullConsumer(consumerConfig);
        if (messageQueueListener != null) {
            pullConsumer.setMessageQueueListener(messageQueueListener);
        }
        return pullConsumer;
    }

    /**
     * 创建PUSH 模式消费者<BR/>
     * ★★ 切记该方式创建出来的消费者未设置监听器 ★★
     *
     * @param consumerConfig 配置项
     * @return
     */
    public static DefaultMQPushConsumer createPushConsumer(RocketMQConfigProperties.ConsumerConfig consumerConfig) {
        DefaultMQPushConsumer pushConsumer = new DefaultMQPushConsumer();
        pushConsumer.setConsumerGroup(consumerConfig.getConsumerGroup());
        pushConsumer.setInstanceName(consumerConfig.getInstanceName());
        if (consumerConfig.getMaxReconsumeTimes() > 0) {
            pushConsumer.setMaxReconsumeTimes(consumerConfig.getMaxReconsumeTimes());
        }
        if (consumerConfig.getConsumeTimeout() > 0) {
            pushConsumer.setConsumeTimeout(consumerConfig.getConsumeTimeout());
        }
        pushConsumer.setUnitMode(consumerConfig.getUnitMode());
        if (StringUtils.isNotBlank(consumerConfig.getUnitName())) {
            pushConsumer.setUnitName(consumerConfig.getUnitName());
        }
        pushConsumer.setMessageModel(consumerConfig.getConsumeMessageModel());
        pushConsumer.setVipChannelEnabled(consumerConfig.getVipChannelEnabled());
        pushConsumer.setNamesrvAddr(consumerConfig.getNameServerAddress());
        return pushConsumer;
    }

    /**
     * 创建PUSH 模式消费者<BR/>
     *
     * @param consumerConfig  配置项
     * @param messageListener 监听器
     * @return
     */
    public static DefaultMQPushConsumer createPushConsumer(RocketMQConfigProperties.ConsumerConfig consumerConfig, MessageListener messageListener) {
        DefaultMQPushConsumer pushConsumer = createPushConsumer(consumerConfig);
        if (messageListener != null) {
            if (messageListener instanceof MessageListenerConcurrently) {
                pushConsumer.registerMessageListener((MessageListenerConcurrently) messageListener);
            } else {
                pushConsumer.registerMessageListener((MessageListenerOrderly) messageListener);
            }
        }
        return pushConsumer;
    }
}
