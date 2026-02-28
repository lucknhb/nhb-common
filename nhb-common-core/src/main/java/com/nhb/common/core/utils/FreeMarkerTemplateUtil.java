package com.nhb.common.core.utils;

import com.nhb.common.core.exception.ServiceException;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.IOException;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/28 13:52
 * @description:
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FreeMarkerTemplateUtil extends FreeMarkerTemplateUtils {

    /**
     * 模板配置.
     */
    private static final Configuration CONFIGURATION = new Configuration(
            Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);

    /**
     * 根据模板获取内容.
     *
     * @param template 模板
     * @param params   参数
     * @return 内容
     */
    public static String getContent(String template, Map<String, Object> params) {
        try {
            Template temp = getTemplate(template);
            return FreeMarkerTemplateUtils.processTemplateIntoString(temp, params);
        } catch (Exception e) {
            log.error("FreeMarkTemplate Convert error:{}", e.getMessage(), e);
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 获取模板.
     *
     * @param template 模板名称
     * @return 模板
     * @throws IOException 异常
     */
    private static Template getTemplate(String template) throws IOException {
        return new Template("template", template, CONFIGURATION);
    }
}
