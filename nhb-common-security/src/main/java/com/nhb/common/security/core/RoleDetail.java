package com.nhb.common.security.core;

import lombok.Data;

import java.io.Serializable;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/20 8:39
 * @description: 角色信息
 */
@Data
public class RoleDetail implements Serializable {
    /**
     * 角色ID
     */
    private Long id;
    /**
     * 角色CODE
     */
    private String roleCode;
    /**
     * 角色名称
     */
    private String roleName;
    /**
     * 角色权限 根据 ; 分割
     */
    private String rolePermission;
    /**
     * 数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限 5：仅本人数据权限 6：部门及以下或本人数据权限）
     */
    private String dataScope;
}
