package com.nhb.common.rocketmq.config;

import com.nhb.common.fory.factory.ForyFactory;
import com.nhb.common.rocketmq.properties.RocketMQConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/13 9:53
 * @description:
 */
@AutoConfiguration
@EnableConfigurationProperties(RocketMQConfigProperties.class)
public class RocketMQAutoConfiguration {
    /**
     * fory序列化工具
     * @return fory实例
     */
    @Bean
    public ForyFactory foryFactory() {
        return ForyFactory.INSTANCE;
    }


}
