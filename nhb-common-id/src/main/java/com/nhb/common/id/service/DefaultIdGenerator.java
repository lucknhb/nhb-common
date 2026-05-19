package com.nhb.common.id.service;

import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.id.core.BitsAllocator;
import com.nhb.common.id.core.IdGenerator;
import com.nhb.common.id.core.WorkerIdAssigner;
import com.nhb.common.id.exception.IdGeneratorException;
import com.nhb.common.id.properties.IdGeneratorConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 15:21
 * @description: 默认的 ID 生成器实现<BR/>
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultIdGenerator implements IdGenerator, InitializingBean {

    private final WorkerIdAssigner workerIdAssigner;
    private final IdGeneratorConfigProperties idGeneratorConfigProperties;
    /**
     * 纪元日期配置
     * 作用：将系统时间的起点往前移，从而减少时间戳部分占用的位数。
     */
    protected String epochStr;
    protected long epochMilliseconds;

    /**
     * 核心组件与分配结果
     */
    protected BitsAllocator bitsAllocator; // 位分配器，负责具体的位运算
    protected long workerId;               // 当前节点的 Worker ID

    /**
     * 状态变量 (用于 nextId 的同步控制)
     */
    protected long sequence = 0L;          // 当前毫秒内的序列号
    protected long lastTimestamp = -1L;    // 上次生成 ID 的时间戳(毫秒)


    /**
     * Spring Bean 初始化回调
     * 在 Bean 属性设置完成后，初始化位分配器和 Worker ID。
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 1. 初始化位分配器，根据配置的 timeBits, workerBits, seqBits 进行位运算准备
        bitsAllocator = new BitsAllocator(idGeneratorConfigProperties.getTimestampBits(), idGeneratorConfigProperties.getWorkerIdBits(), idGeneratorConfigProperties.getSequenceBits());
        // 2. 通过 WorkerIdAssigner 获取当前节点的 Worker ID
        workerId = workerIdAssigner.assignWorkerId();
        // 3. 校验获取到的 Worker ID 是否超出配置的位数限制
        if (workerId > bitsAllocator.getMaxWorkerId()) {
            throw new RuntimeException("Worker id " + workerId + " exceeds the max " + bitsAllocator.getMaxWorkerId());
        }
        setEpochStr(idGeneratorConfigProperties.getEpochDate());
        log.info("Initialized bits(1, {}, {}, {}) for workerID:{}", idGeneratorConfigProperties.getTimestampBits(), idGeneratorConfigProperties.getWorkerIdBits(), idGeneratorConfigProperties.getSequenceBits(), workerId);
    }

    /**
     * 获取全局唯一 ID
     *
     * @return ID
     * @throws IdGeneratorException 包装底层生成异常
     */
    @Override
    public long getID() {
        try {
            return nextId();
        } catch (Exception e) {
            log.error("Generate Unique Id Exception. ", e);
            throw new IdGeneratorException(e);
        }
    }

    /**
     * 解析 ID (反向解析)
     * <p>
     * 作用：将生成的 64 位 ID 拆解，还原出生成该 ID 时的时间、机器 ID 和序列号。
     * 用途：在宁波的电商或物流系统中，常用于通过订单 ID 追溯生成该订单的服务器节点
     * 或者排查时间回拨问题。
     *
     * @param id 64位ID
     * @return JSON格式的解析结果字符串
     */
    @Override
    public String parseID(long id) {
        // 获取各部分位数
        long totalBits = BitsAllocator.TOTAL_BITS; // 64
        long signBits = bitsAllocator.getSignBits(); // 1
        long timestampBits = bitsAllocator.getTimestampBits(); // 41
        long workerIdBits = bitsAllocator.getWorkerIdBits(); // 10
        long sequenceBits = bitsAllocator.getSequenceBits(); // 12

        // 位运算解析 (利用移位和掩码)
        // 解析序列号：左移将高位丢弃，右移保留低位
        long sequence = (id << (totalBits - sequenceBits)) >>> (totalBits - sequenceBits);

        // 解析 Worker ID：左移跳过时间戳，右移保留 Worker ID 部分
        long workerId = (id << (timestampBits + signBits)) >>> (totalBits - workerIdBits);

        // 解析时间戳：右移跳过 Worker ID 和 Sequence
        long deltaMilliseconds = id >>> (workerIdBits + sequenceBits);

        // 还原绝对时间
        // 1. 计算绝对毫秒值：纪元毫秒 + 偏移毫秒
        long absoluteMilliseconds = epochMilliseconds + deltaMilliseconds;
        // 2. 通过 Instant 创建时间点 (UTC 时间)
        Instant instant = Instant.ofEpochMilli(absoluteMilliseconds);
        // 3. 转换为宁波所在时区 (Asia/Shanghai, UTC+8)
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        ZonedDateTime zonedDateTime = instant.atZone(shanghaiZone);
        // 4. 格式化为可读字符串
        // 使用 DateTimeFormatter 替代 SimpleDateFormat (线程安全)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        String thatTimeStr = zonedDateTime.format(formatter);
        // 格式化输出
        return String.format("{\"ID\":\"%d\",\"timestamp\":\"%s\",\"workerId\":\"%d\",\"sequence\":\"%d\"}",
                id, thatTimeStr, workerId, sequence);
    }

    /**
     * 核心 ID 生成逻辑 (线程安全)
     *
     * @return 64位ID
     * @throws IdGeneratorException 时间回拨或时间戳溢出
     */
    protected synchronized long nextId() {
        long currentTimestamp = getCurrentMillisecond(); // 获取当前毫秒级时间戳

        // 1. 时间回拨检测 (Critical!)
        // 如果系统时间回拨，拒绝生成 ID 以防止重复
        if (currentTimestamp < lastTimestamp) {
            long refusedMilliseconds = lastTimestamp - currentTimestamp;
            throw new IdGeneratorException("Clock moved backwards. Refusing for %d milliseconds", refusedMilliseconds);
        }

        // 2. 同一毫秒内并发处理
        if (currentTimestamp == lastTimestamp) {
            // 序列号自增，并与最大序列号进行掩码与运算 (保证不超限)
            sequence = (sequence + 1) & bitsAllocator.getMaxSequence();

            // 如果序列号溢出 (达到最大值后归零)
            if (sequence == 0) {
                // 阻塞等待下一毫秒
                currentTimestamp = getNextMillisecond(lastTimestamp);
            }
        } else {// 3. 跨毫秒处理
            // 新的一毫秒，序列号重置为 0
            sequence = 0L;
        }
        // 更新最后生成时间
        lastTimestamp = currentTimestamp;
        // 4. 分配位并生成最终 ID
        // 将 delta milliseconds, workerId, sequence 按照预定的位数拼装成 64 位 Long
        return bitsAllocator.allocate(currentTimestamp - epochMilliseconds, workerId, sequence);
    }

    /**
     * 自旋等待下一毫秒
     * 用于当序列号用尽时，等待时间进入下一毫秒。
     */
    private long getNextMillisecond(long lastTimestamp) {
        long timestamp = getCurrentMillisecond();
        // 自旋直到时间戳发生变化
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentMillisecond();
        }
        return timestamp;
    }

    /**
     * 获取当前毫秒级时间戳，并进行有效性检查
     */
    private long getCurrentMillisecond() {
        long currentMillisecond = System.currentTimeMillis();
        // 检查时间戳位数是否已耗尽 (基于配置的 timeBits 和 epoch)
        if (currentMillisecond - epochMilliseconds > bitsAllocator.getMaxDeltaMilliseconds()) {
            throw new IdGeneratorException("Timestamp bits is exhausted. Refusing UID generate. Now: " + currentMillisecond);
        }
        return currentMillisecond;
    }

    /**
     * 设置纪元日期字符串
     * <p>
     * 作用：设置自定义纪元日期，用于计算时间戳偏移量。
     * 纪元日期是指 ID 生成器首次上线的时间，所有生成的时间戳都是相对于这个时间的偏移量。
     * <p>
     *
     * @param epochStr 纪元日期字符串，格式为 "yyyy-MM-dd"，例如 "2024-01-01"
     */
    public void setEpochStr(String epochStr) {
        Assert.hasLength(epochStr, "Epoch str must not be null or empty,Please setting [id-generator.epoch-date]");
        if (StringUtil.isNotBlank(epochStr)) {
            try {
                // 1. 定义日期格式 (线程安全)
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                // 2. 解析日期字符串为 LocalDate (无时区日期)
                LocalDate localDate = LocalDate.parse(epochStr, dateFormatter);
                // 3. 转换为所在时区 (Asia/Shanghai, UTC+8) 的当天开始时间
                ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
                ZonedDateTime zonedDateTime = localDate.atStartOfDay(shanghaiZone);
                // 4. 转换为 Instant (UTC 时间戳)
                Instant instant = zonedDateTime.toInstant();
                // 5. 获取毫秒值
                this.epochMilliseconds = instant.toEpochMilli();
                this.epochStr = epochStr;
                log.info("Set epoch to: {} ({} ms)", epochStr, this.epochMilliseconds);
            } catch (DateTimeParseException e) {
                // 如果日期格式不正确，记录错误日志并保留原值
                log.error("Invalid epoch date format: {}. Expected format: yyyy-MM-dd", epochStr, e);
                throw new IllegalArgumentException("Invalid epoch date format: " + epochStr, e);
            }
        }
    }
}
