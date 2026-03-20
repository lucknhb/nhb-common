package com.nhb.common.mybatis.aspectj;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/19 16:37
 * @description: 数据权限注解切面定义
 */
@SuppressWarnings("all")
public class DataPermissionPointcutAdvisorAspect extends AbstractPointcutAdvisor {
    private final Advice advice;
    private final Pointcut pointcut;

    public DataPermissionPointcutAdvisorAspect() {
        this.advice = new DataPermissionAdviceAspect();
        this.pointcut =  new DataPermissionPointcutAspect();
    }

    @Override
    public Pointcut getPointcut() {
        return this.pointcut;
    }

    @Override
    public Advice getAdvice() {
        return this.advice;
    }
}
