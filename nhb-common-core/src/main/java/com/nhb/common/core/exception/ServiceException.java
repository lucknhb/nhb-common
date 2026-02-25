package com.nhb.common.core.exception;

import cn.hutool.core.text.StrFormatter;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/25 11:14
 * @description: 内部业务异常
 */
public class ServiceException extends RuntimeException {
    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 错误明细，内部调试错误
     */
    private String detailMessage;

    public ServiceException(String message) {
        this.message = message;
    }

    public ServiceException(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public ServiceException(String message, Object... args) {
        this.message = StrFormatter.format(message, args);
    }

    @Override
    public String getMessage() {
        return message;
    }

  public ServiceException setMessage(String message) {
    this.message = message;
    return this;
  }

  public ServiceException setDetailMessage(String detailMessage) {
    this.detailMessage = detailMessage;
    return this;
  }
}
