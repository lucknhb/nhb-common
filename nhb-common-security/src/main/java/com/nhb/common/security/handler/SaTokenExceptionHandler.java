package com.nhb.common.security.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.hutool.http.HttpStatus;
import com.nhb.common.core.domain.ResultMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/5 9:11
 * @description: SaToken异常处理器
 */
@Slf4j
@RestControllerAdvice
public class SaTokenExceptionHandler {
    /**
     * 权限码异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public ResultMessage<Void> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("Request url'{}',Permission Code Check Fail'{}'", requestURI, e.getMessage());
        return ResultMessage.fail(HttpStatus.HTTP_FORBIDDEN, "没有访问权限，请联系申请授权");
    }

    /**
     * 角色权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    public ResultMessage<Void> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("Request url'{}',Role Check Fail'{}'", requestURI, e.getMessage());
        return ResultMessage.fail(HttpStatus.HTTP_FORBIDDEN, "没有访问权限，请联系申请授权");
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(NotLoginException.class)
    public ResultMessage<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("Request url'{}',Not Login'{}',无法访问系统资源", requestURI, e.getMessage());
        return ResultMessage.fail(HttpStatus.HTTP_UNAUTHORIZED, "认证失败，无法访问系统资源");
    }
}
