package com.nhb.common.core.utils;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.nhb.common.core.constant.StringPoolConstants;
import io.github.linpeilie.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.yaml.snakeyaml.util.UriEncoder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 16:42
 * @description map工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapUtil {
    /**
     * @param numMappings 映射数量
     */
    public static <K, V> HashMap<K, V> newHashMap(int numMappings) {
        return HashMap.newHashMap(numMappings);
    }

    public static <K, V> LinkedHashMap<K, V> newLinkedHashMap(int numMappings) {
        return LinkedHashMap.newLinkedHashMap(numMappings);
    }

    /**
     * 判断不为空.
     * @param map map对象
     * @return 判断结果
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    /**
     * 判断为空.
     * @param map map对象
     * @return 判断结果
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return ObjectUtils.isNull(map) || map.isEmpty();
    }

    /**
     * 转URI Map.
     * @param uriMap map对象
     * @param serviceId 服务ID
     * @param separator 分隔符
     * @return Map对象
     */
    public static Map<String, Set<String>> toUriMap(Map<String, Set<String>> uriMap, String serviceId,
                                                    String separator) {
        return uriMap.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue()
                                .stream()
                                .filter(item -> item.contains(serviceId))
                                .map(item -> item.substring(0, item.indexOf(separator)))
                                .collect(Collectors.toSet())));
    }

    /**
     * 转URI Map.
     * @param uriMap map对象
     * @param serviceId 服务ID
     * @return Map对象
     */
    public static Map<String, Set<String>> toUriMap(Map<String, Set<String>> uriMap, String serviceId) {
        return toUriMap(uriMap, serviceId, StringPoolConstants.EQUAL);
    }

    // @formatter:off
    /**
     * 字符串参数转为map参数.
     * @see <a href="https://github.com/livk-cloud/spring-boot-extension/blob/main/spring-extension-commons/src/main/java/com/livk/commons/util/MultiValueMapSplitter.java">MultiValueMapSplitter</a>.
     * @param params 参数
     * @return map参数对象
     */
    public static MultiValueMap<String, String> getParameterMap(String params, String separator) {
        if (StrUtil.isNotEmpty(params)) {
            String[] strings = params.split(separator);
            MultiValueMap<String, String> parameterMap = new LinkedMultiValueMap<>(strings.length * 2);
            for (String string : strings) {
                int index = string.indexOf(StringPoolConstants.EQUAL);
                if (index > -1) {
                    String key = string.substring(0, index);
                    String value = UriEncoder.decode(string.substring(index + 1));
                    parameterMap.add(key, value);
                }
            }
            return parameterMap;
        }
        return new LinkedMultiValueMap<>(0);
    }
    // @formatter:on

    /**
     * map转字符串.
     * @param paramMap map对象
     * @param isEncode 是否编码
     * @return 字符串
     */
    public static String parseParamterString(Map<String, String> paramMap, boolean isEncode) {
        if (paramMap.isEmpty()) {
            return StrUtil.EMPTY;
        }
        Iterator<Map.Entry<String, String>> iterator = paramMap.entrySet().iterator();
        StringBuilder stringBuilder = new StringBuilder();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuilder.append(key)
                    .append(StringPoolConstants.EQUAL)
                    .append(isEncode ? URLEncoder.encode(value, StandardCharsets.UTF_8) : value)
                    .append(StringPoolConstants.AND);
        }
        String str = stringBuilder.toString();
        return str.substring(0, str.length() - 1);
    }

    /**
     * map转为字符串.
     * @param paramMap 参数
     * @return 字符串
     */
    public static String parseParamterString(Map<String, String> paramMap) {
        return parseParamterString(paramMap, true);
    }

    /**
     * 请求对象构建MultiValueMap.
     * @param parameterMap 请求参数Map
     * @return MultiValueMap
     */
    public static MultiValueMap<String, String> getParameterMap(Map<String, String[]> parameterMap) {
        Map<String, List<String>> transformValues = Maps.transformValues(parameterMap, Lists::newArrayList);
        return new MultiValueMapAdapter<>(transformValues);
    }

}
