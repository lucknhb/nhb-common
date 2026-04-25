package com.nhb.common.rocketmq.utils;

import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.rocketmq.converter.MessageConverter;
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

    private static final MessageConverter MESSAGE_CONVERTER = SpringContextUtil.getBean(MessageConverter.class);

    /**
     * 发送消息
     *
     * @param message 实际发送的消息体
     * @return 发送结果
     * @throws Exception 异常信息
     */
    public static SendResult sendMessage(Message message) throws Exception {
        return PRODUCER.send(message);
    }

    /**
     * 发送消息
     *
     * @param message      实际发送的消息体
     * @param sendCallback 回调接口
     * @throws Exception 异常信息
     */
    public static void sendMessage(Message message, SendCallback sendCallback) throws Exception {
        PRODUCER.send(message, sendCallback);
    }


    /**
     * 发送顺序消息
     *
     * @param message              实际发送的消息体
     * @param messageQueueSelector 队列选择器 为空时默认使用 Hash方式
     * @param uniqueId             通过该值进行确定队列
     * @throws Exception 异常信息
     */
    public static SendResult sendMessageOrderly(Message message, MessageQueueSelector messageQueueSelector, Object uniqueId) throws Exception {
        SendResult sendResult;
        if (Objects.isNull(messageQueueSelector)) {
            sendResult = PRODUCER.send(message, new SelectMessageQueueByHash(), uniqueId);
        } else {
            sendResult = PRODUCER.send(message, messageQueueSelector, uniqueId);
        }
        return sendResult;
    }

    /**
     * 发送顺序消息
     *
     * @param message              实际发送的消息体
     * @param messageQueueSelector 队列选择器 为空时默认使用 Hash方式
     * @param uniqueId             通过该值进行确定队列
     * @param sendCallback         回调接口
     * @throws Exception 异常信息
     */
    public static void sendMessageOrderly(Message message, MessageQueueSelector messageQueueSelector, Object uniqueId, SendCallback sendCallback) throws Exception {
        if (Objects.isNull(messageQueueSelector)) {
            PRODUCER.send(message, new SelectMessageQueueByHash(), uniqueId, sendCallback);
        } else {
            PRODUCER.send(message, messageQueueSelector, uniqueId, sendCallback);
        }
    }

    /**
     * 获取消费生产者<BR/>
     * @return 消费生产者
     */
    public static DefaultMQProducer getProducer() {
        return PRODUCER;
    }

    /**
     * 获取消息内容序列化器
     *
     * @return 序列化器
     */
    public static MessageConverter getMessageConverter() {
        return MESSAGE_CONVERTER;
    }

    /**
     * 将消息内容转换为字节数组
     *
     * @param body 消息内容
     * @return 字节数组
     * @throws Exception 异常信息
     */
    public static byte[] convertBodyToBytes(Object body) throws Exception {
        return MESSAGE_CONVERTER.toByte(body);
    }

    /**
     * 将字节数组转换为实体类
     * @param body           字节数组
     * @param clazz          实体类类型
     * @return               实体类
     * @param <T>            实体类类型
     * @throws Exception     异常信息
     */
    public static <T> T convertBodyToObject(byte[] body, Class<T> clazz) throws Exception {
        return MESSAGE_CONVERTER.fromMessage(body, clazz);
    }


}
