package com.nhb.common.generator.test;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/30 17:17
 * @description:
 */
@TableName("test_table")
public class TestEntity {
    @TableId
    private Long id;
}
