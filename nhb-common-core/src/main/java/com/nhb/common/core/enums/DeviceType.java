package com.nhb.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/14 9:58
 * @description: 设备类型
 */
@Getter
@AllArgsConstructor
public enum DeviceType {
    /**
     * pc端
     */
    PC("pc"),

    /**
     * app端
     */
    APP("app"),

    /**
     * 小程序端
     */
    XCX("xcx"),

    /**
     * 第三方社交登录平台
     */
    SOCIAL("social");

    /**
     * 设备标识
     */
    private final String device;
}
