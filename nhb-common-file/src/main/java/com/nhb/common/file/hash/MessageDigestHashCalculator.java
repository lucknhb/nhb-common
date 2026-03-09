package com.nhb.common.file.hash;

import cn.hutool.core.util.HexUtil;
import com.nhb.common.file.constant.FileStorageConstants;
import com.nhb.common.file.exception.FileStorageException;

import java.security.MessageDigest;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 16:02
 * @description: 摘要信息哈希计算器，支持计算 MD5、SHA1、SHA256等<BR/>
 * 详情{@link FileStorageConstants.Hash.MessageDigest}
 */
public class MessageDigestHashCalculator implements  HashCalculator {
    /**
     * 摘要信息对象
     */
    private final MessageDigest messageDigest;
    /**
     * 哈希值
     */
    private volatile String value;

    /**
     * 构造摘要信息哈希计算器
     * @param name 哈希名称，例如 MD5、SHA1、SHA256等<BR/>
     *             详情{@link FileStorageConstants.Hash.MessageDigest}
     */
    public MessageDigestHashCalculator(String name) {
        try {
            messageDigest = MessageDigest.getInstance(name);
        } catch (Exception e) {
            throw new FileStorageException("Create StandardHashCalculator Fail，Not Support The Type：" + name, e);
        }
    }

    /**
     * 构造摘要信息哈希计算器
     * @param messageDigest 消息摘要算法
     */
    public MessageDigestHashCalculator(MessageDigest messageDigest) {
        this.messageDigest = messageDigest;
    }

    /**
     * 获取哈希名称，例如 MD5、SHA1、SHA256等<BR/>
     * 详情{@link FileStorageConstants.Hash}
     */
    @Override
    public String getName() {
        return messageDigest.getAlgorithm();
    }

    /**
     * 获取哈希值，注意获取后将不能继续增量计算哈希
     */
    @Override
    public String getValue() {
        if (value == null) {
            synchronized (this) {
                if (value == null) {
                    value = HexUtil.encodeHexStr(messageDigest.digest());
                }
            }
        }
        return value;
    }

    /**
     * 增量计算哈希
     */
    @Override
    public void update(byte[] bytes) {
        messageDigest.update(bytes);
    }
}
