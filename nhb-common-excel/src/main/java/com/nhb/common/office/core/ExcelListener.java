package com.nhb.common.office.core;

import org.apache.fesod.sheet.read.listener.ReadListener;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 16:09
 * @description: Excel 导入监听
 */
public interface ExcelListener<T> extends ReadListener<T> {
    ExcelResult<T> getExcelResult();
}
