package com.nhb.common.dubbo.handler;

import com.nhb.common.core.domain.ResultMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/27 14:50
 * @description: 异常类处理
 */
@Slf4j
@RestControllerAdvice
public class DubboExceptionHandler {
    /**
     * RPC异常拦截提示
     */
    @ExceptionHandler(RpcException.class)
    public ResultMessage<Void> handleDubboException(RpcException e) {
        log.error("RPC happen error: {}", e.getMessage(),e);
        return ResultMessage.failed("服务处理异常,请联系管理员");
    }
}
