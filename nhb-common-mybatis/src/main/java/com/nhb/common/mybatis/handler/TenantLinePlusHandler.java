package com.nhb.common.mybatis.handler;

import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.mybatis.helper.TenantHelper;
import com.nhb.common.mybatis.properties.TenantConfigProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.StringValue;

import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/23 16:06
 * @description: 自定义租户处理器
 */
@Slf4j
@AllArgsConstructor
public class TenantLinePlusHandler implements TenantLineHandler {

    private final TenantConfigProperties tenantConfigProperties;

    @Override
    public Expression getTenantId() {
        String tenantId = TenantHelper.getTenantId();
        if (StringUtil.isBlank(tenantId)) {
            log.error("无法获取有效的租户id -> Null");
            return new NullValue();
        }
        // 返回固定租户
        return new StringValue(tenantId);
    }

    @Override
    public boolean ignoreTable(String tableName) {
        String tenantId = TenantHelper.getTenantId();
        // 判断是否有租户
        if (StringUtil.isNotBlank(tenantId)) {
            // 不需要过滤租户的表
            List<String> excludes = tenantConfigProperties.getExcludeTables();
            return StringUtil.equalsAnyIgnoreCase(tableName, excludes.toArray(new String[0]));
        }
        return true;
    }
}
