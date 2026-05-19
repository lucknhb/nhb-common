package com.nhb.common.id.constants;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 14:09
 * @description: 机器ID 生成策略
 */
public interface MachineIdGeneratorType {
    /**
     * K8S环境下可通过
     */
    String POD = "POD";
    /**
     * 使用数据库存储
     */
    String DB = "DB";
    /**
     * 使用redis存储
     */
    String REDIS =  "REDIS";
}
