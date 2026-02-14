package com.nhb.common.core.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/13 14:55
 * @description:  接口返回结果
 */
@Data
public class ResultMessage <T> implements Serializable {
    /**
     * 成功
     */
    public static final int SUCCESS = 200;
    /**
     * 失败
     */
    public static final int FAIL = 500;
    /**
     * 状态码
     */
    private int code;
    /**
     * 提示信息
     */
    private String msg;
    /**
     * 业务数据
     */
    private T data;

    public static <T> ResultMessage<T> ok() {
        return buildResult(null, SUCCESS, null);
    }

    public static <T> ResultMessage<T> ok(T data) {
        return buildResult(data, SUCCESS, null);
    }

    public static <T> ResultMessage<T> ok(T data, String msg) {
        return buildResult(data, SUCCESS, msg);
    }

    public static <T> ResultMessage<T> failed() {
        return buildResult(null, FAIL, null);
    }

    public static <T> ResultMessage<T> failed(String msg) {
        return buildResult(null, FAIL, msg);
    }

    public static <T> ResultMessage<T> failed(T data) {
        return buildResult(data, FAIL, null);
    }

    public static <T> ResultMessage<T> failed(T data, String msg) {
        return buildResult(data, FAIL, msg);
    }

    /**
     * 构造返回结果
     * @param data  业务数据
     * @param code  状态码
     * @param msg   提示信息
     * @return
     * @param <T>   返回结果
     */
    private static <T> ResultMessage<T> buildResult(T data, int code, String msg) {
        ResultMessage<T> apiResult = new ResultMessage<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

}
