package com.nhb.common.file.exception;

import java.io.IOException;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 16:44
 * @description: 带 IOException 异常的 Consumer
 */
@FunctionalInterface
public interface IOExceptionConsumer <T> {
    void accept(T t) throws IOException;
}
