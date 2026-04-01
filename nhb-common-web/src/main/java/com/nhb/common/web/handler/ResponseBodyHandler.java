package com.nhb.common.web.handler;

import cn.hutool.core.util.ObjectUtil;
import com.nhb.common.core.domain.ResultMessage;
import io.micrometer.common.lang.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/28 14:40
 * @description: 请求响应拦截处理器<BR />
 * 防止接口返回 null
 */
@Slf4j
@RestControllerAdvice
public class ResponseBodyHandler implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            String requestUri = attributes.getRequest().getRequestURI();
            // 排除 OpenAPI 相关路径
            return !requestUri.startsWith("/v3/api-docs");
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        if (body instanceof ResultMessage<?> result) {
            return result;
        }
        if (ObjectUtil.isNull(body)) {
            return ResultMessage.ok();
        } else {
            return ResultMessage.ok(body);
        }
    }
}
