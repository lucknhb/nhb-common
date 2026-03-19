package com.nhb.common.nacos.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/19 10:08
 * @description:  是否存在gateway环境
 */
public class NoGatewayCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ClassLoader classLoader = context.getClassLoader();
        boolean hasGatewayClass = ClassUtils.isPresent(
                "org.springframework.cloud.gateway.route.RouteLocator",
                classLoader
        );
        return !hasGatewayClass;
    }
}
