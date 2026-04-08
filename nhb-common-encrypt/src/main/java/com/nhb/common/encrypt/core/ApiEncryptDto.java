package com.nhb.common.encrypt.core;

import lombok.Data;

import java.io.Serializable;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/8 17:57
 * @description: 加密接口请求参数
 */
@Data
public class ApiEncryptDto implements Serializable {
    /**
     * 加密后的数据
     */
    private String data;
}
