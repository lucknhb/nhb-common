package com.nhb.common.sensitive.serializer;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.nhb.common.sensitive.annotation.Sensitive;
import com.nhb.common.sensitive.service.SensitiveService;
import com.nhb.common.sensitive.strategy.SensitiveStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;

import java.io.IOException;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/25 15:58
 * @description: 数据脱敏json序列化工具
 */
@Slf4j
public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {
    private final SensitiveStrategy strategy;
    private final String[] roleKey;
    private final String[] perms;

    /**
     * 提供给 jackson 创建上下文序列化器时使用 不然会报错
     */
    public SensitiveSerializer() {
        this.strategy = null;
        this.roleKey = null;
        this.perms = null;
    }

    public SensitiveSerializer(SensitiveStrategy strategy, String[] roleKey, String[] perms) {
        this.strategy = strategy;
        this.roleKey = roleKey;
        this.perms = perms;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            SensitiveService sensitiveService = SpringUtil.getBean(SensitiveService.class);
            if (ObjectUtil.isNotNull(sensitiveService) && sensitiveService.isSensitive(roleKey, perms)) {
                gen.writeString(strategy.desensitizer().apply(value));
            } else {
                gen.writeString(value);
            }
        } catch (BeansException e) {
            log.error("脱敏实现不存在, 采用默认处理 => {}", e.getMessage());
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            return new SensitiveSerializer(annotation.strategy(), annotation.roleKeys(), annotation.permissions());
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
