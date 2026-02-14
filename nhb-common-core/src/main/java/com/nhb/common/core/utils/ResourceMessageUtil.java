package com.nhb.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/14 10:33
 * @description: 国际化工具类 <BR/>
 * 切记不可在初始化中使用该方法 此时容器中的数据并未初始化完成 无法获取到 MessageSource
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceMessageUtil {
    private static final MessageSource MESSAGE_SOURCE = SpringContextUtil.getBean(MessageSource.class);

    /**
     * 根据消息键和参数 获取消息 委托给spring messageSource
     *
     * @param code 消息键
     * @param args 参数
     * @return 获取国际化翻译值 如果获取不到则返回null
     */
    public static String message(String code, Object... args) {
        try {
            return MESSAGE_SOURCE.getMessage(code, args, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            log.error("Not found the code[{}] from i18n resource",code);
            return null;
        }
    }
}
