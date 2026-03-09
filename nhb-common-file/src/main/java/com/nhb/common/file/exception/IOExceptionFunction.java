package com.nhb.common.file.exception;

import java.io.IOException;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 16:43
 * @description: 带 IOException 异常的 Function
 */
public interface IOExceptionFunction<T, R> {
    R apply(T t) throws IOException;
}
