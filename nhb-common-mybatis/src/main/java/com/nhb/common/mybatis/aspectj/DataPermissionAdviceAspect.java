package com.nhb.common.mybatis.aspectj;

import com.nhb.common.mybatis.annotation.DataPermission;
import com.nhb.common.mybatis.helper.DataPermissionHelper;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/19 16:11
 * @description: 数据权限注解Advice
 */
@Slf4j
public class DataPermissionAdviceAspect implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object target = invocation.getThis();
        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();
        // 设置权限注解
        DataPermissionHelper.setPermission(getDataPermissionAnnotation(target, method, args));
        try {
            // 执行代理方法
            return invocation.proceed();
        } finally {
            // 清除权限注解
            DataPermissionHelper.removePermission();
        }
    }

    /**
     * 获取数据权限注解
     * @param target   目标对象
     * @param method   目标方法
     * @param args     执行参数
     * @return         方法上的 数据权限 注解
     */
    private DataPermission getDataPermissionAnnotation(Object target, Method method, Object[] args) {
        DataPermission dataPermission = method.getAnnotation(DataPermission.class);
        // 优先获取方法上的注解
        if (dataPermission != null) {
            return dataPermission;
        }
        // 方法上没有注解，则获取类上的注解
        Class<?> targetClass = target.getClass();
        // 如果是 JDK 动态代理，则获取真实的Class实例
        if (Proxy.isProxyClass(targetClass)) {
            targetClass = targetClass.getInterfaces()[0];
        }
        dataPermission = targetClass.getAnnotation(DataPermission.class);
        return dataPermission;
    }
}
