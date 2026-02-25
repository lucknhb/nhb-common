package com.nhb.common.sensitive.strategy;

import cn.hutool.core.convert.Convert;
import com.nhb.common.core.utils.DesensitizeUtil;
import lombok.AllArgsConstructor;

import java.util.function.Function;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/25 15:53
 * @description: 脱敏策略
 */
@AllArgsConstructor
public enum SensitiveStrategy {
    /**
     * 身份证脱敏
     */
    ID_CARD(s -> DesensitizeUtil.idCardNum(s, 3, 4)),

    /**
     * 手机号脱敏
     */
    PHONE(DesensitizeUtil::mobilePhone),

    /**
     * 地址脱敏
     */
    ADDRESS(s -> DesensitizeUtil.address(s, 8)),

    /**
     * 邮箱脱敏
     */
    EMAIL(DesensitizeUtil::email),

    /**
     * 银行卡
     */
    BANK_CARD(DesensitizeUtil::bankCard),

    /**
     * 中文名
     */
    CHINESE_NAME(DesensitizeUtil::chineseName),

    /**
     * 固定电话
     */
    FIXED_PHONE(DesensitizeUtil::fixedPhone),

    /**
     * 用户ID
     */
    USER_ID(s -> Convert.toStr(DesensitizeUtil.userId())),

    /**
     * 密码
     */
    PASSWORD(DesensitizeUtil::password),

    /**
     * ipv4
     */
    IPV4(DesensitizeUtil::ipv4),

    /**
     * ipv6
     */
    IPV6(DesensitizeUtil::ipv6),

    /**
     * 中国大陆车牌，包含普通车辆、新能源车辆
     */
    CAR_LICENSE(DesensitizeUtil::carLicense),

    /**
     * 只显示第一个字符
     */
    FIRST_MASK(DesensitizeUtil::firstMask),

    /**
     * 通用字符串脱敏
     * 可配置前后可见长度和中间掩码长度
     * 默认示例：前4位可见，后4位可见，中间固定4个*
     */
    STRING_MASK(s -> DesensitizeUtil.mask(s, 4, 4, 4)),

    /**
     * 高安全级别脱敏（Token / 私钥）：前2位可见，后2位可见，中间全部掩码
     */
    MASK_HIGH_SECURITY(s -> DesensitizeUtil.maskHighSecurity(s, 2, 2)),

    /**
     * 清空为null
     */
    CLEAR(s -> DesensitizeUtil.clear()),

    /**
     * 清空为""
     */
    CLEAR_TO_NULL(s -> DesensitizeUtil.clearToNull());

    //可自行添加其他脱敏策略

    private final Function<String, String> desensitizer;

    public Function<String, String> desensitizer() {
        return desensitizer;
    }
}
