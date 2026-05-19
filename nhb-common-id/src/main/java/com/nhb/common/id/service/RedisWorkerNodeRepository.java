package com.nhb.common.id.service;

import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.id.core.WorkerNodeRepository;
import com.nhb.common.id.entity.WorkerNode;
import com.nhb.common.id.exception.IdGeneratorException;
import com.nhb.common.id.properties.IdGeneratorConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.TimeUnit;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/19 13:51
 * @description: 利用redis进行workId分配
 */
@Slf4j
@RequiredArgsConstructor
public class RedisWorkerNodeRepository implements WorkerNodeRepository, DisposableBean {
    private final RedissonClient redissonClient;

    /**
     * 全局 WorkerId 分配计数器
     */
    private final String assignerKey = "global:id-generator:assigner";

    /**
     * WorkerId 注册表（MapCache，带过期时间）
     */
    private final String workerRegistryKey = "global:id-generator:worker:registry";

    /**
     * WorkerId 心跳续期时间（秒），默认 60 秒
     */
    private final long heartbeatSeconds = 60;

    /**
     * WorkerId 最大重试次数
     */
    private static final int MAX_RETRY_TIMES = 100;

    /**
     * 本地缓存：当前 Pod 的 WorkerId
     */
    private volatile Long localWorkerId = null;

    @Override
    public WorkerNode getWorkerNodeByHostPort(String host, String port) {
        // 从注册表中查询已有的 WorkerId
        RMapCache<String, Long> registry = redissonClient.getMapCache(workerRegistryKey);
        String key = host + ":" + port;
        Long workerId = registry.get(key);
        if (workerId != null) {
            log.info("Get from the registry to what you already have WorkerId: {} for host:port={}", workerId, key);
            WorkerNode entity = new WorkerNode();
            entity.setId(workerId);
            entity.setHostName(host);
            entity.setPort(port);
            return entity;
        }
        return null;
    }

    @Override
    public void addWorkerNode(WorkerNode entity) {
        // 1. 先检查本地缓存，避免重复分配
        if (localWorkerId != null) {
            entity.setId(localWorkerId);
            log.debug("Using a local cache WorkerId: {}", localWorkerId);
            return;
        }
        // 2. 检查是否已有注册记录（幂等性处理）
        String registryKey = entity.getHostName() + ":" + entity.getPort();
        RMapCache<String, Long> registry = redissonClient.getMapCache(workerRegistryKey);
        Long existingWorkerId = registry.get(registryKey);
        if (existingWorkerId != null) {
            // 已有注册记录，复用 WorkerId 并续期
            renewHeartbeat(registry, registryKey, existingWorkerId);
            entity.setId(existingWorkerId);
            localWorkerId = existingWorkerId;
            log.info("Reuse existing ones WorkerId: {} for {}", existingWorkerId, registryKey);
            return;
        }
        // 3. 使用 Redisson 的 RAtomicLong 进行原子自增
        RAtomicLong counter = redissonClient.getAtomicLong(assignerKey);
        IdGeneratorConfigProperties idGeneratorConfigProperties = SpringContextUtil.getBean(IdGeneratorConfigProperties.class);
        // 尝试多次获取 WorkerId（防止并发冲突）
        for (int retry = 0; retry < MAX_RETRY_TIMES; retry++) {
            // 3.1 原子自增
            long nextValue = counter.incrementAndGet();
            // 3.2 取模，确保在 0~1023 范围内（10 bit）
            long workerId = nextValue % (1L << idGeneratorConfigProperties.getWorkerIdBits());
            // 3.3 尝试注册到注册表（使用 putIfAbsent 保证原子性）
            Long oldValue = registry.putIfAbsent(registryKey, workerId,
                    heartbeatSeconds, TimeUnit.SECONDS);
            if (null == oldValue) {
                // 注册成功
                entity.setId(workerId);
                localWorkerId = workerId;
                log.info("Successfully assign a new one WorkerId: {} for {} (counter={})",
                        workerId, registryKey, nextValue);
                return;
            }
            // 注册失败（被其他 Pod 抢占了），继续重试
            log.warn("WorkerId {} is occupied, retry the assignment... (retry={})", workerId, retry);
        }
        // 4. 重试耗尽，抛出异常
        throw new IdGeneratorException("Cannot be assigned WorkerId，retry " + MAX_RETRY_TIMES + " failed after the second time");
    }

    /**
     * 续期 WorkerId 的心跳
     */
    private void renewHeartbeat(RMapCache<String, Long> registry,
                                String key, Long workerId) {
        // 重新设置过期时间，实现心跳续期
        registry.put(key, workerId, heartbeatSeconds, TimeUnit.SECONDS);
        log.debug("Renewal WorkerId heartbeat: {} for {}", workerId, key);
    }

    /**
     * 启动心跳续期线程
     */
    public void startHeartbeat() {
        if (localWorkerId == null) {
            log.warn("WorkerId not yet assigned to start a heartbeat");
            return;
        }

        // 使用 Redisson 的分布式调度器
        redissonClient.getExecutorService("id-heartbeat")
                .scheduleWithFixedDelay(() -> {
                    try {
                        RMapCache<String, Long> registry =
                                redissonClient.getMapCache(workerRegistryKey);
                        String key = getCurrentPodKey();
                        if (key != null && localWorkerId != null) {
                            renewHeartbeat(registry, key, localWorkerId);
                        }
                    } catch (Exception e) {
                        log.error("Heartbeat renewal failed", e);
                    }
                }, heartbeatSeconds / 2, heartbeatSeconds / 2, TimeUnit.SECONDS);

        log.info("WorkerId The heartbeat renewal period is activated, interval {} s", heartbeatSeconds / 2);
    }

    /**
     * 获取当前 Pod 的唯一标识
     */
    private String getCurrentPodKey() {
        // 优先使用 K8S 环境变量
        String podName = System.getenv("POD_NAME");
        if (podName != null && !podName.isEmpty()) {
            return podName;
        }

        // 降级使用 HOSTNAME
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && !hostname.isEmpty()) {
            return hostname;
        }

        return null;
    }

    /**
     * 优雅关闭：释放 WorkerId
     */
    @Override
    public void destroy() throws Exception {
        if (localWorkerId == null) {
            return;
        }
        try {
            RMapCache<String, Integer> registry =
                    redissonClient.getMapCache(workerRegistryKey);
            String key = getCurrentPodKey();
            if (key != null) {
                // 删除注册记录
                registry.remove(key);
                log.info("WorkerId {} was released", localWorkerId);
            }
        } catch (Exception e) {
            log.error("Released WorkerId failed", e);
        } finally {
            localWorkerId = null;
        }
    }
}
