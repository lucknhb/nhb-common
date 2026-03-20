package com.nhb.common.mybatis.handler;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.handlers.PostInitTableInfoHandler;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import org.apache.ibatis.session.Configuration;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/20 15:51
 * @description: 修改表信息初始化方式
 * 目前用于全局修改是否使用逻辑删除
 */
public class PostInitTableInfoPlusHandler implements PostInitTableInfoHandler {

    @Override
    public void postTableInfo(TableInfo tableInfo, Configuration configuration) {
        String flag = SpringUtil.getProperty("mybatis-plus.enableLogicDelete", "true");
        // 只有关闭时 统一设置false 为true时mp自动判断不处理
        if (!Convert.toBool(flag)) {
            ReflectUtil.setFieldValue(tableInfo, "withLogicDelete", false);
        }
    }
}
