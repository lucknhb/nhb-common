package com.nhb.common.encrypt.encryptor;

import com.nhb.common.encrypt.core.EncryptContext;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/2 11:02
 * @description: 加密执行者的基类
 */
public abstract class AbstractEncryptor implements IEncryptor {

    /**
     * 用户配置校验与配置注入
     * @param context
     */
    public AbstractEncryptor(EncryptContext context) {
    }

}
