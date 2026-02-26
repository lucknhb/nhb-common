package com.nhb.common.redis.config;

import cn.hutool.core.util.ObjectUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.redis.handler.KeyPrefixHandler;
import com.nhb.common.redis.handler.RedisExceptionHandler;
import com.nhb.common.redis.properties.RedissonProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.fory.logging.LoggerFactory;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.redisson.codec.ForyCodec;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.VirtualThreadTaskExecutor;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/26 10:48
 * @description: redisson 配置
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonConfig {

    @Bean
    public RedissonAutoConfigurationCustomizer redissonCustomizer(RedissonProperties redissonProperties) {
        return config -> {
            log.info(">>>>>>>> Start config redisson <<<<<<<<");
            //使用fory作为序列化工具
            LoggerFactory.useSlf4jLogging(true);
            ForyCodec foryCodec = new ForyCodec();
            CompositeCodec codec = new CompositeCodec(StringCodec.INSTANCE, foryCodec, foryCodec);

            config.setThreads(redissonProperties.getThreads())
                    .setNettyThreads(redissonProperties.getNettyThreads())
                    .setUseScriptCache(true) //缓存 Lua 脚本 减少网络传输(redisson 大部分的功能都是基于 Lua 脚本实现)
                    .setCodec(codec);
            if (SpringContextUtil.isVirtual()) {
                config.setNettyExecutor(new VirtualThreadTaskExecutor("redisson-"));
            }
            RedissonProperties.SingleServerProperties singleServerProperties = redissonProperties.getSingleServer();
            if (ObjectUtil.isNotNull(singleServerProperties)) {
                // 使用单机模式
                config.useSingleServer()
                        .setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix())) //设置redis key前缀
                        .setTimeout(singleServerProperties.getTimeout())
                        .setClientName(singleServerProperties.getClientName())
                        .setIdleConnectionTimeout(singleServerProperties.getIdleConnectionTimeout())
                        .setSubscriptionConnectionPoolSize(singleServerProperties.getSubscriptionConnectionPoolSize())
                        .setConnectionMinimumIdleSize(singleServerProperties.getConnectionMinimumIdleSize())
                        .setConnectionPoolSize(singleServerProperties.getConnectionPoolSize());
            }
            // 集群配置方式
            RedissonProperties.ClusterServerProperties clusterServerProperties = redissonProperties.getClusterServer();
            if (ObjectUtil.isNotNull(clusterServerProperties)) {
                config.useClusterServers()
                        .setNameMapper(new KeyPrefixHandler(redissonProperties.getKeyPrefix())) //设置redis key前缀
                        .setTimeout(clusterServerProperties.getTimeout())
                        .setClientName(clusterServerProperties.getClientName())
                        .setIdleConnectionTimeout(clusterServerProperties.getIdleConnectionTimeout())
                        .setSubscriptionConnectionPoolSize(clusterServerProperties.getSubscriptionConnectionPoolSize())
                        .setMasterConnectionMinimumIdleSize(clusterServerProperties.getMasterConnectionMinimumIdleSize())
                        .setMasterConnectionPoolSize(clusterServerProperties.getMasterConnectionPoolSize())
                        .setSlaveConnectionMinimumIdleSize(clusterServerProperties.getSlaveConnectionMinimumIdleSize())
                        .setSlaveConnectionPoolSize(clusterServerProperties.getSlaveConnectionPoolSize())
                        .setReadMode(clusterServerProperties.getReadMode())
                        .setSubscriptionMode(clusterServerProperties.getSubscriptionMode());
            }
            log.info(">>>>>>>> End config redisson <<<<<<<<");
        };
    }

    /**
     * 异常处理器
     */
    @Bean
    public RedisExceptionHandler redisExceptionHandler() {
        return new RedisExceptionHandler();
    }
}
