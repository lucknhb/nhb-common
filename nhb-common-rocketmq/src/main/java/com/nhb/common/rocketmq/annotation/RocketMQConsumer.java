package com.nhb.common.rocketmq.annotation;

import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;

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
    String consumerGroup();
    /**
     * 消费超时时间 minutes
     */
    int consumeTimeout() default -1;
    /**
     * 最大重试次数
     */
    int maxReconsumeTimes() default 16;
    /**
     * 最大线程数
     */
    int consumeMaxThread() default -1;
    /**
     * 最大拉取数量 仅在ConsumerType=PULL时有效
     */
    int maxPullNum() default 16;
    /**
     * 拉取间隔 建议大于5秒避免重复拉取
     */
    int pullNextDelayTimeMillis() default 10 * 1000;
    /**
     * 消费者第一次消费的时间点
     */
    String consumeTimestamp() default "";
    /**
     * 消费模式,默认为集群模式
     */
    MessageModel consumeMessageModel() default MessageModel.CLUSTERING;
    /**
     * Whether the unit of subscription group
     */
    boolean unitMode() default false;

    String unitName() default "";

    /**
     * The socket timeout in milliseconds
     */
    long consumerPullTimeoutMillis() default 1000 * 10;

    boolean vipChannelEnabled() default false;

    boolean repeatCheck() default false;

    boolean withProfile() default true;
}
