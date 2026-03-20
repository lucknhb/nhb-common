package com.nhb.common.mybatis.core;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/20 16:23
 * @description:
 */
@Service("dataScopeSqlTemplate")
public class DataScopeSqlTemplateService {
    @DubboReference
    private RemoteCustomDataScopeService remoteCustomDataScopeService;

    /**
     * 自定义权限查询拼接语句
     * @param userId
     * @return
     */
    @Cacheable(cacheNames = CacheNameConstants.CUSTOM_DATA_SCOPE_SQL, key = "#userId", condition = "#userId != null")
    public String customDataScopeSql(Long userId) {
        return remoteCustomDataScopeService.getCustomSqlTemplate(userId);
    }

    /**
     * 获取部门和下级权限语句
     *
     * @param deptId 部门ID
     * @return 返回部门及其下级的权限语句，如果没有找到则返回 null
     */
    @Cacheable(cacheNames = CacheNameConstants.DATA_SCOPE_DEPT_AND_CHILD, key = "#deptId", condition = "#deptId != null")
    public String getDeptAndChild(Long deptId) {
        return remoteCustomDataScopeService.getDeptAndChild(deptId);
    }
}
