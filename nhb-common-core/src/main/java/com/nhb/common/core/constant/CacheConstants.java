package com.nhb.common.core.constant;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/14 9:08
 * @description: 缓存常用常量
 */
public interface CacheConstants {
    /**
     * 在线用户 redis key
     */
    String ONLINE_TOKEN_KEY = "online_tokens:";

    /**
     * 参数管理 cache key
     */
    String SYS_CONFIG_KEY = "sys_config:";

    /**
     * 字典管理 cache key
     */
    String SYS_DICT_KEY = "sys_dict:";

    /**
     * 登录账户密码错误次数 redis key
     */
    String PWD_ERR_CNT_KEY = "pwd_err_cnt:";
}
