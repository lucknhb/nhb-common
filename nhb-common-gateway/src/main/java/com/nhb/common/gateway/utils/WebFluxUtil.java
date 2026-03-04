package com.nhb.common.gateway.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import java.util.Set;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/4 16:49
 * @description:
 */
public class WebFluxUtil {
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    /**
     * 获取参数值
     * @param request 请求对象
     * @param paramName 请求参数名称
     * @return 参数值
     */
    public static String getParamValue(ServerHttpRequest request, String paramName) {
        // 从header中获取
        String paramValue = request.getHeaders().getFirst(paramName);
        // 从参数中获取
        if (StringUtils.isEmpty(paramValue)) {
            paramValue = request.getQueryParams().getFirst(paramName);
        }
        return StringUtils.isEmpty(paramValue) ? "" : paramValue.trim();
    }

    /**
     * 获取请求路径URL
     * @param request 请求对象
     * @return 路径URL
     */
    public static String getRequestUrl(ServerHttpRequest request) {
        return request.getPath().pathWithinApplication().value();
    }

    /**
     * 获取主机
     * @param request 请求对象
     * @return 主机
     */
    public static String getHost(ServerHttpRequest request) {
        return request.getURI().getHost();
    }

    /**
     * 获取请求格式
     * @param request 请求对象
     * @return 请求格式
     */
    public static MediaType getContentType(ServerHttpRequest request) {
        String value = request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        return StringUtils.isNotBlank(value) ? MediaType.parseMediaType(value) : null;
    }

    /**
     * 获取请求方法
     * @param request 请求对象
     * @return 请求方法
     */
    public static String getMethodName(ServerHttpRequest request) {
        return request.getMethod().name();
    }

    /**
     * 路径匹配
     * @param requestURL 请求路径URL
     * @param urls       URL集合
     * @return 匹配结果
     */
    public static boolean pathMatcher(String requestURL, Set<String> urls) {
        if (CollectionUtils.isEmpty(urls)) {
            return false;
        }
        for (String url : urls) {
            if (pathMatcher(url, requestURL)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 路径匹配.
     * @param requestURL 请求路径URL
     * @param url 路径URL
     * @return 匹配结果
     */
    private static boolean pathMatcher(String url, String requestURL) {
        return ANT_PATH_MATCHER.match(url, requestURL);
    }
}
