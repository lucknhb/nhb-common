package com.nhb.common.core.utils;



import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.type.MapType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/13 16:23
 * @description: json工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JacksonUtil {

    private static final ObjectMapper OBJECT_MAPPER = SpringContextUtil.getBean(ObjectMapper.class);

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 将对象转换为JSON格式的字符串
     *
     * @param object 要转换的对象
     * @return JSON格式的字符串，如果对象为null，则返回null
     * @throws RuntimeException 如果转换过程中发生JSON处理异常，则抛出运行时异常
     */
    public static String toJsonString(Object object) {
        if (ObjectUtil.isNull(object)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型的对象
     *
     * @param text  JSON格式的字符串
     * @param clazz 要转换的目标对象类型
     * @param <T>   目标对象的泛型类型
     * @return 转换后的对象，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字节数组转换为指定类型的对象
     *
     * @param bytes 字节数组
     * @param clazz 要转换的目标对象类型
     * @param <T>   目标对象的泛型类型
     * @return 转换后的对象，如果字节数组为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(bytes, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型的对象，支持复杂类型
     *
     * @param text          JSON格式的字符串
     * @param typeReference 指定类型的TypeReference对象
     * @param <T>           目标对象的泛型类型
     * @return 转换后的对象，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> T parseObject(String text, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为Dict对象
     *
     * @param text JSON格式的字符串
     * @return 转换后的Dict对象，如果字符串为空或者不是JSON格式则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static Dict parseMap(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructType(Dict.class));
        } catch (MismatchedInputException e) {
            // 类型不匹配说明不是json
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为Dict对象的列表
     *
     * @param text JSON格式的字符串
     * @return 转换后的Dict对象的列表，如果字符串为空则返回null
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static List<Dict> parseArrayMap(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, Dict.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将JSON格式的字符串转换为指定类型对象的列表
     *
     * @param text  JSON格式的字符串
     * @param clazz 要转换的目标对象类型
     * @param <T>   目标对象的泛型类型
     * @return 转换后的对象的列表，如果字符串为空则返回空列表
     * @throws RuntimeException 如果转换过程中发生IO异常，则抛出运行时异常
     */
    public static <T> List<T> toList(String text, Class<T> clazz) {
        if (StringUtils.isEmpty(text)) {
            return new ArrayList<>();
        }
        try {
            return OBJECT_MAPPER.readValue(text, OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Map转对象.
     * @param obj 对象
     * @param keyClass 键类
     * @param valueClass 值类
     * @param <K> 键泛型
     * @param <T> 泛型
     * @param <V> 值泛型
     * @return 对象
     */
    public static <T, K, V> T toMap(Object obj, Class<K> keyClass, Class<V> valueClass) {
        return OBJECT_MAPPER.convertValue(obj, mapType(keyClass, valueClass));
    }


    /**
     * 创建mapType.
     * @param keyClass 键类
     * @param valueClass 值类
     * @param <K> 键泛型
     * @param <V> 值泛型
     * @return MapType
     */
    private static <K, V> MapType mapType(Class<K> keyClass, Class<V> valueClass) {
        return OBJECT_MAPPER.getTypeFactory().constructMapType(Map.class, keyClass, valueClass);
    }

    /**
     * 判断字符串是否为合法 JSON（对象或数组）
     *
     * @param str 待校验字符串
     * @return true = 合法 JSON，false = 非法或空
     */
    public static boolean isJson(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为 JSON 对象（{}）
     *
     * @param str 待校验字符串
     * @return true = JSON 对象
     */
    public static boolean isJsonObject(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(str);
            return node.isObject();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断字符串是否为 JSON 数组（[]）
     *
     * @param str 待校验字符串
     * @return true = JSON 数组
     */
    public static boolean isJsonArray(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        try {
            JsonNode node = OBJECT_MAPPER.readTree(str);
            return node.isArray();
        } catch (Exception e) {
            return false;
        }
    }
}
