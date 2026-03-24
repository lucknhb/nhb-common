package com.nhb.common.translation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhb.common.translation.annotation.TranslationType;
import com.nhb.common.translation.core.TranslationInterface;
import com.nhb.common.translation.handler.TranslationBeanSerializerModifier;
import com.nhb.common.translation.handler.TranslationHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 9:01
 * @description:
 */
@Slf4j
@AutoConfiguration
public class TranslationAutoConfiguration {

    @Resource
    private List<TranslationInterface<?>> translationInterfaces;
    @Resource
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        Map<String, TranslationInterface<?>> map = new HashMap<>(translationInterfaces.size());
        for (TranslationInterface<?> trans : translationInterfaces) {
            if (trans.getClass().isAnnotationPresent(TranslationType.class)) {
                TranslationType annotation = trans.getClass().getAnnotation(TranslationType.class);
                map.put(annotation.type(), trans);
            } else {
                log.warn("{} 翻译实现类未标注 TranslationType 注解!", trans.getClass().getName());
            }
        }
        TranslationHandler.TRANSLATION_MAPPER.putAll(map);
        // 设置 Bean 序列化修改器
        objectMapper.setSerializerFactory(
                objectMapper.getSerializerFactory()
                        .withSerializerModifier(new TranslationBeanSerializerModifier()));
    }

}
