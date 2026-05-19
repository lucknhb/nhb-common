package com.nhb.common.id.core;

import com.nhb.common.id.exception.IdGeneratorException;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 15:10
 * @description: ID生成器
 */
public interface IdGenerator {
    /**
     * 获取ID
     *
     * @return ID
     * @throws IdGeneratorException
     */
    long getID() throws IdGeneratorException;

    /**
     * 将id反向解析为原始数据
     *
     * @param id ID值
     * @return 反向解析后的值
     */
    String parseID(long id);
}
