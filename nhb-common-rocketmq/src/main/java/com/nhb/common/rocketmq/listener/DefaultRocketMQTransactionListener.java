package com.nhb.common.rocketmq.listener;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.HashSet;
import java.util.Set;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/13 14:31
 * @description:  事务监听<BR/>
 * 发送半消息：生产者发送一条“半消息”（Half Message）到 Broker，此时消息对消费者不可见<BR/>
 * 执行本地事务：Broker 确认半消息存储成功后，回调TransactionListener.executeLocalTransaction，执行本地数据库事务<BR/>
 * 提交/回滚消息：<BR/>
 * 如果本地事务执行成功，返回COMMIT_MESSAGE，Broker 将半消息转为正常消息，消费者可见<BR/>
 * 如果本地事务失败，返回ROLLBACK_MESSAGE，Broker 删除半消息<BR/>
 * 如果返回UNKNOWN，Broker 会定时回查该事务的状态<BR/>
 * 事务状态回查：Broker 定期调用checkLocalTransaction，根据返回的结果最终提交或回滚消息<BR/>
 */
public class DefaultRocketMQTransactionListener implements TransactionListener {
    //用于存储事务ID
    private final Set<String> transactionSet = new HashSet<>();

    /**
     * 执行本地业务事务
     * @param msg Half(prepare) message
     * @param arg Custom business parameter
     * @return
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        transactionSet.add(msg.getTransactionId());
        return LocalTransactionState.COMMIT_MESSAGE;
    }

    /**
     * 检查本地事务的最终结果
     * @param msg Check message
     * @return
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        boolean contains = transactionSet.contains(msg.getTransactionId());
        if (contains) {
            transactionSet.remove(msg.getTransactionId());
            return LocalTransactionState.COMMIT_MESSAGE;
        }
        return LocalTransactionState.ROLLBACK_MESSAGE;
    }
}
