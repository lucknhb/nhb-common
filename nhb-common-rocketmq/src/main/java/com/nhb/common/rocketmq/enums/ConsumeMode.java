package com.nhb.common.rocketmq.enums;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/17 8:50
 * @description: 消费模式  并发/顺序
 */
public enum ConsumeMode {
    /**
     * 并发消费
     */
    CONCURRENTLY,
    /**
     * 顺序消费
     */
    ORDERLY
}
