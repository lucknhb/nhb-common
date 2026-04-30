package com.nhb.common.lock.core;

import com.nhb.common.lock.exception.LockFailureException;

import java.lang.reflect.Method;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 16:14
 * @description:
 */
public class DefaultLockFailureStrategy implements LockFailureStrategy {

    protected static String DEFAULT_MESSAGE = "Request Failed,Please Retry It.";

    @Override
    public void onLockFailure(String key, Method method, Object[] arguments) {
        throw new LockFailureException(DEFAULT_MESSAGE);
    }
}
