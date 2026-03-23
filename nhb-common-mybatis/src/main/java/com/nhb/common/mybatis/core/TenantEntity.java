package com.nhb.common.mybatis.core;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/23 16:46
 * @description: 包含租户Id的基础类
 */
@Data
public class TenantEntity extends BaseEntity {
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;
}
