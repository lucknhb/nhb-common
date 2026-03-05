package com.nhb.common.security.service;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaSessionCustomUtil;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.nhb.common.security.constant.SecurityConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/5 11:44
 * @description: sa-token 权限管理实现类
 */
@Slf4j
public class DefaultSaTokenPermissionService implements StpInterface {
    /**
     * 返回指定账号id所拥有的权限码集合
     *
     * @param loginId   账号id
     * @param loginType 账号类型
     * @return 该账号id具有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<String> roles = getRoleList(loginId, loginType);
        // 权限码集合容器
        List<String> permissions = new ArrayList<>();
        Optional.ofNullable(roles).ifPresent(list -> list.forEach(roleId -> {
            //获取该Session可直接赋值
            SaSession roleSession = SaSessionCustomUtil.getSessionById(SecurityConstants.USER_ROLE + roleId, Boolean.FALSE);
            if (Objects.isNull(roleSession)) {
                throw new NotLoginException("权限已过期，请重新登录",loginType,null);
            }
            List<String> permissionCodes = (List<String>) roleSession.get(SecurityConstants.USER_PERMISSIONS);
            permissions.addAll(permissionCodes);
        }));
        return permissions;
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
        SaSession session = StpUtil.getSessionByLoginId(loginId);
        return (List<String>) session.get(SecurityConstants.USER_ROLES);
    }
}
