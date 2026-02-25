package com.nhb.common.sensitive.service;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/25 16:04
 * @description:
 * 脱敏服务
 * 默认管理员不过滤
 * 需自行根据业务重写实现
 */
public interface SensitiveService {
    /**
     * 是否脱敏
     */
    boolean isSensitive(String[] roleKey, String[] perms);
}
