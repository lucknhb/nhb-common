package com.nhb.common.signature.utils;/**
 * @author luck_nhb
 * @date 2026/4/14 15:58
 * @version 1.0
 * @description: 
 */
public class RequestBodyHolder {
    private static final ThreadLocal<byte[]> REQUEST_BODY = new ThreadLocal<>();

    public static void setBody(byte[] body) {
        REQUEST_BODY.set(body);
    }

    public static byte[] getBody() {
        return REQUEST_BODY.get();
    }

    public static void clear() {
        REQUEST_BODY.remove();
    }
}
