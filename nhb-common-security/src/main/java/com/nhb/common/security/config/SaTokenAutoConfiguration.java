package com.nhb.common.security.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.jwt.StpLogicJwtForSimple;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpLogic;
import com.nhb.common.core.factory.YamlPropertySourceFactory;
import com.nhb.common.security.handler.SaTokenExceptionHandler;
import com.nhb.common.security.service.DefaultSaTokenDao;
import com.nhb.common.security.service.DefaultSaTokenPermissionService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/5 9:04
 * @description:
 */
@AutoConfiguration
@PropertySource(value = "classpath:satoken-default.yaml", factory = YamlPropertySourceFactory.class)
public class SaTokenAutoConfiguration {

    /**
     * 不同模式之间的比较 参考 <a href ="https://sa-token.cc/doc.html#/plugin/jwt-extend"/>
     * @return  Simple 简单模式 JWT
     */
    @Bean
    public StpLogic getStpLogicJwt() {
        return new StpLogicJwtForSimple();
    }

    /**
     * 自定义dao层存储
     */
    @Bean
    public SaTokenDao saTokenDao() {
        return new DefaultSaTokenDao();
    }

    @Bean
    public StpInterface stpInterface() {
        return new DefaultSaTokenPermissionService();
    }

    /**
     * 异常处理器
     */
    @Bean
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        return new SaTokenExceptionHandler();
    }
}
