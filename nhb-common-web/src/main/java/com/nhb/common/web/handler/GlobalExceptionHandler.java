package com.nhb.common.web.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import com.fasterxml.jackson.core.JsonParseException;
import com.nhb.common.core.domain.ResultMessage;
import com.nhb.common.core.exception.ServiceException;
import com.nhb.common.core.utils.StreamUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.expression.ExpressionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.io.IOException;
import java.sql.SQLSyntaxErrorException;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/28 14:53
 * @description: 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLSyntaxErrorException.class)
    public ResultMessage<Void> handleSQLSyntaxErrorException(SQLSyntaxErrorException sqlSyntaxErrorException){
        log.error("SQL Syntax Error:{}", sqlSyntaxErrorException.getMessage(),sqlSyntaxErrorException);
        return ResultMessage.fail("操作异常,请联系管理员");
    }
    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResultMessage<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        return ResultMessage.fail(HttpStatus.HTTP_BAD_METHOD, e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(ServiceException.class)
    public ResultMessage<Void> handleServiceException(ServiceException e, HttpServletRequest request) {
        log.error(e.getMessage());
        return ObjectUtil.isNotNull(e.getCode()) ? ResultMessage.fail(e.getCode(), e.getMessage()) : ResultMessage.fail(e.getMessage());
    }

    /**
     * 信息转换异常
     * @param e
     * @param request
     * @return
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    public ResultMessage<Void> handleHttpMessageNotWritableException(HttpMessageNotWritableException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String message = ExceptionUtils.getRootCause(e).getMessage();
        log.error("请求地址'{}' 数据转换异常", requestURI, e);
        return ResultMessage.fail(message);
    }

    /**
     * 参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResultMessage<Void> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}'", requestURI, e);
        return ResultMessage.fail(e.getMessage());
    }

    /**
     * servlet异常
     */
    @ExceptionHandler(ServletException.class)
    public ResultMessage<Void> handleServletException(ServletException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        return ResultMessage.fail(e.getMessage());
    }


    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public ResultMessage<Void> handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI);
        return ResultMessage.fail(String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResultMessage<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求参数类型不匹配'{}',发生系统异常.", requestURI);
        return ResultMessage.fail(String.format("请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'", e.getName(), e.getRequiredType().getName(), e.getValue()));
    }

    /**
     * 找不到路由
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResultMessage<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}'不存在.", requestURI);
        return ResultMessage.fail(HttpStatus.HTTP_NOT_FOUND, e.getMessage());
    }

    /**
     * 拦截未知的运行时异常
     */
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(IOException.class)
    public void handleIoException(IOException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("sse")) {
            // sse 经常性连接中断 例如关闭浏览器 直接屏蔽
            return;
        }
        log.error("请求地址'{}',连接中断", requestURI, e);
    }

    /**
     * sse 连接超时异常 不需要处理
     */
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public void handleRuntimeException(AsyncRequestTimeoutException e) {
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResultMessage<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        return ResultMessage.fail(e.getMessage());
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public ResultMessage<Void> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        return ResultMessage.fail(e.getMessage());
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public ResultMessage<Void> handleBindException(BindException e) {
        log.error(e.getMessage());
        String message = StreamUtil.join(e.getAllErrors(), DefaultMessageSourceResolvable::getDefaultMessage, ", ");
        return ResultMessage.fail(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResultMessage<Void> constraintViolationException(ConstraintViolationException e) {
        log.error(e.getMessage());
        String message = StreamUtil.join(e.getConstraintViolations(), ConstraintViolation::getMessage, ", ");
        return ResultMessage.fail(message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResultMessage<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage());
        String message = StreamUtil.join(e.getBindingResult().getAllErrors(), DefaultMessageSourceResolvable::getDefaultMessage, ", ");
        return ResultMessage.fail(message);
    }

    /**
     * 方法参数校验异常 用于处理 @Validated 注解
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResultMessage<Void> handlerMethodValidationException(HandlerMethodValidationException e) {
        log.error(e.getMessage());
        String message = StreamUtil.join(e.getAllErrors(), MessageSourceResolvable::getDefaultMessage, ", ");
        return ResultMessage.fail(message);
    }

    /**
     * JSON 解析异常（Jackson 在处理 JSON 格式出错时抛出）
     * 可能是请求体格式非法，也可能是服务端反序列化失败
     */
    @ExceptionHandler(JsonParseException.class)
    public ResultMessage<Void> handleJsonParseException(JsonParseException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}' 发生 JSON 解析异常: {}", requestURI, e.getMessage());
        return ResultMessage.fail(HttpStatus.HTTP_BAD_REQUEST, "请求数据格式错误（JSON 解析失败）：" + e.getMessage());
    }

    /**
     * 请求体读取异常（通常是请求参数格式非法、字段类型不匹配等）
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResultMessage<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e, HttpServletRequest request) {
        log.error("请求地址'{}', 参数解析失败: {}", request.getRequestURI(), e.getMessage());
        return ResultMessage.fail(HttpStatus.HTTP_BAD_REQUEST, "请求参数格式错误：" + e.getMostSpecificCause().getMessage());
    }

    /**
     * SpEL 表达式相关异常
     */
    @ExceptionHandler(ExpressionException.class)
    public ResultMessage<Void> handleSpelException(ExpressionException e, HttpServletRequest request) {
        log.error("请求地址'{}'，SpEL解析异常: {}", request.getRequestURI(), e.getMessage());
        return ResultMessage.fail(HttpStatus.HTTP_INTERNAL_ERROR, "SpEL解析失败：" + e.getMessage());
    }

}
