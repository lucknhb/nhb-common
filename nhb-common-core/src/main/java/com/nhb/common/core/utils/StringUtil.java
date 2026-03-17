package com.nhb.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.AntPathMatcher;

import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/25 9:51
 * @description: 自实现字符串工具类
 */
@Slf4j
public class StringUtil extends StringUtils{
    public static final String SEPARATOR = ",";

    /**
     * 是否为http(s)://开头
     *
     * @param link 链接
     * @return 结果
     */
    public static boolean isHttp(String link) {
        return Validator.isUrl(link);
    }

    /**
     * 字符串转set
     *
     * @param str 字符串
     * @param sep 分隔符
     * @return set集合
     */
    public static Set<String> str2Set(String str, String sep) {
        return new HashSet<>(str2List(str, sep, true, false));
    }

    /**
     * 字符串转list
     *
     * @param str         字符串
     * @param sep         分隔符
     * @param filterBlank 过滤纯空白
     * @param trim        去掉首尾空白
     * @return list集合
     */
    public static List<String> str2List(String str, String sep, boolean filterBlank, boolean trim) {
        List<String> list = new ArrayList<>();
        if (StringUtils.isEmpty(str)) {
            return list;
        }
        // 过滤空白字符串
        if (filterBlank && StringUtils.isBlank(str)) {
            return list;
        }
        String[] split = str.split(sep);
        for (String string : split) {
            if (filterBlank && StringUtils.isBlank(string)) {
                continue;
            }
            if (trim) {
                string = StringUtils.trim(string);
            }
            list.add(string);
        }
        return list;
    }

    /**
     * 查找指定字符串是否匹配指定字符串列表中的任意一个字符串
     *
     * @param target  指定字符串
     * @param values 需要检查的字符串数组
     * @return 是否匹配
     */
    public static boolean matches(String target, List<String> values) {
        if (StringUtils.isEmpty(target) || CollUtil.isEmpty(values)) {
            return false;
        }
        for (String pattern : values) {
            if (isMatch(pattern, target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断url是否与规则配置:
     * ? 表示单个字符;
     * * 表示一层路径内的任意字符串，不可跨层级;
     * ** 表示任意层路径;
     *
     * @param pattern 匹配规则
     * @param url     需要匹配的url
     */
    public static boolean isMatch(String pattern, String url) {
        AntPathMatcher matcher = new AntPathMatcher();
        return matcher.match(pattern, url);
    }

    /**
     * 数字左边补齐0，使之达到指定长度。注意，如果数字转换为字符串后，长度大于size，则只保留 最后size个字符。
     *
     * @param num  数字对象
     * @param size 字符串指定长度
     * @return 返回数字的字符串格式，该字符串为指定长度。
     */
    public static String fillLeft(final Number num, final int size) {
        return fillLeft(num.toString(), size, '0');
    }

    /**
     * 字符串左补齐。如果原始字符串s长度大于size，则只保留最后size个字符。
     *
     * @param s    原始字符串
     * @param size 字符串指定长度
     * @param c    用于补齐的字符
     * @return 返回指定长度的字符串，由原字符串左补齐或截取得到。
     */
    public static String fillLeft(final String s, final int size, final char c) {
        final StringBuilder sb = new StringBuilder(size);
        if (s != null) {
            final int len = s.length();
            if (s.length() <= size) {
                sb.append(Convert.toStr(c).repeat(size - len));
                sb.append(s);
            } else {
                return s.substring(len - size, len);
            }
        } else {
            sb.append(Convert.toStr(c).repeat(Math.max(0, size)));
        }
        return sb.toString();
    }

    /**
     * 切分字符串(分隔符默认逗号)
     *
     * @param str 被切分的字符串
     * @return 分割后的数据列表
     */
    public static List<String> splitList(String str) {
        return splitTo(str, Convert::toStr);
    }

    /**
     * 切分字符串
     *
     * @param str       被切分的字符串
     * @param separator 分隔符
     * @return 分割后的数据列表
     */
    public static List<String> splitList(String str, String separator) {
        return splitTo(str, separator, Convert::toStr);
    }

    /**
     * 切分字符串自定义转换(分隔符默认逗号)
     *
     * @param str    被切分的字符串
     * @param mapper 自定义转换
     * @return 分割后的数据列表
     */
    public static <T> List<T> splitTo(String str, Function<? super Object, T> mapper) {
        return splitTo(str, SEPARATOR, mapper);
    }

    /**
     * 切分字符串自定义转换
     *
     * @param str       被切分的字符串
     * @param separator 分隔符
     * @param mapper    自定义转换
     * @return 分割后的数据列表
     */
    public static <T> List<T> splitTo(String str, String separator, Function<? super Object, T> mapper) {
        if (StringUtils.isBlank(str)) {
            return new ArrayList<>(0);
        }
        return StrUtil.split(str, separator)
                .stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 不区分大小写检查 CharSequence 是否以指定的前缀开头。
     *
     * @param str     要检查的 CharSequence 可能为 null
     * @param prefixes 要查找的前缀可能为 null
     * @return 是否包含
     */
    public static boolean startWithAnyIgnoreCase(CharSequence str, CharSequence... prefixes) {
        // 判断是否是以指定字符串开头
        for (CharSequence prefix : prefixes) {
            if (StringUtils.startsWithIgnoreCase(str, prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将字符串从源字符集转换为目标字符集
     *
     * @param input       原始字符串
     * @param fromCharset 源字符集
     * @param toCharset   目标字符集
     * @return 转换后的字符串
     */
    public static String convert(String input, Charset fromCharset, Charset toCharset) {
        if (StringUtils.isBlank(input)) {
            return input;
        }
        try {
            // 从源字符集获取字节
            byte[] bytes = input.getBytes(fromCharset);
            // 使用目标字符集解码
            return new String(bytes, toCharset);
        } catch (Exception e) {
            return input;
        }
    }

    /**
     * 将可迭代对象中的元素使用逗号拼接成字符串
     *
     * @param iterable 可迭代对象，如 List、Set 等
     * @return 拼接后的字符串
     */
    public static String joinComma(Iterable<?> iterable) {
        return StringUtils.join(iterable, SEPARATOR);
    }

    /**
     * 将数组中的元素使用逗号拼接成字符串
     *
     * @param array 任意类型的数组
     * @return 拼接后的字符串
     */
    public static String joinComma(Object[] array) {
        return StringUtils.join(array, SEPARATOR);
    }

}
