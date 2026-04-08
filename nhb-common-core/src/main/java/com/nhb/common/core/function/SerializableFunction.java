package com.nhb.common.core.function;



import java.io.Serializable;
import java.util.function.Function;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/8 14:52
 * @description: 可序列化的Function接口，用于支持Lambda表达式的序列化
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}
