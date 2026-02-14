package com.nhb.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/14 9:37
 * @description: 时间格式
 */
@Getter
@AllArgsConstructor
public enum DateTimeFormatType {
    /**
     * 例如：2026年表示为"26"
     */
    YY("yy"),
    /**
     * 例如：2026年表示为"2026"
     */
    YYYY("yyyy"),

    /**
     * 例例如，2026年1月可以表示为 "2026-01"
     */
    YYYY_MM("yyyy-MM"),

    /**
     * 例如，日期 "2026年1月1日" 可以表示为 "2026-01-01"
     */
    YYYY_MM_DD("yyyy-MM-dd"),

    /**
     * 例如，当前时间如果是 "2026年1月1日下午3点30分"，则可以表示为 "2026-01-01 15:30"
     */
    YYYY_MM_DD_HH_MM("yyyy-MM-dd HH:mm"),

    /**
     * 例如，当前时间如果是 "2026年1月1日下午3点30分45秒"，则可以表示为 "2026-01-01 15:30:45"
     */
    YYYY_MM_DD_HH_MM_SS("yyyy-MM-dd HH:mm:ss"),

    /**
     * 例如：下午3点30分45秒，表示为 "15:30:45"
     */
    HH_MM_SS("HH:mm:ss"),

    /**
     * 例例如，2026年1月可以表示为 "2026/01"
     */
    YYYY_MM_SLASH("yyyy/MM"),

    /**
     * 例如，日期 "2026年1月1日" 可以表示为 "2026/01/01"
     */
    YYYY_MM_DD_SLASH("yyyy/MM/dd"),

    /**
     * 例如，当前时间如果是 "2026年1月1日下午3点30分45秒"，则可以表示为 "2026/01/01 15:30:45"
     */
    YYYY_MM_DD_HH_MM_SLASH("yyyy/MM/dd HH:mm"),

    /**
     * 例如，当前时间如果是 "2026年1月1日下午3点30分45秒"，则可以表示为 "2026/01/01 15:30:45"
     */
    YYYY_MM_DD_HH_MM_SS_SLASH("yyyy/MM/dd HH:mm:ss"),

    /**
     * 例例如，2026年1月可以表示为 "2026.01"
     */
    YYYY_MM_DOT("yyyy.MM"),

    /**
     * 例如，日期 "2026年1月1日" 可以表示为 "2026.01.01"
     */
    YYYY_MM_DD_DOT("yyyy.MM.dd"),

    /**
     * 例如，当前时间如果是 "2026年1月1日下午3点30分"，则可以表示为 "2026.01.01 15:30"
     */
    YYYY_MM_DD_HH_MM_DOT("yyyy.MM.dd HH:mm"),

    /**
     * 例如，当前时间如果是 "2026年1月1日下午3点30分45秒"，则可以表示为 "2026.01.01 15:30:45"
     */
    YYYY_MM_DD_HH_MM_SS_DOT("yyyy.MM.dd HH:mm:ss"),

    /**
     * 例如，2026年1月可以表示为 "202601"
     */
    YYYYMM("yyyyMM"),

    /**
     * 例如，2026年1月1日可以表示为 "20260101"
     */
    YYYYMMDD("yyyyMMdd"),

    /**
     * 例如，2026年1月1日下午3点可以表示为 "2026010115"
     */
    YYYYMMDDHH("yyyyMMddHH"),

    /**
     * 例如，2026年1月1日下午3点30分可以表示为 "202601011530"
     */
    YYYYMMDDHHMM("yyyyMMddHHmm"),

    /**
     * 例如，2026年1月1日下午3点30分45秒可以表示为 "20260101153045"
     */
    YYYYMMDDHHMMSS("yyyyMMddHHmmss");

    /**
     * 时间格式
     */
    private final String timeFormatType;
}
