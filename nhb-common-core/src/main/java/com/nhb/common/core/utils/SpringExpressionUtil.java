package com.nhb.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/13 15:32
 * @description:  SpEL (Spring Expression Language) 表达式工具类
 *  提供高效、线程安全的表达式解析、求值、模板处理等功能
 *  内部缓存已编译的表达式，避免重复解析开销
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SpringExpressionUtil {

    // 线程安全的 SpEL 解析器（官方实现是线程安全的）
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    // 缓存已解析的表达式对象（key: 表达式字符串）
    private static final ConcurrentHashMap<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>();

    // 默认的模板解析上下文，用于处理 #{...} 形式的模板表达式
    private static final ParserContext TEMPLATE_PARSER_CONTEXT = new TemplateParserContext();

    // ========================== 表达式解析（带缓存）==========================

    /**
     * 解析表达式字符串，返回编译后的 Expression 对象（自动缓存）
     *
     * @param expression 表达式字符串
     * @return Expression 对象
     */
    public static Expression parseExpression(String expression) {
        return EXPRESSION_CACHE.computeIfAbsent(expression, exp -> {
            try {
                return PARSER.parseExpression(exp);
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid SpEL expression: " + exp, e);
            }
        });
    }

    // ========================== 简单求值（基于根对象）==========================

    /**
     * 对表达式求值，结果自动转换为指定类型
     *
     * @param expression  SpEL 表达式
     * @param rootObject  根对象
     * @param desiredType 期望返回的类型
     * @param <T>         泛型类型
     * @return 表达式计算结果
     */
    public static <T> T getValue(String expression, Object rootObject, Class<T> desiredType) {
        Expression expr = parseExpression(expression);
        try {
            return expr.getValue(rootObject, desiredType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate SpEL expression: " + expression, e);
        }
    }

    /**
     * 对表达式求值，返回 Object 类型
     *
     * @param expression SpEL 表达式
     * @param rootObject 根对象
     * @return 表达式计算结果
     */
    public static Object getValue(String expression, Object rootObject) {
        return getValue(expression, rootObject, Object.class);
    }

    // ========================== 带变量的求值 ==========================

    /**
     * 使用变量 Map 对表达式求值
     *
     * @param expression SpEL 表达式
     * @param variables  变量 Map，key 为变量名
     * @param rootObject 根对象（可为 null）
     * @param desiredType 期望返回的类型
     * @param <T>        泛型类型
     * @return 表达式计算结果
     */
    public static <T> T getValue(String expression, Map<String, Object> variables,
                                 Object rootObject, Class<T> desiredType) {
        // 创建 StandardEvaluationContext，并设置变量
        StandardEvaluationContext context = new StandardEvaluationContext(rootObject);
        if (variables != null) {
            context.setVariables(variables);
        }
        Expression expr = parseExpression(expression);
        try {
            return expr.getValue(context, desiredType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate SpEL expression with variables: " + expression, e);
        }
    }

    /**
     * 使用变量 Map 对表达式求值，返回 Object 类型
     */
    public static Object getValue(String expression, Map<String, Object> variables, Object rootObject) {
        return getValue(expression, variables, rootObject, Object.class);
    }

    // ========================== 自定义 EvaluationContext 求值 ==========================

    /**
     * 使用自定义的 EvaluationContext 对表达式求值
     *
     * @param expression SpEL 表达式
     * @param context    自定义上下文（注意线程安全，若复用需自行保证）
     * @param desiredType 期望返回的类型
     * @param <T>        泛型类型
     * @return 表达式计算结果
     */
    public static <T> T getValue(String expression, EvaluationContext context, Class<T> desiredType) {
        Expression expr = parseExpression(expression);
        try {
            return expr.getValue(context, desiredType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate SpEL expression with custom context: " + expression, e);
        }
    }

    /**
     * 使用自定义的 EvaluationContext 对表达式求值，返回 Object 类型
     */
    public static Object getValue(String expression, EvaluationContext context) {
        return getValue(expression, context, Object.class);
    }

    // ========================== 模板解析 ==========================

    /**
     * 解析模板字符串，将 #{...} 中的表达式替换为实际值
     * 示例：parseTemplate("Hello #{name}", user)  ->  Hello Alice
     *
     * @param template   包含 #{...} 的模板字符串
     * @param rootObject 根对象
     * @return 解析后的字符串
     */
    public static String parseTemplate(String template, Object rootObject) {
        return parseTemplate(template, rootObject, null);
    }

    /**
     * 解析模板字符串，支持变量 Map
     *
     * @param template   模板字符串
     * @param rootObject 根对象
     * @param variables  变量 Map
     * @return 解析后的字符串
     */
    public static String parseTemplate(String template, Object rootObject, Map<String, Object> variables) {
        try {
            // 使用模板解析上下文编译模板，生成一个组合表达式
            Expression expr = PARSER.parseExpression(template, TEMPLATE_PARSER_CONTEXT);
            if (variables == null || variables.isEmpty()) {
                return expr.getValue(rootObject, String.class);
            } else {
                StandardEvaluationContext context = new StandardEvaluationContext(rootObject);
                context.setVariables(variables);
                return expr.getValue(context, String.class);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse SpEL template: " + template, e);
        }
    }

    // ========================== 设置属性值 ==========================

    /**
     * 向根对象的指定属性设值（通过表达式）
     *
     * @param expression 赋值表达式，如 "user.name"
     * @param rootObject 根对象
     * @param value      要设置的值
     */
    public static void setValue(String expression, Object rootObject, Object value) {
        Expression expr = parseExpression(expression);
        try {
            expr.setValue(rootObject, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set value via SpEL expression: " + expression, e);
        }
    }

    /**
     * 使用 EvaluationContext 向指定属性设值
     */
    public static void setValue(String expression, EvaluationContext context, Object value) {
        Expression expr = parseExpression(expression);
        try {
            expr.setValue(context, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set value via SpEL expression with custom context: " + expression, e);
        }
    }

    // ========================== 辅助方法 ==========================

    /**
     * 清空表达式缓存（一般无需调用，仅在动态生成大量表达式且需要回收内存时使用）
     */
    public static void clearCache() {
        EXPRESSION_CACHE.clear();
    }

    /**
     * 获取当前缓存的表达式数量
     */
    public static int getCacheSize() {
        return EXPRESSION_CACHE.size();
    }

    // ========================== 快捷创建 EvaluationContext ==========================

    /**
     * 创建一个只读的、安全的 SimpleEvaluationContext（适合仅属性访问的场景，不支持方法调用等）
     *
     * @param rootObject 根对象
     * @return SimpleEvaluationContext
     */
    public static EvaluationContext createSimpleContext(Object rootObject) {
        return SimpleEvaluationContext.forReadOnlyDataBinding().withRootObject(rootObject).build();
    }

    /**
     * 创建一个 StandardEvaluationContext，支持完整 SpEL 功能（非线程安全，建议每次新建）
     *
     * @param rootObject 根对象
     * @return StandardEvaluationContext
     */
    public static EvaluationContext createStandardContext(Object rootObject) {
        return new StandardEvaluationContext(rootObject);
    }
}