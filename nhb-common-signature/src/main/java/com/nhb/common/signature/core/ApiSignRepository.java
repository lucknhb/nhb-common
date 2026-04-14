package com.nhb.common.signature.core;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/4/14 15:41
 * @description: @ApiSign 验签数据查询接口
 */
public interface ApiSignRepository {
    /**
     * 通过clientId获取对应的公钥 进行验签
     * @param clientId  客户ID
     * @return          公钥
     */
    String findPublicKeyByClientId(Long clientId);
}
