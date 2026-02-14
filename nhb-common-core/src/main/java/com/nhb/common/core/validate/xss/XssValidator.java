package com.nhb.common.core.validate.xss;

import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HtmlUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/14 8:28
 * @description: xss校验注解逻辑实现
 */
public class XssValidator implements ConstraintValidator<Xss, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return !ReUtil.contains(HtmlUtil.RE_HTML_MARK, value);
    }
}
