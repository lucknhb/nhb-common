package com.nhb.common.security.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.httpauth.basic.SaHttpBasicUtil;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.same.SaSameUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.security.properties.SecurityConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/4 11:47
 * @description:
 */
@Slf4j
@AutoConfiguration
@RequiredArgsConstructor
@EnableConfigurationProperties(SecurityConfigProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class SecurityWebMvcAutoConfiguration implements WebMvcConfigurer {
    private final SecurityConfigProperties securityConfigProperties;

    // 注册拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册Sa-Token的路由拦截器，并排除登录接口或其他可匿名访问的接口地址 (与注解拦截器无关)
        //gateway 进行放行后 此处也需要配置放行的话是否拦截
        log.info("Security ignoreUrl list ：{}",securityConfigProperties.getPaths());
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(securityConfigProperties.getPaths())
                .excludePathPatterns("/favicon.ico", "/actuator", "/actuator/**", "/resource/sse","/v3/api-docs");
    }

    /**
     * 校验是否从网关转发
     */
    @Bean
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .setExcludeList(securityConfigProperties.getPaths())
                .addExclude("/favicon.ico", "/actuator", "/actuator/**", "/resource/sse")
                .setAuth(obj -> {

                    if (SaManager.getConfig().getCheckSameToken()) {
                        SaSameUtil.checkCurrentRequestToken();
                    }
                })
                .setError(e -> SaResult.error("Unauthorized The Message").setCode(HttpStatus.UNAUTHORIZED.value()));
    }

    /**
     * 对 actuator 健康检查接口 做账号密码鉴权
     */
    @Bean
    public SaServletFilter actuatorFilter() {
        //TODO 可替换账号密码来源
        String username = SpringContextUtil.getProperty("spring.cloud.nacos.discovery.metadata.username");
        String password = SpringContextUtil.getProperty("spring.cloud.nacos.discovery.metadata.userpassword");
        return new SaServletFilter()
                .addInclude("/actuator", "/actuator/**")
                .setAuth(obj -> {
                    SaHttpBasicUtil.check(username + ":" + password);
                })
                .setError(e -> SaResult.error(e.getMessage()).setCode(HttpStatus.UNAUTHORIZED.value()));
    }
}
