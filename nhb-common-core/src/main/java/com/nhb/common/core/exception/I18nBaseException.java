package com.nhb.common.core.exception;

import com.nhb.common.core.utils.I18MessageUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/14 10:28
 * @description: 定义I18N基础异常类 <BR/>
 * 可根据code进行国际化处理
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class I18nBaseException extends RuntimeException {
    /**
     * 错误码
     */
    private String code;

    /**
     * 错误码对应的参数
     */
    private Object[] args;

    /**
     * 错误消息
     */
    private String message;


    public I18nBaseException(String message) {
        this(null, null, message);
    }

    public I18nBaseException(String code, Object[] args) {
        this(code, args, null);
    }


    /**
     * 根据code获取国际化信息 当获取不到时 使用默认异常信息
     *
     * @return
     */
    @Override
    public String getMessage() {
        String result = null;
        if (!StringUtils.isEmpty(code)) {
            result = I18MessageUtil.message(code, args);
        }
        if (Objects.isNull(result)) {
            result = message;
        }
        return result;
    }
}
