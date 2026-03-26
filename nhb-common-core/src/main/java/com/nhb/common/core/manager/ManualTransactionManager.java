package com.nhb.common.core.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/26 11:39
 * @description: 手动事务工具类
 * 支持在任意地方手动开启、提交、回滚事务，且不会影响 Spring 默认的声明式事务。
 * 每个线程最多只能有一个活跃的手动事务，重复开启会抛出异常。
 */
@Slf4j
public class ManualTransactionManager {
    private final PlatformTransactionManager transactionManager;

    // 存储当前线程的活跃事务状态
    private final ThreadLocal<TransactionStatus> currentTransactionStatus = new ThreadLocal<>();

    // 事务定义：使用 REQUIRES_NEW 传播行为，确保独立于现有事务
    private final TransactionDefinition transactionDefinition;

    public ManualTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.transactionDefinition = definition;
    }

    /**
     * 开启一个新事务。
     * 如果当前线程已经存在一个未提交/回滚的手动事务，则抛出 IllegalStateException。
     * 如果 Spring 环境中已存在事务（如 @Transactional 方法内调用），该事务会被挂起，
     * 新事务独立运行，完成后自动恢复原事务，互不影响。
     */
    public void begin() {
        if (currentTransactionStatus.get() != null) {
            throw new IllegalStateException("The current thread already has an uncommitted or rolled back manual transaction, please commit or roll back first");
        }
        TransactionStatus status = transactionManager.getTransaction(transactionDefinition);
        currentTransactionStatus.set(status);
    }

    /**
     * 提交当前手动事务。
     * 如果没有活跃的手动事务，抛出 IllegalStateException。
     */
    public void commit() {
        TransactionStatus status = currentTransactionStatus.get();
        if (status == null) {
            throw new IllegalStateException("There are no active manual transactions to commit");
        }
        try {
            transactionManager.commit(status);
        } finally {
            // 无论提交成功与否，都清除当前线程的事务记录
            currentTransactionStatus.remove();
        }
    }

    /**
     * 回滚当前手动事务。
     * 如果没有活跃的手动事务，抛出 IllegalStateException。
     */
    public void rollback() {
        TransactionStatus status = currentTransactionStatus.get();
        if (status == null) {
            throw new IllegalStateException("There are no active manual transactions to roll back");
        }
        try {
            transactionManager.rollback(status);
        } finally {
            currentTransactionStatus.remove();
        }
    }

    /**
     * 检查当前线程是否有活跃的手动事务。
     */
    public boolean isActive() {
        return currentTransactionStatus.get() != null;
    }
}
