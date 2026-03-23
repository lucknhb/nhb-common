package com.nhb.common.sms.handler;

import cn.hutool.http.HttpStatus;
import com.nhb.common.core.domain.ResultMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.dromara.sms4j.comm.exception.SmsBlendException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/23 15:13
 * @description: SMS异常处理器
 */
@Slf4j
@RestControllerAdvice
public class SmsExceptionHandler {
    @ExceptionHandler(SmsBlendException.class)
    public ResultMessage<Void> handleSmsBlendException(SmsBlendException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("Request Uri '{}',SMS Send Happen Exception.", requestURI, e);
        return ResultMessage.fail(HttpStatus.HTTP_INTERNAL_ERROR, "短信发送失败，请稍后再试...");
    }
}
