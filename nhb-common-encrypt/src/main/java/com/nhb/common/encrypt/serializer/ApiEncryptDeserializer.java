package com.nhb.common.encrypt.serializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.encrypt.annotation.ApiEncrypt;
import com.nhb.common.encrypt.properties.ApiEncryptProperties;
import com.nhb.common.encrypt.utils.EncryptUtil;
import com.nhb.common.encrypt.utils.HttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/9 9:23
 * @description: 反序列化加密数据
 */
@Slf4j
public class ApiEncryptDeserializer extends StdDeserializer<Object> implements ContextualDeserializer {

    private final JavaType targetType;

    public ApiEncryptDeserializer() {
        // 绑定到 Object 类型，表示处理所有类型
        super(Object.class);
        this.targetType = null;
    }

    // 带目标类型的构造器
    private ApiEncryptDeserializer(JavaType targetType) {
        super(targetType);
        this.targetType = targetType;
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        //读取密文字符串
        if (jsonParser.hasToken(JsonToken.VALUE_NULL)) {
            return null;
        }
        String content = jsonParser.getValueAsString();
        if (StringUtil.isBlank(content)) {
            return null;
        }
        try {
            ObjectMapper objectMapper = JacksonUtil.getObjectMapper();
            //需要判断是否是HTTP请求/请求资源是否存在@ApiEncrypt注解
            HttpServletRequest httpServletRequest = HttpRequestUtil.getRequest();
            if (Objects.isNull(httpServletRequest)) {
                return objectMapper.readValue(content, targetType);
            }
            ApiEncrypt apiEncrypt = HttpRequestUtil.getApiEncryptAnnotation(httpServletRequest);
            if (Objects.nonNull(apiEncrypt) && apiEncrypt.request()) {
                return objectMapper.readValue(content, targetType);
            }
            // 从请求头获取RSA加密后的AES秘钥
            ApiEncryptProperties apiEncryptProperties = SpringContextUtil.getBean(ApiEncryptProperties.class);
            String headerAes = httpServletRequest.getHeader(apiEncryptProperties.getHeaderFlag());
            Assert.hasLength(headerAes,"无法解析加密属性,请核实请求信息");
            //使用RSA私钥解密 获取AES秘钥
            Map<String, byte[]> keyMap = EncryptUtil.parseHeaderAesWithRsa(headerAes, apiEncryptProperties.getPrivateKey());
            String originalContent = EncryptUtil.decryptByAes(content, keyMap.get(EncryptUtil.PASSWORD), keyMap.get(EncryptUtil.SALT));
            //JSON 反序列化为目标类型对象
            return objectMapper.readValue(originalContent, targetType);
        } catch (Exception e) {
            throw new IOException("Failed to decrypt field", e);
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext context,
                                                BeanProperty property) {
        // 获取当前属性（字段/参数）的类型
        JavaType type = property.getType();
        return new ApiEncryptDeserializer(type);
    }

}
