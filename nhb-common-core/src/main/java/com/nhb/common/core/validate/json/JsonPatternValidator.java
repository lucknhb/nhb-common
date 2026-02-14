package com.nhb.common.core.validate.json;

import com.nhb.common.core.utils.JacksonUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/13 17:00
 * @description: JSON 格式校验器
 */
public class JsonPatternValidator implements ConstraintValidator<JsonPattern, String> {
    /**
     * 注解中指定的 JSON 类型枚举
     */
    private JsonType jsonType;

    /**
     * 初始化校验器，从注解中提取 JSON 类型
     *
     * @param annotation 注解实例
     */
    @Override
    public void initialize(JsonPattern annotation) {
        this.jsonType = annotation.type();
    }

    /**
     * 校验字符串是否为合法 JSON
     *
     * @param value   待校验字符串
     * @param context 校验上下文，可用于自定义错误信息
     * @return true = 合法 JSON 或为空，false = 非法 JSON
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (StringUtils.isBlank(value)) {
            // @NotBlank 或 @NotNull 控制是否允许为空
            return true;
        }
        // 根据 JSON 类型进行不同的校验
        return switch (jsonType) {
            case ANY -> JacksonUtil.isJson(value);
            case OBJECT -> JacksonUtil.isJsonObject(value);
            case ARRAY -> JacksonUtil.isJsonArray(value);
        };
    }
}
