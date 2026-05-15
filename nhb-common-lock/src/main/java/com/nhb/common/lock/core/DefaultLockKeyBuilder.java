package com.nhb.common.lock.core;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 16:14
 * @description: 分布式锁Key生成器
 */
@Slf4j
public class DefaultLockKeyBuilder implements LockKeyBuilder {

    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private final BeanResolver beanResolver;

    public DefaultLockKeyBuilder(BeanFactory beanFactory) {
        this.beanResolver = new BeanFactoryResolver(beanFactory);
    }

    @Override
    public String buildKey(MethodInvocation invocation, String[] definitionKeys) {
        if (definitionKeys.length > 1 || !"".equals(definitionKeys[0])) {
            return getSpelDefinitionKey(definitionKeys, invocation);
        }
        return "";
    }

    protected String getSpelDefinitionKey(String[] definitionKeys, MethodInvocation invocation) {
        Method method = invocation.getMethod();
        Object[] arguments = invocation.getArguments();
        Object rootObject = invocation.getThis();
        StandardEvaluationContext context = new MethodBasedEvaluationContext(Objects.requireNonNull(rootObject), method, arguments, NAME_DISCOVERER);
        context.setBeanResolver(beanResolver);
        List<String> definitionKeyList = new ArrayList<>(definitionKeys.length);
        for (String definitionKey : definitionKeys) {
            if (definitionKey != null && !definitionKey.isEmpty()) {
                String key = PARSER.parseExpression(definitionKey).getValue(context, String.class);
                definitionKeyList.add(key);
            }
        }
        return StringUtils.collectionToDelimitedString(definitionKeyList, ".", "", "");
    }


}
