package com.nhb.common.lock.aspectj;

import com.nhb.common.lock.annotation.Lock;
import com.nhb.common.lock.core.LockFailureStrategy;
import com.nhb.common.lock.core.LockInfo;
import com.nhb.common.lock.core.LockKeyBuilder;
import com.nhb.common.lock.core.LockTemplate;
import com.nhb.common.lock.properties.LockConfigProperties;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.OrderComparator;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 15:58
 * @description: 分布式锁aop处理器
 */
@Slf4j
@RequiredArgsConstructor
public class LockInterceptor implements MethodInterceptor, InitializingBean {

    private final Map<Class<? extends LockKeyBuilder>, LockKeyBuilder> keyBuilderMap = new LinkedHashMap<>();
    private final Map<Class<? extends LockFailureStrategy>, LockFailureStrategy> failureStrategyMap = new LinkedHashMap<>();

    private final LockTemplate lockTemplate;

    private final List<LockKeyBuilder> keyBuilders;

    private final List<LockFailureStrategy> failureStrategies;

    private final LockConfigProperties lockConfigProperties;

    private LockOperation primaryLockOperation;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        //fix 使用其他aop组件时,aop切了两次.
        Class<?> cls = AopProxyUtils.ultimateTargetClass(Objects.requireNonNull(invocation.getThis()));
        if (!cls.equals(invocation.getThis().getClass())) {
            return invocation.proceed();
        }
        Lock lock = AnnotatedElementUtils.findMergedAnnotation(invocation.getMethod(), Lock.class);
        LockInfo lockInfo = null;
        try {
            LockOperation lockOperation = buildLockOperation(Objects.requireNonNull(lock));
            String prefix = lockConfigProperties.getLockKeyPrefix() + ":";
            prefix += StringUtils.hasText(lock.name()) ? lock.name() :
                    invocation.getMethod().getDeclaringClass().getName() + invocation.getMethod().getName();
            String key = prefix + "#" + lockOperation.lockKeyBuilder.buildKey(invocation, lock.keys());
            lockInfo = lockTemplate.lock(key, lock.expire(), lock.acquireTimeout(), lock.executor());
            if (null != lockInfo) {
                return invocation.proceed();
            }
            // lock failure
            lockOperation.lockFailureStrategy.onLockFailure(key, invocation.getMethod(), invocation.getArguments());
            return null;
        } finally {
            if (null != lockInfo && lock.autoRelease()) {
                final boolean releaseLock = lockTemplate.releaseLock(lockInfo);
                if (!releaseLock) {
                    log.error("releaseLock fail,lockKey={},lockValue={}", lockInfo.getLockKey(),
                            lockInfo.getLockValue());
                }
            }
        }
    }

    /**
     * Invoked by the containing {@code BeanFactory} after it has set all bean properties
     * and satisfied {@link BeanFactoryAware}, {@code ApplicationContextAware} etc.
     * <p>This method allows the bean instance to perform validation of its overall
     * configuration and final initialization when all bean properties have been set.
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an
     *                   essential property) or if initialization fails for any other reason
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        keyBuilderMap.putAll(keyBuilders.stream().collect(Collectors.toMap(LockKeyBuilder::getClass, x -> x)));
        failureStrategyMap.putAll(failureStrategies.stream().collect(Collectors.toMap(LockFailureStrategy::getClass, x -> x)));
        LockKeyBuilder lockKeyBuilder;
        LockFailureStrategy lockFailureStrategy;
        List<LockKeyBuilder> priorityOrderedLockBuilders = keyBuilders.stream().filter(Ordered.class::isInstance).collect(Collectors.toList());
        if (lockConfigProperties.getPrimaryKeyBuilder() != null) {
            lockKeyBuilder = keyBuilderMap.get(lockConfigProperties.getPrimaryKeyBuilder());
        } else if (!priorityOrderedLockBuilders.isEmpty()) {
            sortOperation(priorityOrderedLockBuilders);
            lockKeyBuilder = priorityOrderedLockBuilders.getFirst();
        } else {
            lockKeyBuilder = keyBuilders.getFirst();
        }
        List<LockFailureStrategy> priorityOrderedFailures = failureStrategies.stream().filter(Ordered.class::isInstance).collect(Collectors.toList());
        if (lockConfigProperties.getPrimaryFailureStrategy() != null) {
            lockFailureStrategy = failureStrategyMap.get(lockConfigProperties.getPrimaryFailureStrategy());
        } else if (!priorityOrderedFailures.isEmpty()) {
            sortOperation(priorityOrderedFailures);
            lockFailureStrategy = priorityOrderedFailures.getFirst();
        } else {
            lockFailureStrategy = failureStrategies.getFirst();
        }
        primaryLockOperation = LockOperation.builder().lockKeyBuilder(lockKeyBuilder).lockFailureStrategy(lockFailureStrategy).build();
    }

    @Builder
    private static class LockOperation {
        /**
         * key生成器
         */
        private LockKeyBuilder lockKeyBuilder;
        /**
         * 锁失败策略
         */
        private LockFailureStrategy lockFailureStrategy;
    }

    private LockOperation buildLockOperation(Lock lock) {
        LockKeyBuilder lockKeyBuilder;
        LockFailureStrategy lockFailureStrategy;
        Class<? extends LockFailureStrategy> failStrategy = lock.failStrategy();
        Class<? extends LockKeyBuilder> keyBuilderStrategy = lock.keyBuilderStrategy();
        if (keyBuilderStrategy == null || keyBuilderStrategy == LockKeyBuilder.class) {
            lockKeyBuilder = primaryLockOperation.lockKeyBuilder;
        } else {
            lockKeyBuilder = keyBuilderMap.get(keyBuilderStrategy);
        }
        if (failStrategy == null || failStrategy == LockFailureStrategy.class) {
            lockFailureStrategy = primaryLockOperation.lockFailureStrategy;
        } else {
            lockFailureStrategy = failureStrategyMap.get(failStrategy);
        }
        return LockOperation.builder().lockKeyBuilder(lockKeyBuilder).lockFailureStrategy(lockFailureStrategy).build();
    }

    private void sortOperation(List<?> operations) {
        if (operations.size() <= 1) {
            return;
        }
        operations.sort(OrderComparator.INSTANCE);
    }
}
