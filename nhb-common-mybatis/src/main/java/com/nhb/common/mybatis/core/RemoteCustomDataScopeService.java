package com.nhb.common.mybatis.core;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/20 16:55
 * @description: 数据权限服务
 */
public interface RemoteCustomDataScopeService {

    /**
     * 获取角色自定义权限语句
     *
     * @param userId 用户ID
     * @return 自定义权限语句 可返回 null
     */
    String getCustomSqlTemplate(Long userId);

    /**
     * 获取部门和下级权限语句
     *
     * @param deptId 部门ID
     * @return 返回部门及其下级的权限语句，如果没有找到则返回 null
     */
    String getDeptAndChild(Long deptId);
}
