package com.nhb.common.security.core;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/20 8:27
 * @description: 用户信息
 */
@Data
public class UserDetail implements Serializable {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 部门ID <BR/>
     * 当SAAS化时可理解为公司下的部门<BR/>
     * 非SAAS化时可理解为与公司相同作用
     */
    private Long deptId;
    /**
     * 租户ID  可理解为公司
     */
    private String tenantId;
    /**
     * 用户唯一标识
     */
    private String token;
    /**
     * 所有角色
     */
    private Set<RoleDetail> roles;
    /**
     * 所有角色代码
     */
    private Set<String> roleCodes;
    /**
     * 所有权限 实际从菜单数据中权限标识汇总
     */
    private Set<String> rolePermissions;

    /**
     * 获取登录id
     */
    public String getLoginId() {
        return String.valueOf(userId);
    }

}
