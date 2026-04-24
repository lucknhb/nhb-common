package com.nhb.common.rocketmq.hook;

import com.nhb.common.rocketmq.annotation.RocketMQConsumer;
import com.nhb.common.rocketmq.register.RocketMQConsumerRegistry;
import com.nhb.common.rocketmq.utils.TopicUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.remoting.CommandCustomHeader;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.protocol.RemotingCommand;
import org.apache.rocketmq.remoting.protocol.RequestCode;
import org.apache.rocketmq.remoting.protocol.header.SendMessageRequestHeader;
import org.apache.rocketmq.remoting.protocol.header.SendMessageRequestHeaderV2;
import org.apache.rocketmq.remoting.rpc.TopicQueueRequestHeader;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/16 16:36
 * @description: 处理topic后缀
 */
@Slf4j
public class TopicSuffixRPCHook implements RPCHook {


    @Override
    public void doBeforeRequest(String remoteAddr, RemotingCommand request) {
        // 仅在发送消息的请求时进行处理
        List<Integer> sendMessageCodes = List.of(RequestCode.SEND_MESSAGE, RequestCode.SEND_MESSAGE_V2, RequestCode.SEND_BATCH_MESSAGE);
        if (sendMessageCodes.contains(request.getCode())) {
            CommandCustomHeader header = request.readCustomHeader();
            if (header instanceof SendMessageRequestHeader || header instanceof SendMessageRequestHeaderV2) {
                TopicQueueRequestHeader topicQueueRequestHeader = (TopicQueueRequestHeader) header;
                String originalTopic = topicQueueRequestHeader.getTopic();
                String newTopic = TopicUtil.topicSuffix(originalTopic);
                log.info("RocketMQ Send Message Topic Change From {}  To {}", originalTopic, newTopic);
                // 更新请求头中的Topic名称
                topicQueueRequestHeader.setTopic(newTopic);
            }
        }
    }

    /**
     * 监听时候的TOPIC 已在 {@link RocketMQConsumerRegistry#registerConsumerMethod(Object, Method, RocketMQConsumer)} 设置
     *
     * @param remoteAddr
     * @param request
     * @param response
     */
    @Override
    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {

    }
}
