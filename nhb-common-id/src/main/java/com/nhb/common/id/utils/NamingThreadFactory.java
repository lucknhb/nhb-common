package com.nhb.common.id.utils;

import com.nhb.common.core.utils.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ClassUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 16:21
 * @description: 命名线程工厂
 * <p>
 * 作用：用于创建具有特定名称、守护状态和异常处理机制的线程。
 * 特性：
 * 1. 自动检测调用者类名：如果未指定线程名前缀，会自动通过栈帧查找调用者的简单类名。
 * 2. 线程命名序列化：同一名字前缀的线程会自动追加递增数字（如 MyTask-1, MyTask-2）。
 * 3. 异常安全兜底：如果未设置自定义处理器，默认会记录错误日志，防止线程因未捕获异常而“悄悄”终止。
 */
@Data
@Slf4j
public class NamingThreadFactory implements ThreadFactory {
    /**
     * 线程名称前缀
     * 例如：如果设置为 "ID-Generator"，生成的线程名可能是 "ID-Generator-1"
     */
    private String name;
    /**
     * 是否为守护线程
     * true: 守护线程（随主线程结束而结束）
     * false: 用户线程（JVM会等待其执行完毕）
     */
    private boolean daemon;
    /**
     * 未捕获异常处理器
     * 用于处理线程 run() 方法中抛出的未捕获异常。
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    /**
     * 序列号生成器容器
     * Key: 线程名前缀 (String)
     * Value: 该前缀下的当前序列号 (AtomicLong)
     * 作用：保证同名线程池创建的线程具有唯一递增的后缀。
     */
    private final ConcurrentHashMap<String, AtomicLong> sequences;

    /**
     * 默认构造函数：非守护线程，无特定名称，无自定义异常处理器
     */
    public NamingThreadFactory() {
        this(null, false, null);
    }

    public NamingThreadFactory(String name) {
        this(name, false, null);
    }

    public NamingThreadFactory(String name, boolean daemon) {
        this(name, daemon, null);
    }

    public NamingThreadFactory(String name, boolean daemon, Thread.UncaughtExceptionHandler handler) {
        this.name = name;
        this.daemon = daemon;
        this.uncaughtExceptionHandler = handler;
        this.sequences = new ConcurrentHashMap<String, AtomicLong>();
    }

    /**
     * 创建新线程
     *
     * @param r 任务 Runnable
     * @return 配置好的 Thread 实例
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        // 1. 设置守护状态
        thread.setDaemon(this.daemon);

        // 2. 确定线程名称前缀
        // 如果外部未指定 name，则尝试自动获取调用者的类名（性能开销较大，仅在未配置时使用）
        String prefix = this.name;
        if (StringUtil.isBlank(prefix)) {
            // getInvoker(2) 表示栈深度为2，即跳过当前方法和newThread方法，获取真正的调用者
            prefix = getInvoker(2);
        }
        // 3. 设置线程完整名称：前缀 + 序列号
        // 例如：ID-Generator-1
        thread.setName(prefix + "-" + getSequence(prefix));

        // 4. 设置异常处理器
        if (this.uncaughtExceptionHandler != null) {
            thread.setUncaughtExceptionHandler(this.uncaughtExceptionHandler);
        } else {
            // 默认处理器：仅记录错误日志
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    log.error("Unhandled exception in thread: {}:{}", t.threadId(), t.getName(), e);
                }
            });
        }

        return thread;
    }

    /**
     * 获取调用者的类名 (辅助方法)
     * 通过抛出一个临时异常并解析其栈轨迹来获取调用类名。
     *
     * @param depth 栈深度，0为当前方法，数值越大越往外层调用追溯
     * @return 调用者的简单类名 (Simple Class Name)
     */
    private String getInvoker(int depth) {
        Exception e = new Exception();
        StackTraceElement[] steps = e.getStackTrace();
        // 检查栈深度是否合法
        if (steps.length > depth) {
            // 从栈帧中提取类名，并去除包路径（仅保留类名）
            return ClassUtils.getShortClassName(steps[depth].getClassName());
        }
        // 如果无法获取调用者，返回当前类名
        return getClass().getSimpleName();
    }

    /**
     * 获取指定前缀的下一个序列号
     * 线程安全的获取或创建序列计数器
     *
     * @param invoker 名称前缀
     * @return 下一个可用的序列号
     */
    private long getSequence(String invoker) {
        AtomicLong r = this.sequences.get(invoker);
        if (r == null) {
            r = new AtomicLong(0);
            // putIfAbsent 确保多线程环境下只有一个计数器生效
            AtomicLong previous = this.sequences.putIfAbsent(invoker, r);
            if (previous != null) {
                r = previous;
            }
        }
        // 原子性递增并返回新值
        return r.incrementAndGet();
    }
}
