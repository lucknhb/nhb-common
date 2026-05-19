package com.nhb.common.id.core;

import com.nhb.common.id.service.DefaultIdGenerator;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/19 8:43
 * @description: Worker ID 分配器接口<BR/>
 *
 * 作用：<BR/>
 * 为 {@link DefaultIdGenerator} 分配工作节点 ID。<BR/>
 *
 * 在分布式唯一 ID 生成方案中（如雪花算法 Snowflake），<BR/>
 * 每个工作节点（应用实例）都需要一个全局唯一的 Worker ID，<BR/>
 * 以确保不同节点生成的 ID 不会因时间戳或序列号冲突而重复。<BR/>
 *
 * 实现策略示例：<BR/>
 *  1. 基于数据库自增 ID 分配<BR/>
 *  2. 基于 Redis 原子操作分配<BR/>
 *  3. 基于容器环境自分配
 */
public interface WorkerIdAssigner {

    /**
     * 为当前工作节点分配唯一的 Worker ID
     *
     * @return 分配的工作节点 ID（通常是一个长整型数字）
     *
     * @throws RuntimeException 如果分配失败（如所有 Worker ID 已被占用）
     *
     * @note 实现类应保证该方法的：
     *       1. 原子性：同一时刻只有一个节点能获取到同一个 ID
     *       2. 持久性：节点重启后应能获取到相同的 ID（基于持久化存储）
     *       3. 可用性：在注册中心故障时应有降级方案
     */
    long assignWorkerId();
}
