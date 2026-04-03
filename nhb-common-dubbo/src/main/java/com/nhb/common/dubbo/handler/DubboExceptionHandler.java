package com.nhb.common.dubbo.handler;

import com.nhb.common.core.domain.ResultMessage;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.dubbo.properties.DubboCustomProperties;
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
        log.error("Dubbo RPC Happen Error: {}", e.getMessage(),e);
        DubboCustomProperties dubboCustomProperties = SpringContextUtil.getBean(DubboCustomProperties.class);
        return ResultMessage.fail(dubboCustomProperties.getFailMessage());
    }
}
