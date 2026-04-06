package com.nhb.common.encrypt.filter;

import cn.hutool.core.util.ObjectUtil;
import com.nhb.common.core.exception.ServiceException;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.encrypt.annotation.ApiEncrypt;
import com.nhb.common.encrypt.properties.ApiEncryptProperties;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 11:27
 * @description: 加解密过滤器
 */
@Slf4j
@RequiredArgsConstructor
public class EncryptFilter implements Filter {
    private final ApiEncryptProperties apiEncryptProperties;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        HttpServletResponse servletResponse = (HttpServletResponse) response;
        // 获取加密注解
        ApiEncrypt apiEncrypt = this.getApiEncryptAnnotation(servletRequest);
        boolean responseFlag = apiEncrypt != null && apiEncrypt.response();
        boolean requestFlag = apiEncrypt != null && apiEncrypt.request();
        ServletRequest requestWrapper = null;
        ServletResponse responseWrapper = null;
        EncryptResponseBodyWrapper responseBodyWrapper = null;
        // 是否存在加密标头
        String headerValue = servletRequest.getHeader(apiEncryptProperties.getApiEncryptHeaderFlag());
        //请求处理
        if (requestFlag) {
            if (StringUtil.isNotBlank(headerValue)) {
                // 请求解密
                requestWrapper = new EncryptRequestBodyWrapper(servletRequest, apiEncryptProperties.getPrivateKey(), apiEncryptProperties.getApiEncryptHeaderFlag());
            } else {
                // 是否有注解，有就报错，没有放行
                if (ObjectUtil.isNotNull(apiEncrypt)) {
                    HandlerExceptionResolver exceptionResolver = SpringContextUtil.getBean("handlerExceptionResolver", HandlerExceptionResolver.class);
                    exceptionResolver.resolveException(
                            servletRequest, servletResponse, null,
                            new ServiceException("没有访问权限，请联系授权", HttpStatus.METHOD_NOT_ALLOWED));
                    return;
                }
            }
        }
        // 判断是否响应加密
        if (responseFlag) {
            responseBodyWrapper = new EncryptResponseBodyWrapper(servletResponse);
            responseWrapper = responseBodyWrapper;
        }
        //调用链路
        chain.doFilter(ObjectUtil.defaultIfNull(requestWrapper, request),
                ObjectUtil.defaultIfNull(responseWrapper, response));

        //响应处理
        if (responseFlag) {
            servletResponse.reset();
            // 对原始内容加密
            String encryptContent = "";
            try {
                encryptContent = responseBodyWrapper.getEncryptContent(
                        servletResponse, apiEncryptProperties.getPublicKey(), apiEncryptProperties.getApiEncryptHeaderFlag());
            } catch (Exception e) {
                HandlerExceptionResolver exceptionResolver = SpringContextUtil.getBean("handlerExceptionResolver", HandlerExceptionResolver.class);
                exceptionResolver.resolveException(
                        servletRequest, servletResponse, null, e);
            }
            // 对加密后的内容写出
            servletResponse.getWriter().write(encryptContent);
        }
    }

    /**
     * 获取 ApiEncrypt 注解
     */
    private ApiEncrypt getApiEncryptAnnotation(HttpServletRequest servletRequest) {
        RequestMappingHandlerMapping handlerMapping = SpringContextUtil.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        // 获取注解
        try {
            HandlerExecutionChain mappingHandler = handlerMapping.getHandler(servletRequest);
            if (ObjectUtil.isNotNull(mappingHandler)) {
                Object handler = mappingHandler.getHandler();
                if (ObjectUtil.isNotNull(handler)) {
                    // 从handler获取注解
                    if (handler instanceof HandlerMethod handlerMethod) {
                        return handlerMethod.getMethodAnnotation(ApiEncrypt.class);
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public void destroy() {
    }
}
