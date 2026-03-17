package com.nhb.common.rocketmq.core;

import com.nhb.common.rocketmq.annotation.RocketMQConsumer;
import com.nhb.common.rocketmq.enums.ConsumeMode;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 11:15
 * @description: 消费者分组信息
 */
@Data
public class ConsumerMethodGroup {
    /**
     * 主题
     */
    private String topic;
    /**
     * 消费者组
     */
    private String consumerGroup;
    /**
     * 精确tag -> method
     */
    private Map<String, ConsumerMethod> tagMethods = new HashMap<>();
    /**
     * tag="*"的方法，最多一个
     */
    private ConsumerMethod wildcardMethod;
    /**
     * 消费模式
     */
    private ConsumeMode consumeMode;

    /**
     * 仅两个参数的构造函数
     * @param topic            主题
     * @param consumerGroup    消费者组
     */
    public ConsumerMethodGroup(String topic, String consumerGroup) {
        this.topic = topic;
        this.consumerGroup = consumerGroup;
    }

    /**
     * 添加被RocketMQConsumer注解的方法
     * @param bean          容器中Bean对象
     * @param method        RocketMQConsumer注解的方法
     * @param annotation    RocketMQConsumer注解
     */
    public synchronized void addMethod(Object bean, Method method, RocketMQConsumer annotation) {
        // 检查消费模式一致性
        if (consumeMode == null) {
            consumeMode = annotation.consumeMode();
        } else if (consumeMode != annotation.consumeMode()) {
            throw new IllegalArgumentException("Methods in same group (topic=" + topic + ", group=" + consumerGroup +
                    ") must have same consumeMode, but found " + consumeMode + " and " + annotation.consumeMode());
        }
        ConsumerMethod cm = new ConsumerMethod(bean, method, annotation);
        String tagExpr = annotation.tags().trim();
        if ("*".equals(tagExpr)) {
            if (wildcardMethod != null) {
                throw new IllegalStateException("Duplicate wildcard method for topic=" + topic + ", group=" + consumerGroup);
            }
            wildcardMethod = cm;
        } else {
            // 按 "||" 分割得到具体tag列表
            String[] tags = tagExpr.split("\\|\\|");
            Set<String> tagSet = new HashSet<>();
            for (String tag : tags) {
                String trimmed = tag.trim();
                if (trimmed.isEmpty()) continue;
                if (!tagSet.add(trimmed)) {
                    throw new IllegalArgumentException("Duplicate tag '" + trimmed + "' in expression: " + tagExpr);
                }
            }
            // 检查每个tag是否已被其他方法占用
            for (String tag : tagSet) {
                ConsumerMethod existing = tagMethods.get(tag);
                if (existing != null && existing != cm) {
                    throw new IllegalStateException("Tag '" + tag + "' is already handled by method: " + existing.getMethod().getName());
                }
            }
            // 建立tag->方法的映射（一个方法可能对应多个tag）
            for (String t : tagSet) {
                tagMethods.put(t, cm);
            }
        }
    }
}
