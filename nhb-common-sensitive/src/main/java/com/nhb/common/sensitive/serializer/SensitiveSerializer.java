package com.nhb.common.sensitive.serializer;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
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
    private SensitiveStrategy strategy;
    private String[] roleKey;
    private String[] permissions;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        try {
            //先判断是否明确角色或权限
            //都为空时 直接脱敏处理  如果存在其中一个不为空 则使用自实现的类进行权限判断
            if (ArrayUtil.isEmpty(roleKey) && ArrayUtil.isEmpty(permissions)) {
                gen.writeString(strategy.desensitizer().apply(value));
            }else {
                SensitiveService sensitiveService = SpringContextUtil.getBean(SensitiveService.class);
                //不需要脱敏处理的话 则直接json化
                if (ObjectUtil.isNotNull(sensitiveService) && sensitiveService.isSensitive(roleKey, permissions)) {
                    gen.writeString(strategy.desensitizer().apply(value));
                } else {
                    gen.writeString(value);
                }
            }
        } catch (BeansException e) {
            log.error("Not found SensitiveService Implementation class. Now use default mode  => {}", e.getMessage());
            gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (Objects.nonNull(annotation) && Objects.equals(String.class, property.getType().getRawClass())) {
            this.strategy = annotation.strategy();
            this.roleKey = annotation.roleKeys();
            this.permissions = annotation.permissions();
            return this;
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
