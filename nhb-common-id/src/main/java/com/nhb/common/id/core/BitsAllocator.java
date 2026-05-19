package com.nhb.common.id.core;

import lombok.Data;
import org.springframework.util.Assert;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 14:31
 * @description: 位分配器<P />
 * 负责将 64 位 long 型数字按规则拆分成：符号位 + 时间戳 + 工作节点ID + 序列号<BR/>
 */
@Data
public class BitsAllocator {
    /**
     * 总位数，固定为 63（因为最高位符号位固定为0）
     */
    public static final int TOTAL_BITS = 1 << 6;

    /**
     * 固定1bit符号标识，即生成的UID为正数
     */
    private final int signBits = 1;
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
     * 时间戳的最大值（用于校验）
     */
    private final long maxDeltaMilliseconds;
    /**
     * 工作节点ID的最大值（用于校验）
     */
    private final long maxWorkerId;
    /**
     * 序列号的最大值（用于校验）
     */
    private final long maxSequence;
    //以下两个偏移量可以理解为 数据从右往左的移动长度
    /**
     * 时间戳偏移量 = 工作节点长度+序列号长度
     */
    private final int timestampShift;
    /**
     * 工作节点偏移量 = 序列号长度
     */
    private final int workerIdShift;


    /**
     * 构造函数
     * @param timestampBits 时间戳位数
     * @param workerIdBits  工作节点位数
     * @param sequenceBits  序列号位数
     */
    public BitsAllocator(int timestampBits, int workerIdBits, int sequenceBits) {
        // 需要确定分配的是64位
        int allocateTotalBits = signBits + timestampBits + workerIdBits + sequenceBits;
        Assert.isTrue(allocateTotalBits == TOTAL_BITS, "Snowflake Allocate Not Enough 64 Bits");
        this.timestampBits = timestampBits;
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;
        //计算时间戳/工作节点/序列号的最大值
        //例如：timestampBits=28，则 maxDeltaSeconds = 2^28 - 1
        this.maxDeltaMilliseconds = ~(-1L << timestampBits);
        this.maxWorkerId = ~(-1L << workerIdBits);
        this.maxSequence = ~(-1L << sequenceBits);
        //计算时间戳/工作节点偏移量
        this.timestampShift = workerIdBits + sequenceBits;
        this.workerIdShift = sequenceBits;
    }

    /**
     * 将各字段组合成一个 64 位的唯一 ID
     *
     * @param deltaSeconds 相对时间戳（当前时间 - 基准时间，单位：秒）
     * @param workerId     工作节点ID
     * @param sequence     序列号
     * @return 组合后的 64 位 ID
     */
    public long allocate(long deltaSeconds, long workerId, long sequence) {
        // 1. 时间戳左移 (workerIdBits + sequenceBits) 位
        // 2. 工作节点ID左移 sequenceBits 位
        // 3. 序列号放在最低位
        // 4. 三者按位或运算组合
        return (deltaSeconds << timestampShift) | (workerId << workerIdShift) | sequence;
    }
}
