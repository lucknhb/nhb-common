package com.nhb.common.security.service;

import cn.dev33.satoken.stp.StpInterface;
import com.nhb.common.security.utils.UserContextUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/5 11:44
 * @description: sa-token 权限管理实现类
 */
@Slf4j
public class DefaultSaTokenPermissionService implements StpInterface {
    /**
     * 返回指定账号id所拥有的权限集合
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 该账号id具有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return new ArrayList<>(UserContextUtil.getPermissions());
    }

    /**
     * 返回指定账号id所拥有的角色标识集合
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 该账号id具有的角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return new ArrayList<>(UserContextUtil.getRoleCodes());
    }
}
