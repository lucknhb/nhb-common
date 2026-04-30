package com.nhb.common.lock.exception;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 15:18
 * @description:
 */
public class LockFailureException extends LockException {
    public LockFailureException(String message) {
        super(message);
    }
}
