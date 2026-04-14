package com.nhb.common.encrypt.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.ObjectSelfUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.encrypt.annotation.ApiEncrypt;
import com.nhb.common.encrypt.properties.ApiEncryptProperties;
import com.nhb.common.encrypt.utils.EncryptUtil;
import com.nhb.common.encrypt.utils.HttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.IOException;
import java.rmi.ServerException;
import java.util.Map;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/9 8:28
 * @description: @ApiEncrypt注解 JSON 序列化工具
 */
@Slf4j
public class ApiEncryptSerializer extends StdSerializer<Object> {


    public ApiEncryptSerializer() {
        //绑定到 Object 类型，表示处理所有类型
        super(Object.class);
    }

    @Override
    public void serialize(Object value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (value == null) {
            jsonGenerator.writeNull();
            return;
        }

        try {
            // 将原始对象序列化为 JSON 字符串
            String originalJson = JacksonUtil.getObjectMapper().writeValueAsString(value);
            HttpServletRequest httpServletRequest = HttpRequestUtil.getRequest();
            //非HTTP请求或者请求资源已经存在@ApiEncrypt 则在此不进行加密
            if (ObjectSelfUtil.isNull(httpServletRequest)) {
                jsonGenerator.writeString(originalJson);
                return;
            }
            ApiEncrypt apiEncrypt = HttpRequestUtil.getApiEncryptAnnotation(httpServletRequest);
            if (Objects.nonNull(apiEncrypt) && apiEncrypt.response()) {
                jsonGenerator.writeString(originalJson);
                return;
            }
            //从请求头中获取已经存在的加密头信息
            ApiEncryptProperties apiEncryptProperties = SpringContextUtil.getBean(ApiEncryptProperties.class);
            String header = HttpRequestUtil.getHeader(httpServletRequest, apiEncryptProperties.getHeaderFlag());
            Assert.hasLength(header,"无法加密响应值,请核实请求信息");
            // 设置响应头
            HttpServletResponse httpServletResponse = HttpRequestUtil.getResponse();
            Objects.requireNonNull(httpServletResponse).setHeader(apiEncryptProperties.getHeaderFlag(), header);
            //每次加密请求都有调用方生成临时AES秘钥及向量
            Map<String, byte[]> keyMap = EncryptUtil.parseHeaderAesWithRsa(header, apiEncryptProperties.getPrivateKey());
            byte[]  aesKey = keyMap.get(EncryptUtil.PASSWORD);
            byte[] aesVi = keyMap.get(EncryptUtil.SALT);
            String data = EncryptUtil.encryptByAesBase64(originalJson, aesKey, aesVi);
            //输出密文字符串
            jsonGenerator.writeString(data);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServerException(e.getMessage());
        }
    }
}
