package com.nhb.common.dubbo.filter;

import cn.hutool.core.util.StrUtil;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.dubbo.properties.DubboCustomProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.service.GenericService;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/27 14:24
 * @description:  在 Provider 和 Consumer 端都生效<BR/>
 *  执行顺序设置为最大值，确保在所有其他过滤器之后执行
 */
@Slf4j
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER}, order = Integer.MAX_VALUE)
public class DubboLogFilter implements Filter {
    /**
     * Dubbo Filter 接口实现方法，处理服务调用逻辑并打印日志
     *
     * @param invoker    Dubbo 服务调用者实例
     * @param invocation 调用的具体方法信息
     * @return 调用结果
     * @throws RpcException 如果调用过程中发生异常
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        DubboCustomProperties properties = SpringContextUtil.getBean(DubboCustomProperties.class);
        // 如果未开启请求日志记录，则直接执行服务调用并返回结果
        if (!properties.getLogEnabled()) {
            return invoker.invoke(invocation);
        }
        // 判断是 Provider 还是 Consumer
        String client = CommonConstants.PROVIDER;
        if (RpcContext.getServiceContext().isConsumerSide()) {
            client = CommonConstants.CONSUMER;
        }
        // 构建基础日志信息
        String  baseLog = StrUtil.format("Dubbo Client[{}] InterfaceName={} MethodName={}", client, invoker.getInterface().getName(), invocation.getMethodName());
        log.info("DUBBO - INVOKE: {},Parameter={}", baseLog, invocation.getArguments());
        // 记录调用开始时间
        long startTime = System.currentTimeMillis();
        // 执行接口调用逻辑
        Result result = invoker.invoke(invocation);
        // 计算调用耗时
        long elapsed = System.currentTimeMillis() - startTime;
        // 如果发生异常且调用的不是泛化服务，则记录异常日志
        if (result.hasException() && !invoker.getInterface().equals(GenericService.class)) {
            log.error("DUBBO - INVOKE EXCEPTION. The Request message is :{} ,the exception message is:{}", baseLog, result.getException().getMessage());
        } else {
            // 根据日志级别输出服务响应信息
            log.info("DUBBO - 服务响应: {},SpendTime=[{}ms],Response={}", baseLog, elapsed, JacksonUtil.toJsonString(new Object[]{result.getValue()}));
        }
        return result;
    }
}
