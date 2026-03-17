package com.nhb.common.rocketmq.utils;

import com.nhb.common.core.utils.SpringContextUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;

import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 11:30
 * @description: rocketMQ 发送工具类 构建发送消息体可使用 {@link com.nhb.common.rocketmq.builder.MessageBuilder}
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RocketMQClientUtil {

    private static final DefaultMQProducer PRODUCER = SpringContextUtil.getBean(DefaultMQProducer.class);

    /**
     * 发送消息
     *
     * @param message 实际发送的消息体
     * @return 发送结果
     * @throws Exception 异常信息
     */
    public static SendResult sendMessage(Message message) throws Exception {
        log.info("Send Message :{}", message);
        SendResult sendResult = PRODUCER.send(message);
        log.info("Send Message:{} Result :{}", message, sendResult);
        return sendResult;
    }

    /**
     * 发送消息
     *
     * @param message      实际发送的消息体
     * @param sendCallback 回调接口
     * @throws Exception 异常信息
     */
    public static void sendMessage(Message message, SendCallback sendCallback) throws Exception {
        log.info("Send Call Back Message :{}", message);
        PRODUCER.send(message, sendCallback);
    }


    /**
     * 发送顺序消息
     *
     * @param message               实际发送的消息体
     * @param messageQueueSelector  队列选择器 为空时默认使用 Hash方式
     * @param uniqueId              通过该值进行确定队列
     * @throws Exception 异常信息
     */
    public static SendResult sendMessageOrderly(Message message, MessageQueueSelector messageQueueSelector, Object uniqueId) throws Exception {
        log.info("Send Orderly Message :{} With {}", message, uniqueId);
        SendResult sendResult;
        if (Objects.isNull(messageQueueSelector)) {
            sendResult = PRODUCER.send(message, new SelectMessageQueueByHash(), uniqueId);
        }else {
            sendResult = PRODUCER.send(message, messageQueueSelector, uniqueId);
        }
        log.info("Send Orderly Message:{} With {} Result :{}", message, uniqueId, sendResult);
        return sendResult;
    }

    /**
     * 发送顺序消息
     *
     * @param message  实际发送的消息体
     * @param messageQueueSelector  队列选择器 为空时默认使用 Hash方式
     * @param uniqueId 通过该值进行确定队列
     * @param sendCallback 回调接口
     * @throws Exception 异常信息
     */
    public static void sendMessageOrderly(Message message,MessageQueueSelector messageQueueSelector, Object uniqueId,SendCallback sendCallback) throws Exception {
        log.info("Send Orderly Call Back Message :{} With {}", message, uniqueId);
        if (Objects.isNull(messageQueueSelector)) {
            PRODUCER.send(message, new SelectMessageQueueByHash(), uniqueId,sendCallback);
        }else {
            PRODUCER.send(message, messageQueueSelector, uniqueId,sendCallback);
        }
    }



}
