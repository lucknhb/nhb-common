package com.nhb.common.excel.core;

import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 16:07
 * @description: excel返回对象
 */
public interface ExcelResult<T> {
    /**
     * 对象列表
     */
    List<T> getList();

    /**
     * 错误列表
     */
    List<String> getErrorList();

    /**
     * 导入回执
     */
    String getAnalysis();
}
