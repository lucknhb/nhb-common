package com.nhb.common.lock.handler;

import cn.hutool.http.HttpStatus;
import com.nhb.common.core.domain.ResultMessage;
import com.nhb.common.lock.exception.LockException;
import com.nhb.common.lock.exception.LockFailureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/26 11:41
 * @description: Redisson 异常处理
 */
@Slf4j
@RestControllerAdvice
public class LockExceptionHandler {

    /**
     * 分布式锁lock异常
     */
    @ExceptionHandler(LockFailureException.class)
    public ResultMessage<Void> handleLockFailureException(LockFailureException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("Acquire Lock Fail '{}'", requestURI, e);
        return ResultMessage.fail(HttpStatus.HTTP_UNAVAILABLE, "业务处理中，请稍后再试...");
    }

    /**
     * 分布式锁lock异常
     */
    @ExceptionHandler(LockException.class)
    public ResultMessage<Void> handleLockException(LockException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("Acquire Lock Fail '{}'", requestURI, e);
        return ResultMessage.fail(HttpStatus.HTTP_UNAVAILABLE, "业务处理中，请稍后再试...");
    }
}