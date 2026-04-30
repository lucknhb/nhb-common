package com.nhb.common.sensitive.serializer;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.sensitive.annotation.Sensitive;
import com.nhb.common.sensitive.service.SensitiveService;
import com.nhb.common.sensitive.strategy.SensitiveStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

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
        Assert.notNull(strategy, StrUtil.format("Strategy is null for value:{}", value));
        SensitiveService sensitiveService = SpringContextUtil.getBeanFactory()
                .getBeanProvider(SensitiveService.class, true)
                .getIfAvailable();
        //SensitiveService为空 或者 有具体实现类时根据返回值判断
        if (Objects.isNull(sensitiveService) || sensitiveService.isSensitive(roleKey, perms)) {
            gen.writeString(strategy.desensitizer().apply(value));
        } else {
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
