package com.nhb.common.lock.core;

import com.nhb.common.lock.exception.LockException;
import com.nhb.common.lock.executor.LockExecutor;
import com.nhb.common.lock.properties.LockConfigProperties;
import com.nhb.common.lock.utils.LockUtil;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/30 16:10
 * @description: 锁模板方法
 */

@Slf4j
@SuppressWarnings("rawtypes")
public class LockTemplate implements InitializingBean {
    private final Map<Class<? extends LockExecutor>, LockExecutor> executorMap = new LinkedHashMap<>();
    @Setter
    private LockConfigProperties lockConfigProperties;
    @Setter
    private List<LockExecutor> executors;

    private LockExecutor primaryExecutor;

    public LockTemplate() {
    }

    public LockInfo lock(String key) {
        return lock(key, 0, -1);
    }

    public LockInfo lock(String key, long expire, long acquireTimeout) {
        return lock(key, expire, acquireTimeout, null);
    }

    /**
     * 加锁方法
     *
     * @param key            锁key 同一个key只能被一个客户端持有
     * @param expire         过期时间(ms) 防止死锁
     * @param acquireTimeout 尝试获取锁超时时间(ms)
     * @param executor       执行器
     * @return 加锁成功返回锁信息 失败返回null
     */
    public LockInfo lock(String key, long expire, long acquireTimeout, Class<? extends LockExecutor> executor) {
        acquireTimeout = acquireTimeout < 0 ? lockConfigProperties.getAcquireTimeout() : acquireTimeout;
        long retryInterval = lockConfigProperties.getRetryInterval();
        LockExecutor lockExecutor = obtainExecutor(executor);
        log.info("Use Lock Class: {} For Key:{}", lockExecutor.getClass(), key);
        expire = !lockExecutor.renewal() && expire <= 0 ? lockConfigProperties.getExpire() : expire;
        int acquireCount = 0;
        String value = LockUtil.simpleUUID();
        long start = System.currentTimeMillis();
        try {
            do {
                acquireCount++;
                Object lockInstance = lockExecutor.acquire(key, value, expire, acquireTimeout);
                if (null != lockInstance) {
                    return new LockInfo(key, value, expire, acquireTimeout, acquireCount, lockInstance,
                            lockExecutor);
                }
                TimeUnit.MILLISECONDS.sleep(retryInterval);
            } while (System.currentTimeMillis() - start < acquireTimeout);
        } catch (InterruptedException e) {
            log.error("lock error", e);
            throw new LockException();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public boolean releaseLock(LockInfo lockInfo) {
        if (null == lockInfo) {
            return false;
        }
        return lockInfo.getLockExecutor().releaseLock(lockInfo.getLockKey(), lockInfo.getLockValue(),
                lockInfo.getLockInstance());
    }

    protected LockExecutor obtainExecutor(Class<? extends LockExecutor> clazz) {
        if (null == clazz || clazz == LockExecutor.class) {
            return primaryExecutor;
        }
        final LockExecutor lockExecutor = executorMap.get(clazz);
        Assert.notNull(lockExecutor, String.format("Can Not Get Bean Type Of %s", clazz));
        return lockExecutor;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.isTrue(lockConfigProperties.getAcquireTimeout() >= 0, "TryTimeout Must Least 0");
        Assert.isTrue(lockConfigProperties.getExpire() >= -1, "ExpireTime Must Least -1");
        Assert.isTrue(lockConfigProperties.getRetryInterval() >= 0, "RetryInterval Must More Least 0");
        Assert.hasText(lockConfigProperties.getLockKeyPrefix(), "Lock Key Prefix Must Be Not Blank");
        Assert.notEmpty(executors, "Executors Must Have At Least One");

        for (LockExecutor executor : executors) {
            executorMap.put(executor.getClass(), executor);
        }

        final Class<? extends LockExecutor> primaryExecutor = lockConfigProperties.getPrimaryExecutor();
        if (null == primaryExecutor) {
            this.primaryExecutor = executors.getFirst();
        } else {
            this.primaryExecutor = executorMap.get(primaryExecutor);
            Assert.notNull(this.primaryExecutor, "PrimaryExecutor Must Be Not Null");
        }
    }
}
