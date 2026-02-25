package com.nhb.common.core.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/25 9:02
 * @description: 校验工具类<BR/>
 * 切记不可在初始化中使用该方法 此时容器中的数据并未初始化完成 无法获取到 Validator
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidatorUtil {

    private static final Validator VALID = SpringContextUtil.getBean(Validator.class);

    /**
     * 对给定对象进行参数校验，并根据指定的校验组进行校验
     *
     * @param object 要进行校验的对象
     * @param groups 校验组
     * @throws ConstraintViolationException 如果校验不通过，则抛出参数校验异常
     */
    public static <T> void validate(T object, Class<?>... groups) {
        Set<ConstraintViolation<T>> validate = VALID.validate(object, groups);
        if (!validate.isEmpty()) {
            throw new ConstraintViolationException("参数校验异常", validate);
        }
    }
}
