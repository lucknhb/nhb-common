package com.nhb.common.nacos.utils;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/6 9:36
 * @description:
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NacosUtil {
    /**
     * 创建配置服务
     * @param serverAddr 服务地址
     */
    public static ConfigService createConfigService(String serverAddr) throws NacosException {
        return NacosFactory.createConfigService(serverAddr);
    }

    /**
     * 创建服务地址
     * @param properties 配置
     */
    public static ConfigService createConfigService(Properties properties) throws NacosException {
        return NacosFactory.createConfigService(properties);
    }

    /**
     * 创建服务发现
     * @param serverAddr 服务地址
     */
    public static NamingService createNamingService(String serverAddr) throws NacosException {
        return NacosFactory.createNamingService(serverAddr);
    }

    /**
     * 创建服务发现
     * @param properties 配置
     */
    public static NamingService createNamingService(Properties properties) throws NacosException {
        return NacosFactory.createNamingService(properties);
    }
}
