package com.nhb.common.file.hash;

import com.nhb.common.file.constant.FileStorageConstants;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 12:29
 * @description: 哈希计算器接口
 */
public interface HashCalculator {
    /**
     * 获取哈希名称，例如 MD5、SHA1、SHA256等，详情{@link FileStorageConstants.Hash}
     */
    String getName();

    /**
     * 获取哈希值，一般情况下获取后将不能继续增量计算哈希
     */
    String getValue();

    /**
     * 增量计算哈希
     * @param bytes 字节数组
     */
    void update(byte[] bytes);
}
