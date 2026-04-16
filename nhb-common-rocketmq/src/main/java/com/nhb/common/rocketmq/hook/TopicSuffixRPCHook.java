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

import java.lang.reflect.Method;

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
        if (RequestCode.SEND_MESSAGE == request.getCode()) {
            CommandCustomHeader header = request.readCustomHeader();
            if (header instanceof SendMessageRequestHeader sendHeader) {
                String originalTopic = sendHeader.getTopic();
                String newTopic = TopicUtil.topicSuffix(originalTopic);
                log.info("RocketMQ Send Message Topic Change From {}  To {}", originalTopic, newTopic);
                // 更新请求头中的Topic名称
                sendHeader.setTopic(newTopic);
            }
        }
    }

    /**
     * 监听时候的TOPIC 已在 {@link RocketMQConsumerRegistry#registerConsumerMethod(Object, Method, RocketMQConsumer)} 设置
     * @param remoteAddr
     * @param request
     * @param response
     */
    @Override
    public void doAfterResponse(String remoteAddr, RemotingCommand request, RemotingCommand response) {

    }
}
