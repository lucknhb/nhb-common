package com.nhb.common.id.service;

import com.nhb.common.core.utils.ContainerUtil;
import com.nhb.common.core.utils.IpUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.id.core.WorkerIdAssigner;
import com.nhb.common.id.core.WorkerNodeRepository;
import com.nhb.common.id.entity.WorkerNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/19 8:51
 * @description:  一次性 Worker ID 分配器实现类<BR/>
 *
 * 作用：<BR/>
 * 实现了基于数据库的 Worker ID 分配策略。<BR/>
 * 它通过向数据库表 (WORKER_NODE) 插入一条代表当前机器/容器的记录，<BR/>
 * 利用数据库的自增主键 (ID) 或唯一性约束来生成 Worker ID。<BR/>
 *
 * 工作原理：<BR/>
 *  1. 启动时，收集本机/容器的 IP 和 Port 信息。<BR/>
 *  2. 向数据库插入一条记录 (IP, PORT, TYPE)。<BR/>
 *  3. 数据库生成主键 ID（或通过 REPLACE INTO 机制生成）。<BR/>
 *  4. 将该 ID 作为当前节点的 Worker ID 返回。
 */
@Slf4j
@RequiredArgsConstructor
public class DisposableWorkerIdAssigner implements WorkerIdAssigner {
    private final WorkerNodeRepository workerNodeRepository;
    /**
     * 为当前工作节点分配唯一的 Worker ID
     *
     * @return 分配的工作节点 ID（通常是一个长整型数字）
     * @throws RuntimeException 如果分配失败（如所有 Worker ID 已被占用）
     * @note 实现类应保证该方法的：
     * 1. 原子性：同一时刻只有一个节点能获取到同一个 ID
     * 2. 持久性：节点重启后应能获取到相同的 ID（基于持久化存储）
     * 3. 可用性：在注册中心故障时应有降级方案
     */
    @Override
    public long assignWorkerId() {
        // 1. 构建工作节点实体：封装当前机器的网络和类型信息
        WorkerNode workerNode = buildWorkerNode();
        // 2. 持久化节点信息：将节点信息写入数据库
        // 注意：addWorkerNode 方法内部通常会处理“幂等性”。
        // 如果该 IP:Port 已存在，通常会更新心跳时间（LAST_HEARTBEAT_TIME），
        // 而不会重复插入导致主键冲突。
        workerNodeRepository.addWorkerNode(workerNode);
        log.info("The worker node was successfully added: {}", workerNode);
        // 3. 返回 ID：该 ID 通常就是数据库表的主键，
        // 或者是基于 IP/Port 计算出的唯一标识。
        return workerNode.getId();
    }

    /**
     * 构建工作节点实体对象
     *
     * 策略：
     * 1. 如果检测到运行在 Docker 容器中：使用 Docker 的 Host 和 Port。
     * 2. 如果运行在物理机中：使用本地 IP 和一个随机端口（模拟）。
     *
     * @return WorkerNodeEntity
     */
    private WorkerNode buildWorkerNode() {
        WorkerNode workerNode = new WorkerNode();

        // 检测是否为容器环境
        if (ContainerUtil.isRunningInsideContainer()) {
            // 获取容器 Host
            workerNode.setHostName(IpUtil.getLocalHostName());
            String port = SpringContextUtil.getProperty("local.server.port", "");
            // 获取容器 Port
            workerNode.setPort(port);
        } else {
            workerNode.setHostName(IpUtil.getLocalhostStr());  // 获取本地 IP
            // 模拟端口：由于物理机可能不开启特定服务端口，
            // 这里使用 "当前时间戳 + 随机数" 来模拟一个端口，
            // 确保在同一台机器上多次启动时能区分不同进程。
            SecureRandom secureRandom = new SecureRandom();
            workerNode.setPort(System.currentTimeMillis() + "-" + secureRandom.nextInt());
        }
        return workerNode;
    }
}
