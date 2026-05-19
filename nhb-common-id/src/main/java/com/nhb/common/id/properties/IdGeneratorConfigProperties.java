package com.nhb.common.id.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.io.Serializable;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 11:45
 * @description: ID生成配置项
 */
@Data
@RefreshScope
@ConfigurationProperties(prefix = IdGeneratorConfigProperties.PREFIX)
public class IdGeneratorConfigProperties implements Serializable {
    public static final String PREFIX = "id-generator";
    /**
     * workId/MachineId生成方式
     */
    private String machineIdGeneratorType;
    /**
     * 纪元年月日 格式例如： 2026-05-19
     */
    private String epochDate;
    /**
     * 时间戳占用的位数
     */
    private int timestampBits;
    /**
     * 工作节点ID占用的位数
     */
    private int workerIdBits;
    /**
     * 序列号占用的位数
     */
    private int sequenceBits;
    /**
     * 百分比值 (0-100)，当剩余ID低于此百分比时触发填充
     */
    private int paddingFactor;
    /**
     * 定时填充间隔（秒），若为空则使用惰性填充（仅在阈值触发时填充）
     */
    private long scheduleInterval;

}
