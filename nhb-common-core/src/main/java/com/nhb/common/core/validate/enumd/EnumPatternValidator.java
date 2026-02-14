package com.nhb.common.core.validate.enumd;

import com.nhb.common.core.utils.ReflectUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/14 8:42
 * @description:
 */
public class EnumPatternValidator implements ConstraintValidator<EnumPattern, String> {

    private EnumPattern annotation;

    @Override
    public void initialize(EnumPattern annotation) {
        ConstraintValidator.super.initialize(annotation);
        this.annotation = annotation;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isNotBlank(value)) {
            String fieldName = annotation.fieldName();
            for (Object e : annotation.type().getEnumConstants()) {
                if (value.equals(ReflectUtils.invokeGetter(e, fieldName))) {
                    return true;
                }
            }
        }
        return false;
    }

}
