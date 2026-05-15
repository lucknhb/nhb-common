package com.nhb.common.rocketmq.properties;

import com.nhb.common.rocketmq.enums.ConverterType;
import com.nhb.common.rocketmq.constant.RocketMQConstants;
import lombok.Data;
import org.apache.rocketmq.remoting.netty.TlsSystemConfig;
import org.apache.rocketmq.remoting.protocol.heartbeat.MessageModel;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import static org.apache.rocketmq.client.ClientConfig.SEND_MESSAGE_WITH_VIP_CHANNEL_PROPERTY;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/13 9:47
 * @description: RocketMQ配置
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = RocketMQConfigProperties.PREFIX)
public class RocketMQConfigProperties {
    public static final String PREFIX = "rocketmq";
    /**
     * 是否开启使用 spring.profiles.active 拼接到topic后缀<BR/>
     * 为true时  topic_dev/topic_test......
     */
    private Boolean profileEnabled = true;
    /**
     * 消息内容序列化方式
     */
    private ConverterType converterType = ConverterType.FORY;

    private ProducerConfig producer;

    private ConsumerConfig consumer;


    /**
     * 生产者配置
     */
    @Data
    public static class ProducerConfig{
        /**
         * 服务地址
         */
        private String nameServerAddress;
        /**
         * 生产者组
         */
        private String producerGroup = RocketMQConstants.DEFAULT_PRODUCER_GROUP;
        /**
         * 发送超时时间
         */
        private Integer sendMessageTimeout = 3000;
        /**
         * 实例名称
         */
        private String instanceName = RocketMQConstants.DEFAULT_INSTANCE_NAME;
        /**
         * 是否开启VIP通道
         */
        private Boolean vipChannelEnabled = Boolean.parseBoolean(System.getProperty(SEND_MESSAGE_WITH_VIP_CHANNEL_PROPERTY, "false"));
        /**
         * topic队列数量
         */
        private Integer topicQueueNumber = 4;
        /**
         * 是否使用TLS
         */
        private Boolean useTLS = TlsSystemConfig.tlsEnable;
        /**
         * 当设置为 true 时，客户端在拉取消息等核心操作中会启用单元化相关的处理逻辑
         */
        private Boolean unitMode = false;
        /**
         * 消费者将只会消费那些发送时指定了相同单元名称的消息
         */
        private String unitName;

    }

    /**
     * 消费者配置
     */
    @Data
    public static class ConsumerConfig{
        /**
         * 服务地址
         */
        private String nameServerAddress;
        /**
         * 消费组
         */
        private String consumerGroup = RocketMQConstants.DEFAULT_CONSUMER_GROUP;
        /**
         * 实例名称
         */
        private String instanceName = RocketMQConstants.DEFAULT_INSTANCE_NAME;
        /**
         * 消费超时时间 minutes
         */
        private Integer consumeTimeout = 15;
        /**
         * 最大重试次数 PUSH 模式下有效
         */
        private Integer maxReconsumeTimes = 3;
        /**
         * 消费模式,默认为集群模式
         */
        private MessageModel consumeMessageModel = MessageModel.CLUSTERING;
        /**
         * 当设置为 true 时，客户端在拉取消息等核心操作中会启用单元化相关的处理逻辑
         */
        private Boolean unitMode = false;
        /**
         * 消费者将只会消费那些发送时指定了相同单元名称的消息
         */
        private String unitName;
        /**
         * PULL模式下拉取超时时间
         */
        private Long consumerPullTimeoutMillis = 1000 * 10L;
        /**
         * 是否开启VIP通道
         */
        private Boolean vipChannelEnabled = Boolean.parseBoolean(System.getProperty(SEND_MESSAGE_WITH_VIP_CHANNEL_PROPERTY, "false"));
    }
}
