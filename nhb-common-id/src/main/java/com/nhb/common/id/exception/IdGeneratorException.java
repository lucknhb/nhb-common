package com.nhb.common.id.exception;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 15:12
 * @description: IdGenerator异常类
 */
public class IdGeneratorException extends RuntimeException {
    public IdGeneratorException(String message) {
        super(message);
    }

    public IdGeneratorException(Throwable throwable) {
        super(throwable);
    }

    public IdGeneratorException(String message, Throwable cause) {
        super( message, cause );
    }

    public IdGeneratorException(String msgFormat, Object... args) {
        super( String.format( msgFormat, args ) );
    }
}
