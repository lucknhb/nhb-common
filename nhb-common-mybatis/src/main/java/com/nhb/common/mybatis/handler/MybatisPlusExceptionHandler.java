package com.nhb.common.mybatis.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.http.HttpStatus;
import com.baomidou.dynamic.datasource.exception.CannotFindDataSourceException;
import com.nhb.common.core.domain.ResultMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/20 15:41
 * @description:
 */
@Slf4j
@RestControllerAdvice
public class MybatisPlusExceptionHandler {

    /**
     * 主键或UNIQUE索引，数据重复异常
     */
    @ExceptionHandler(DuplicateKeyException.class)
    public ResultMessage<Void> handleDuplicateKeyException(DuplicateKeyException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("Request Uri '{}',Record Already Exists In The Database'{}'", requestURI, e.getMessage(), e);
        return ResultMessage.fail(HttpStatus.HTTP_CONFLICT, "数据库中已存在该记录,请联系管理员确认");
    }


    /**
     * Mybatis系统异常 通用处理
     */
    @ExceptionHandler(MyBatisSystemException.class)
    public ResultMessage<Void> handleCannotFindDataSourceException(MyBatisSystemException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        Throwable root = getRootCause(e);
        if (root instanceof NotLoginException) {
            log.error("Request Uri '{}',Auth Fail '{}'", requestURI, root.getMessage(), e);
            return ResultMessage.fail(HttpStatus.HTTP_UNAUTHORIZED, "认证失败,无法访问系统资源");
        }
        if (root instanceof CannotFindDataSourceException) {
            log.error("Request Uri '{}', No DataSource Found", requestURI, e);
            return ResultMessage.fail(HttpStatus.HTTP_INTERNAL_ERROR, "未找到数据源,请联系管理员确认");
        }
        log.error("Request Uri'{}', Mybatis Happen Error", requestURI, e);
        return ResultMessage.fail(HttpStatus.HTTP_INTERNAL_ERROR, "未知异常,请联系管理员确认");
    }


    /**
     * 获取异常的根因（递归查找）
     *
     * @param e 当前异常
     * @return 根因异常（最底层的 cause）
     * <p>
     * 逻辑说明：
     * 1. 如果 e 没有 cause，说明 e 本身就是根因，直接返回
     * 2. 如果 e 的 cause 和自身相同（防止循环引用），也返回 e
     * 3. 否则递归调用，继续向下寻找最底层的 cause
     */
    public static Throwable getRootCause(Throwable e) {
        Throwable cause = e.getCause();
        if (cause == null || cause == e) {
            return e;
        }
        return getRootCause(cause);
    }

}
