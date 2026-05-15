package com.nhb.common.lock.exception;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 15:17
 * @description: 分布式锁异常信息
 */
public class LockException extends RuntimeException {
    public LockException() {
        super();
    }

    public LockException(String message) {
        super(message);
    }
}
