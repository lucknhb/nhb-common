package com.nhb.common.mybatis.core;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.nhb.common.id.service.CachedIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/20 8:49
 * @description:
 */
@Slf4j
@RequiredArgsConstructor
public class NhbIdentifierGenerator implements IdentifierGenerator {
    private final CachedIdGenerator  cachedIdGenerator;
    /**
     * 生成Id
     *
     * @param entity 实体
     * @return id
     */
    @Override
    public Number nextId(Object entity) {
        return cachedIdGenerator.getID();
    }
}
