package com.nhb.common.mybatis.filter;

import com.nhb.common.mybatis.helper.DataPermissionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;

import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/19 16:54
 * @description: dubbo 数据权限参数传递
 */
@Slf4j
@Activate(group = {CommonConstants.CONSUMER})
public class DubboDataPermissionFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcServiceContext context = RpcContext.getServiceContext();
        //将当前数据隔离权限透传至下游
        Map<String, Object> dataPermissionContext = DataPermissionHelper.getContext();
        context.setObjectAttachment(DataPermissionHelper.DATA_PERMISSION_KEY, dataPermissionContext);
        return invoker.invoke(invocation);
    }
}
