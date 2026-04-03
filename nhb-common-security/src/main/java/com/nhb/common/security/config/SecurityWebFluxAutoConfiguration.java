package com.nhb.common.security.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.dev33.satoken.util.SaTokenConsts;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.security.properties.SecurityConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/4 11:41
 * @description: webFlux 环境下SaToken过滤器
 */
@AutoConfiguration
@EnableConfigurationProperties(SecurityConfigProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class SecurityWebFluxAutoConfiguration {

    /**
     * 注册 WebFlux Sa-Token 全局过滤器
     */
    @Bean
    public SaReactorFilter saReactorFilter(SecurityConfigProperties securityConfigProperties) {
        return new SaReactorFilter()
                // 拦截地址
                .addInclude("/**")
                .addExclude("/favicon.ico", "/actuator", "/actuator/**", "/resource/sse")
                // 鉴权方法：每次访问进入
                .setAuth(obj -> {
                    // 登录校验 -- 拦截所有路由
                    SaRouter.match("/**")
                            .notMatch(securityConfigProperties.getPaths())
                            .check(r -> {
                                // 检查是否登录 是否有token
                                StpUtil.checkLogin();
                            });
                }).setError(e -> {
                    ServerHttpResponse response = SaReactorSyncHolder.getExchange().getResponse();
                    response.getHeaders().set(SaTokenConsts.CONTENT_TYPE_KEY, SaTokenConsts.CONTENT_TYPE_APPLICATION_JSON);
                    if (e instanceof NotLoginException) {
                        return SaResult.error(e.getMessage()).setCode(HttpStatus.UNAUTHORIZED.value());
                    }
                    return SaResult.error("Unauthorized The Message").setCode(HttpStatus.UNAUTHORIZED.value());
                });
    }

    /**
     * 对 actuator 健康检查接口 做账号密码鉴权
     */
    @Bean
    public SaReactorFilter actuatorFilter() {
        //TODO 可替换账号密码来源
        String username = SpringContextUtil.getProperty("spring.cloud.nacos.discovery.metadata.username");
        String password = SpringContextUtil.getProperty("spring.cloud.nacos.discovery.metadata.userpassword");
        return new SaReactorFilter()
                .addInclude("/actuator", "/actuator/**")
                .setAuth(obj -> {
                    SaHttpBasicUtil.check(username + ":" + password);
                })
                .setError(e -> {
                    ServerHttpResponse response = SaReactorSyncHolder.getExchange().getResponse();
                    response.getHeaders().set(SaTokenConsts.CONTENT_TYPE_KEY, SaTokenConsts.CONTENT_TYPE_APPLICATION_JSON);
                    return SaResult.error(e.getMessage()).setCode(HttpStatus.UNAUTHORIZED.value());
                });
    }

}
