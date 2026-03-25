package com.nhb.common.excel.core;

import java.util.Set;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 16:10
 * @description: Excel下拉选项数据提供接口
 */
public interface ExcelOptionsProvider {
    /**
     * 获取下拉选项数据
     *
     * @return 下拉选项列表
     */
    Set<String> getOptions();
}
