package com.nhb.common.mybatis.enums;

import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.mybatis.core.DataScopeSqlTemplateService;
import com.nhb.common.mybatis.helper.DataPermissionHelper;
import com.nhb.common.security.core.UserDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/19 16:41
 * @description: 数据权限类型枚举
 * <p>
 * 支持使用 SpEL 模板表达式定义 SQL 查询条件<BR/>
 * 内置数据：
 * - {@code currentUser}: 当前登录用户信息 {@link UserDetail}<BR/>
 * 内置服务：
 * - {@code dataScopeSqlTemplate}: 系统数据权限服务
 * 如需扩展数据，可以通过 {@link DataPermissionHelper} 进行操作
 * 如需扩展服务，可以通过 {@link DataScopeSqlTemplateService} 自行编写
 */
@Getter
@AllArgsConstructor
public enum DataScopeType {
    /**
     * 全部数据权限
     */
    ALL("1", "", ""),

    /**
     * 自定数据权限
     */
    CUSTOM("2", " #{#deptName} IN ( #{@dataScopeSqlTemplate.getRoleCustom( #currentUser.userId )} ) ", " 1 = 0 "),

    /**
     * 部门数据权限
     */
    DEPT("3", " #{#deptName} = #{#currentUser.deptId} ", " 1 = 0 "),

    /**
     * 部门及以下数据权限
     */
    DEPT_AND_CHILD("4", " #{#deptName} IN ( #{@dataScopeSqlTemplate.getDeptAndChild( #currentUser.deptId )} )", " 1 = 0 "),

    /**
     * 仅本人数据权限
     */
    SELF("5", " #{#userName} = #{#currentUser.userId} ", " 1 = 0 "),

    /**
     * 部门及以下或本人数据权限
     */
    DEPT_AND_CHILD_OR_SELF("6", " #{#deptName} IN ( #{@dataScopeSqlTemplate.getDeptAndChild( #currentUser.deptId )} ) OR #{#userName} = #{#currentUser.userId} ", " 1 = 0 ");

    private final String code;

    /**
     * SpEL 模板表达式，用于构建 SQL 条件
     */
    private final String sqlTemplate;

    /**
     * 如果不满足 {@code sqlTemplate} 的条件，则使用此默认 SQL 表达式
     */
    private final String elseSql;

    /**
     * 根据枚举代码查找对应的枚举值
     *
     * @param code 枚举代码
     * @return 对应的枚举值，如果未找到则返回 null
     */
    public static DataScopeType findCode(String code) {
        if (StringUtil.isBlank(code)) {
            return null;
        }
        for (DataScopeType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
