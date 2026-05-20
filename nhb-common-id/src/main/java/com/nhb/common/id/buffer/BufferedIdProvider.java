package com.nhb.common.id.buffer;

import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 15:32
 * @description: 缓冲的ID提供者（支持Lambda），在同一毫秒内提供ID
 */
@FunctionalInterface
public interface BufferedIdProvider {

    /**
     * 在同一毫秒内提供ID集合
     *
     * @param milliseconds 毫秒
     * @return id集合
     */
    List<Long> provide(long milliseconds);
}
