package com.nhb.common.gateway.handler;

import com.nhb.common.gateway.utils.WebFluxUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
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
@Order(-1)
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

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
        return WebFluxUtil.webFluxResponseWriter(response, msg);
    }
}
