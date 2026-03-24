package com.nhb.common.security.utils;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.nhb.common.security.constant.SystemUserConstants;
import com.nhb.common.security.core.UserDetail;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.Set;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/20 8:45
 * @description: 用户工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserContextUtil {
    public static final String CURRENT_USER = "currentUser";
    public static final String TENANT_ID = "tenantId";
    public static final String USER_ID = "userId";
    public static final String DEPT_ID = "deptId";
    public static final String CLIENT_ID = "clientId";

    /**
     * 缓存额外信息
     *
     * @param loginUser 登录用户信息
     * @param model     配置参数
     */
    public static void login(UserDetail loginUser, SaLoginParameter model) {
        model = ObjectUtil.defaultIfNull(model, new SaLoginParameter());
        StpUtil.login(loginUser.getLoginId(),
                model.setExtra(TENANT_ID, loginUser.getTenantId())
                        .setExtra(USER_ID, loginUser.getUserId())
                        .setExtra(DEPT_ID, loginUser.getDeptId())

        );
        SaSession session = StpUtil.getSessionByLoginId(loginUser.getLoginId());
        session.set(CURRENT_USER, loginUser);
    }

    /**
     * 刷新用户信息 主要用于用户权限更新后的操作
     * @param loginUser  需要刷新的用户信息
     */
    public static void reflushUserDetail(UserDetail loginUser) {
        StpUtil.getSessionByLoginId(loginUser.getLoginId()).set(CURRENT_USER, loginUser);
    }

    /**
     * 获取用户(多级缓存)
     */
    @SuppressWarnings("unchecked")
    public static <T extends UserDetail> T getCurrentUserDetail() {
        SaSession session = StpUtil.getSession();
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        return (T) session.get(CURRENT_USER);
    }

    /**
     * 获取用户基于userId
     */
    @SuppressWarnings("unchecked cast")
    public static <T extends UserDetail> T getLoginUser(Long userId) {
        SaSession session = StpUtil.getSessionByLoginId(userId);
        if (ObjectUtil.isNull(session)) {
            return null;
        }
        return (T) session.get(CURRENT_USER);
    }

    /**
     * 获取用户id
     */
    public static Long getUserId() {
        return Convert.toLong(getExtra(USER_ID));
    }

    /**
     * 获取部门ID
     */
    public static Long getDeptId() {
        return Convert.toLong(getExtra(DEPT_ID));
    }

    /**
     * 获取租户ID
     */
    public static Long getTenantId() {
        return Convert.toLong(getExtra(TENANT_ID));
    }

    /**
     * 检查当前用户是否已登录
     *
     * @return 结果
     */
    public static boolean isLogin() {
        try {
            StpUtil.checkLogin();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取角色CODE列表
     * @return 结果
     */
    public static Set<String> getRoleCodes() {
        UserDetail userDetail = getCurrentUserDetail();
        return Objects.requireNonNull(userDetail).getRoleCodes();
    }

    /**
     * 获取权限列表
     * @return 结果
     */
    public static Set<String> getPermissions() {
        UserDetail userDetail = getCurrentUserDetail();
        return Objects.requireNonNull(userDetail).getRolePermissions();
    }

    /**
     * 当前用户是否为超级管理员
     *
     * @return 结果
     */
    public static boolean isSuperAdmin() {
        UserDetail userDetail = getCurrentUserDetail();
        return Objects.requireNonNull(userDetail).getRoles()
                .stream()
                .anyMatch(roleDetail -> SystemUserConstants.SUPER_ADMIN_ROLE_ID.equals(roleDetail.getId()));
    }

    /**
     * 当前用户是否为租户管理员
     * @return  结果值
     */
    public static boolean isTenantAdmin() {
        UserDetail userDetail = getCurrentUserDetail();
        return Objects.requireNonNull(userDetail).getRoles()
                .stream()
                .anyMatch(roleDetail -> SystemUserConstants.TENANT_ADMIN_ROLE_CODE.equals(roleDetail.getRoleCode()));
    }


    /**
     * 获取当前 Token 的扩展信息
     *
     * @param key 键值
     * @return 对应的扩展数据
     */
    private static Object getExtra(String key) {
        try {
            return StpUtil.getExtra(key);
        } catch (Exception e) {
            return null;
        }

    }

}
