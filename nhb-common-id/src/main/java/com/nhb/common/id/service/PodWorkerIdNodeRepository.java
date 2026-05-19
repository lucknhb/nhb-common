package com.nhb.common.id.service;

import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.id.core.WorkerNodeRepository;
import com.nhb.common.id.entity.WorkerNode;
import com.nhb.common.id.properties.IdGeneratorConfigProperties;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/19 13:52
 * @description: 利用容器环境下的指定属性进行workerId分配
 */
@Slf4j
public class PodWorkerIdNodeRepository implements WorkerNodeRepository {
    /**
     * 根据主机名和端口查询工作节点信息
     *
     * @param host 主机名或 IP 地址（如：192.168.1.100 或 docker-host-1）
     * @param port 端口号或进程标识（如：8080 或 202605190852-12345）
     * @return 工作节点实体对象，如果未找到则返回 null
     * @note 该方法用于：
     * 1. 节点重启时，检查是否已有对应的 Worker ID 记录
     * 2. 避免同一节点重复注册导致 Worker ID 浪费
     * 3. 支持节点状态恢复（如获取上次的心跳时间）
     */
    @Override
    public WorkerNode getWorkerNodeByHostPort(String host, String port) {
        return null;
    }

    /**
     * 新增工作节点记录
     * Pod方式时使用主机名进行hash取模
     *
     * @param workerNode 工作节点实体对象，包含主机名、端口等信息
     * @throws RuntimeException 如果数据库操作失败（如连接超时、主键冲突未处理）
     */
    @Override
    public void addWorkerNode(WorkerNode workerNode) {
        IdGeneratorConfigProperties idGeneratorConfigProperties = SpringContextUtil.getBean(IdGeneratorConfigProperties.class);
        long id = workerNode.getHostName().hashCode() % (1L << idGeneratorConfigProperties.getWorkerIdBits());
        log.info("Use PodWorkerIdNodeRepository To Add WorkerNode :{}", workerNode);
        workerNode.setId(id);
    }
}
