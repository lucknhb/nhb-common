package com.nhb.common.gateway.handler;

import com.nhb.common.gateway.utils.WebFluxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/5 16:43
 * @description:
 */
@Slf4j
public class GatewayExceptionHandler implements ErrorWebExceptionHandler, Ordered {

    /**
     * 处理异常并响应.
     * @param exchange 服务器网络交换机
     * @param e 异常
     * @return 响应结果
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable e) {
        ServerHttpResponse response = exchange.getResponse();
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(e);
        }
        String msg;
        if (e instanceof NotFoundException) {
            msg = "Server Not Found";
        } else if (e instanceof ResponseStatusException responseStatusException) {
            msg = responseStatusException.getMessage();
        } else {
            msg = "Server Error";
        }
        log.error("[Gateway Request Error] Url :{}, Exception Is :{}", exchange.getRequest().getPath(), e.getMessage());
        return WebFluxUtil.webFluxResponseWriter(response,determineStatus(e), msg,1);
    }


    /**
     * 处理不同状态码
     * @param ex
     * @return
     */
    private HttpStatus determineStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
        }
        // 可根据不同异常类型返回不同状态码
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    /**
     * 设置最高优先级
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
