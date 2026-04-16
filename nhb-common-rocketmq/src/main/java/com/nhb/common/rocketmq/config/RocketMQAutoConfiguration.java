package com.nhb.common.rocketmq.config;

import com.nhb.common.fory.factory.ForyFactory;
import com.nhb.common.rocketmq.converter.MessageConverter;
import com.nhb.common.rocketmq.converter.impl.ForyMessageConverter;
import com.nhb.common.rocketmq.factory.RocketMQFactory;
import com.nhb.common.rocketmq.hook.TopicSuffixRPCHook;
import com.nhb.common.rocketmq.processor.ConsumerAnnotationBeanPostProcessor;
import com.nhb.common.rocketmq.properties.RocketMQConfigProperties;
import com.nhb.common.rocketmq.register.RocketMQConsumerRegistry;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
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

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer(RocketMQConfigProperties rocketMQConfigProperties) {
        if (Boolean.TRUE.equals(rocketMQConfigProperties.getProfileEnabled())) {
            TopicSuffixRPCHook rpcHook = new TopicSuffixRPCHook();
        }else {
            DefaultMQProducer defaultMQProducer = RocketMQFactory.createNormalProducer(rocketMQConfigProperties.getProducer(),null);
        }
        return defaultMQProducer;
    }

    /**
     * fory序列化工具
     *
     * @return fory实例
     */
    @Bean
    public ForyFactory foryFactory() {
        return ForyFactory.INSTANCE;
    }

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public ForyMessageConverter foryMessageConverter(ForyFactory foryFactory) {
        return new ForyMessageConverter(foryFactory);
    }

    @Bean
    public RocketMQConsumerRegistry rocketMQConsumerRegistry(MessageConverter messageConverter,
                                                             RocketMQConfigProperties rocketMQConfigProperties) {
        return new RocketMQConsumerRegistry(messageConverter, rocketMQConfigProperties.getConsumer());
    }

    @Bean
    public ConsumerAnnotationBeanPostProcessor consumerAnnotationBeanPostProcessor(ApplicationContext applicationContext,
                                                                                   RocketMQConsumerRegistry rocketMQConsumerRegistry) {
        return new ConsumerAnnotationBeanPostProcessor(applicationContext, rocketMQConsumerRegistry);
    }
}
