package com.nhb.common.mybatis.handler;

import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.nhb.common.core.exception.ServiceException;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.security.utils.UserContextUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/20 14:15
 * @description: 注入处理器
 */
@Slf4j
public class InjectionMetaObjectHandler implements MetaObjectHandler {
    private static final String CREATE_USER_ID = "createUserId";
    private static final String UPDATE_USER_ID = "updateUserId";
    private static final String CREATE_TIME = "createTime";
    private static final String UPDATE_TIME = "updateTime";
    private static final String DEPT_ID = "deptId";
    private static final String TENANT_Id = "tenantId";
    /**
     * 如果用户不存在默认注入-1代表无用户
     */
    private static final Long DEFAULT_USER_ID = -1L;

    /**
     * 插入元对象字段填充（用于插入时对公共字段的填充）
     *
     * @param metaObject 元对象 用于获取原始对象并进行填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            String tenantId = metaObject.findProperty(TENANT_Id, true);
            if (StringUtil.isNotBlank(tenantId)){
                this.strictInsertFill(metaObject, TENANT_Id, Long.class, UserContextUtil.getTenantId());
            }
            String deptId = metaObject.findProperty(DEPT_ID, true);
            if (StringUtil.isNotBlank(deptId)){
                this.strictInsertFill(metaObject, DEPT_ID, Long.class, UserContextUtil.getDeptId());
            }
            String createUserId = metaObject.findProperty(CREATE_USER_ID, true);
            if (StringUtil.isNotBlank(createUserId)){
                this.strictInsertFill(metaObject, CREATE_USER_ID, Long.class, UserContextUtil.getUserId());
            }
            String updateUserId = metaObject.findProperty(UPDATE_USER_ID, true);
            if (StringUtil.isNotBlank(updateUserId)){
                this.strictInsertFill(metaObject, UPDATE_USER_ID, Long.class, UserContextUtil.getUserId());
            }
            String createTime = metaObject.findProperty(CREATE_TIME, true);
            if (StringUtil.isNotBlank(createTime)){
                this.strictInsertFill(metaObject, CREATE_TIME, LocalDateTime.class, LocalDateTime.now());
            }
            String updateTime = metaObject.findProperty(UPDATE_TIME, true);
            if (StringUtil.isNotBlank(updateTime)){
                this.strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
            }
        } catch (Exception e) {
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    /**
     * 更新元对象字段填充（用于更新时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        String updateUserId = metaObject.findProperty(UPDATE_USER_ID, true);
        if (StringUtil.isNotBlank(updateUserId)){
            this.strictInsertFill(metaObject, UPDATE_USER_ID, Long.class, UserContextUtil.getUserId());
        }
        String updateTime = metaObject.findProperty(UPDATE_TIME, true);
        if (StringUtil.isNotBlank(updateTime)){
            this.strictInsertFill(metaObject, UPDATE_TIME, LocalDateTime.class, LocalDateTime.now());
        }
    }
}
