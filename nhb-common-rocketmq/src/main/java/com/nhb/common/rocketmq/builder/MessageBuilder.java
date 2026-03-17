package com.nhb.common.rocketmq.builder;

import cn.hutool.core.collection.CollUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.rocketmq.converter.MessageConverter;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;

import java.util.Map;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/13 16:08
 * @description: 消息构建者
 */
@Data
@Accessors(chain = true)
public class MessageBuilder {
    private String topic;
    private String tags;
    private String key;
    private Object body;
    /**
     * 延迟时间 以当前时间为准往后推迟
     */
    private Long delayTime;
    /**
     * 发送时间 需发送方自己计算具体时间戳
     */
    private Long deliverTime;
    /**
     * 事务ID
     */
    private String transactionId;
    /**
     * 自定义属性
     */
    private Map<String, String> properties;


    /**
     * 创建消息体
     * @return
     */
    public Message toMessage() {
        Message message = new Message();
        message.setTopic(getTopic());
        message.setTags(getTags());
        MessageConverter messageConverter = SpringContextUtil.getBean(MessageConverter.class);
        try {
            byte[] bytes = messageConverter.toByte(getBody());
            message.setBody(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Body " + getBody() + " To Byte Error",e);
        }
        if (StringUtils.isNotBlank(getKey())) {
            message.setKeys(getKey());
        }
        if (Objects.nonNull(getDelayTime())) {
            message.setDelayTimeMs(getDelayTime());
        }
        if (Objects.nonNull(getDeliverTime())) {
            message.setDelayTimeMs(getDeliverTime());
        }
        if (Objects.nonNull(getTransactionId())) {
            message.setTransactionId(getTransactionId());
        }
        if (CollUtil.isNotEmpty(getProperties())) {
            properties.forEach(message::putUserProperty);
        }
        return message;
    }


}
