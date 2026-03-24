package com.nhb.common.translation.handler;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.nhb.common.core.utils.ReflectSelfUtil;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.translation.annotation.Translation;
import com.nhb.common.translation.core.TranslationInterface;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 8:54
 * @description:
 */
@Slf4j
public class TranslationHandler extends JsonSerializer<Object> implements ContextualSerializer {

    /**
     * 全局翻译实现类映射器
     */
    public static final Map<String, TranslationInterface<?>> TRANSLATION_MAPPER = new ConcurrentHashMap<>();

    private Translation translation;

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        TranslationInterface<?> trans = TRANSLATION_MAPPER.get(translation.type());
        if (ObjectUtil.isNotNull(trans)) {
            // 如果映射字段不为空 则取映射字段的值
            if (StringUtil.isNotBlank(translation.mapper())) {
                value = ReflectSelfUtil.invokeGetter(gen.currentValue(), translation.mapper());
            }
            // 如果为 null 直接写出
            if (ObjectUtil.isNull(value)) {
                gen.writeNull();
                return;
            }
            try {
                Object result = trans.translation(value, translation.other());
                gen.writeObject(result);
            } catch (Exception e) {
                log.error("翻译处理异常，type: {}, value: {}", translation.type(), value, e);
                // 出现异常时输出原始值而不是中断序列化
                gen.writeObject(value);
            }
        } else {
            gen.writeObject(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        Translation translation = property.getAnnotation(Translation.class);
        if (Objects.nonNull(translation)) {
            this.translation = translation;
            return this;
        }
        return prov.findValueSerializer(property.getType(), property);
    }
}
