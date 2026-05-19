package com.nhb.common.id.entity;

import com.nhb.common.core.utils.JacksonUtil;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/19 8:58
 * @description:
 */
@Data
public class WorkerNode implements Serializable {

    /**
     * 实体唯一ID (数据库主键)
     * <p>
     * 说明：
     * 1. 在数据库中通常是自增主键 (AUTO_INCREMENT)。
     * 2. 在业务逻辑中，这个 'id' 的值通常直接被用作
     * 雪花算法 (Snowflake) 中的 Worker ID。
     * 3. 因此，它的值必须在集群内保持唯一且非负。
     */
    private Long id;

    /**
     * 主机名称或IP地址
     * <p>
     * 说明：
     * 用于区分不同的机器或容器实例。
     * - 物理机/虚拟机模式：存储 IP 地址
     * - 容器模式 (Docker/Podman/K8s)：存储 HostName。
     * <p>
     * 设计意图：
     * 在容器化环境下，容器内部的 IP 可能频繁变化，但 HostName（如 Pod Name）
     * 通常具有稳定性，便于追踪和管理。
     */
    private String hostName;

    /**
     * 端口号或进程标识符
     * <p>
     * 说明：
     * 用于在同一台机器上区分不同的进程实例。
     * - 物理机模式：由于可能没有固定的业务端口，这里存储的是
     * "时间戳 + 随机数" (e.g., "1623456789-12345")，确保唯一性。
     * - 容器模式：存储映射的 Port
     * <p>
     * 注意：
     * 这里的 'Port' 是逻辑概念，不一定对应真实的网络端口监听，
     * 主要是为了构建 (HostName, Port) 的唯一组合。
     */
    private String port;

    /**
     * Worker 启动日期 (默认当前时间)
     * <p>
     * 说明：
     * 记录该工作节点进程启动的时间。
     * 这是一个业务时间戳，用于辅助排查问题（如判断是哪次重启导致的 ID 变更）。
     */
    private LocalDateTime launchTime = LocalDateTime.now();

    /**
     * 创建时间 (Created time)
     * <p>
     * 说明：
     * 记录该条数据库记录的创建时间。
     * 通常由数据库自动维护 (如 MySQL 的 CURRENT_TIMESTAMP)。
     */
    private LocalDateTime createTime;

    /**
     * 最后修改时间 (Last modified)
     * <p>
     * 说明：
     * 记录该条数据库记录的最后更新时间。
     * 在节点重启或心跳更新时，该字段会被刷新，用于监控节点活跃状态。
     */
    private LocalDateTime updateTime;

    @Override
    public String toString() {
        return JacksonUtil.toJsonString(this);
    }
}
