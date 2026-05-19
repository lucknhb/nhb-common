package com.nhb.common.id.core;

import com.nhb.common.id.entity.WorkerNode;
import com.nhb.common.id.service.DisposableWorkerIdAssigner;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/19 8:53
 * @description: 工作节点接口<BR/>
 *
 * 作用：<BR/>
 * 定义了与 WORKER_NODE 表（工作节点表）交互的数据访问方法。<BR/>
 * 该表用于记录分布式系统中所有已注册的工作节点信息，<BR/>
 * 包括节点的主机名、端口、类型（物理机/容器）以及分配到的 Worker ID。<BR/>
 *
 * 使用场景：<BR/>
 * 在 {@link DisposableWorkerIdAssigner} 分配 Worker ID 时，<BR/>
 * 会先通过本接口查询当前节点是否已注册，如果未注册则插入新记录，<BR/>
 * 并利用数据库的自增主键或唯一约束来生成 Worker ID。
 */
public interface WorkerNodeRepository {

    /**
     * 根据主机名和端口查询工作节点信息
     *
     * @param host 主机名或 IP 地址（如：192.168.1.100 或 docker-host-1）
     * @param port 端口号或进程标识（如：8080 或 202605190852-12345）
     * @return 工作节点实体对象，如果未找到则返回 null
     *
     * @note 该方法用于：
     *       1. 节点重启时，检查是否已有对应的 Worker ID 记录
     *       2. 避免同一节点重复注册导致 Worker ID 浪费
     *       3. 支持节点状态恢复（如获取上次的心跳时间）
     */
    WorkerNode getWorkerNodeByHostPort(String host, String port);

    /**
     * 新增工作节点记录
     *
     * @param workerNode 工作节点实体对象，包含主机名、端口、节点类型等信息
     *
     * @note 该方法需要保证幂等性：
     *       1. 如果该节点（host + port）已存在，应更新心跳时间而非重复插入
     *       2. 通常使用 INSERT ... ON DUPLICATE KEY UPDATE 或 REPLACE INTO 实现
     *       3. 插入成功后，entity 的 id 字段会被自动填充为数据库生成的主键值
     *
     * @throws RuntimeException 如果数据库操作失败（如连接超时、主键冲突未处理）
     */
    void addWorkerNode(WorkerNode workerNode);
}
