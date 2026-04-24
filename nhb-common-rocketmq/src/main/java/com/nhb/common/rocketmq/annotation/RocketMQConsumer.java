package com.nhb.common.rocketmq.annotation;

import com.nhb.common.rocketmq.constant.RocketMQConstants;
import com.nhb.common.rocketmq.enums.ConsumeMode;

import java.lang.annotation.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/12 16:16
 * @description: RocketMQ 消费者注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RocketMQConsumer {
    /**
     * 消费模式 默认并发模式消费
     */
    ConsumeMode consumeMode() default ConsumeMode.CONCURRENTLY;
    /**
     * 需要消费的topic
     * 尽量保持一个 Topic 一种消息类型
     */
    String topic();
    /**
     * 过滤的表达式,*代表订阅所有tag,多个tag使用||连接
     */
    String tags() default "*";
    /**
     * 标识一类Consumer的集合名称
     */
    String consumerGroup() default RocketMQConstants.DEFAULT_CONSUMER_GROUP;
}
