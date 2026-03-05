package com.nhb.common.gateway.utils;

import cn.hutool.core.util.ObjectUtil;
import com.nhb.common.core.domain.ResultMessage;
import com.nhb.common.core.utils.JacksonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ORIGINAL_REQUEST_URL_ATTR;

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

    /**
     * 获取原请求路径
     */
    public static String getOriginalRequestUrl(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        LinkedHashSet<URI> uris = exchange.getAttributeOrDefault(GATEWAY_ORIGINAL_REQUEST_URL_ATTR, new LinkedHashSet<>());
        URI requestUri = uris.stream().findFirst().orElse(request.getURI());
        return UriComponentsBuilder.fromPath(requestUri.getRawPath()).build().toUriString();
    }

    /**
     * 是否是Json请求
     *
     * @param exchange HTTP请求
     */
    public static boolean isJsonRequest(ServerWebExchange exchange) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        return StringUtils.startsWithIgnoreCase(header, MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * 读取request内的body
     *
     * 注意一个request只能读取一次 读取之后需要重新包装
     */
    public static String resolveBodyFromRequest(ServerHttpRequest serverHttpRequest) {
        // 获取请求体
        Flux<DataBuffer> body = serverHttpRequest.getBody();
        AtomicReference<String> bodyRef = new AtomicReference<>();
        body.subscribe(buffer -> {
            try (DataBuffer.ByteBufferIterator iterator = buffer.readableByteBuffers()) {
                CharBuffer charBuffer = StandardCharsets.UTF_8.decode(iterator.next());
                DataBufferUtils.release(buffer);
                bodyRef.set(charBuffer.toString());
            }
        });
        return bodyRef.get();
    }

    /**
     * 从缓存中读取request内的body
     *
     * 注意要求经过 {@link ServerWebExchangeUtils#cacheRequestBody(ServerWebExchange, Function)} 此方法创建缓存
     * 框架内已经使用 {@link com.nhb.common.gateway.filter.WebFluxCacheRequestFilter} 全局创建了body缓存
     *
     * @return body
     */
    public static String resolveBodyFromCacheRequest(ServerWebExchange exchange) {
        Object obj = exchange.getAttributes().get(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
        if (ObjectUtil.isNull(obj)) {
            return null;
        }
        DataBuffer buffer = (DataBuffer) obj;
        try (DataBuffer.ByteBufferIterator iterator = buffer.readableByteBuffers()) {
            StringBuilder sb = new StringBuilder();
            iterator.forEachRemaining(e -> {
                sb.append(StandardCharsets.UTF_8.decode(e));
            });
            return sb.toString();
        }
    }

    /**
     * 设置webflux模型响应
     *
     * @param response ServerHttpResponse
     * @param value    响应内容
     * @return Mono<Void>
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, Object value) {
        return webFluxResponseWriter(response, HttpStatus.OK, value, ResultMessage.FAIL);
    }

    /**
     * 设置webflux模型响应
     *
     * @param response ServerHttpResponse
     * @param code     响应状态码
     * @param value    响应内容
     * @return Mono<Void>
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, Object value, int code) {
        return webFluxResponseWriter(response, HttpStatus.OK, value, code);
    }

    /**
     * 设置webflux模型响应
     *
     * @param response ServerHttpResponse
     * @param status   http状态码
     * @param code     响应状态码
     * @param value    响应内容
     * @return Mono<Void>
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, HttpStatus status, Object value, int code) {
        return webFluxResponseWriter(response, MediaType.APPLICATION_JSON_VALUE, status, value, code);
    }

    /**
     * 设置webflux模型响应
     *
     * @param response    ServerHttpResponse
     * @param contentType content-type
     * @param status      http状态码
     * @param code        响应状态码
     * @param value       响应内容
     * @return Mono<Void>
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, String contentType, HttpStatus status, Object value, int code) {
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, contentType);
        ResultMessage<?> result = ResultMessage.fail(code, value.toString());
        DataBuffer dataBuffer = response.bufferFactory().wrap(Objects.requireNonNull(JacksonUtil.toJsonString(result)).getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }
}
