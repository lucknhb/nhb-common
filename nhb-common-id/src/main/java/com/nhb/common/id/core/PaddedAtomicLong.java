package com.nhb.common.id.core;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 15:43
 * @description: 通过缓存行填充（Cache Line Padding）来避免伪共享，从而提升高并发下的性能<BR/>
 * 在 CPU 多核架构中，缓存是以 缓存行（Cache Line） 为单位进行数据交换的，通常大小为 64 字节。<BR/>
 * 当两个不同的变量（如 AtomicLong 实例）恰好位于同一个缓存行中时，如果两个 CPU 核心分别修改这两个变量，<BR/>
 * 就会导致缓存行在核心之间频繁失效和重新加载，造成严重的性能损失。这就是伪共享<BR/>
 * <BR/>
 * 实现原理:
 * - CPU 缓存行通常为 64 字节。
 * - 在 AtomicLong 的 value 字段前后填充 6 个 long 类型字段（共 48 字节），
 *   加上对象头（约 12 字节）和 value 字段本身（8 字节），
 *   使得该对象总大小超过 64 字节，确保 value 字段独占一个缓存行。
 * - 这样，当多个线程分别修改不同的 PaddedAtomicLong 实例时，
 *   不会因为缓存行共享而导致性能下降。
 *
 */
public class PaddedAtomicLong extends AtomicLong implements Serializable {
    /**
     * 缓存行填充字段（6 个 long，共 48 字节）<BR/>
     *
     * 这些字段没有实际业务含义，仅用于内存填充。<BR/>
     * 目的是将 AtomicLong 的 value 字段与相邻对象隔离开，<BR/>
     * 确保 value 字段独占一个 CPU 缓存行。<BR/>
     *
     * 内存布局说明：<BR/>
     * - 对象头（Object Header）：约 12 字节（64 位 JVM，开启指针压缩）<BR/>
     * - value 字段（继承自 AtomicLong）：8 字节<BR/>
     * - p1 到 p6 填充字段：6 * 8 = 48 字节<BR/>
     * - 总计：12 + 8 + 48 = 68 字节（超过 64 字节缓存行大小）<BR/>
     *
     * 注意：p6 初始化为 7L 是为了防止 JIT 编译器优化掉这些“无用”字段
     */
    public volatile long p1, p2, p3, p4, p5, p6 = 7L;

    /**
     * 无参构造函数
     * 初始值为 0
     */
    public PaddedAtomicLong() {
        super();
    }

    /**
     * 带初始值的构造函数
     *
     * @param initialValue 原子变量的初始值
     */
    public PaddedAtomicLong(long initialValue) {
        super( initialValue );
    }

    /**
     * 防止 GC 优化掉填充字段<BR/>
     *
     * 作用：<BR/>
     * - JVM 的垃圾回收器和 JIT 编译器可能会认为 p1-p6 是“无用”字段，<BR/>
     *   从而在优化过程中移除它们，导致填充失效。<BR/>
     * - 该方法通过“使用”这些字段（求和），强制 JVM 保留它们。<BR/>
     *
     * 使用方式：<BR/>
     * - 在对象生命周期内至少调用一次该方法。<BR/>
     * - 通常可以在 toString()、hashCode() 或 equals() 方法中调用。<BR/>
     *
     * @return 所有填充字段的和（无实际业务意义）
     */
    public long sumPaddingToPreventOptimization() {
        return p1 + p2 + p3 + p4 + p5 + p6;
    }
}
