package com.nhb.common.excel.core;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 16:14
 * @description: 默认excel返回对象
 */
public class DefaultExcelResult<T> implements ExcelResult<T> {

    /**
     * 数据对象list
     */
    @Setter
    private List<T> list;

    /**
     * 错误信息列表
     */
    @Getter
    @Setter
    private List<String> errorMessages;

    public DefaultExcelResult() {
        this.list = new ArrayList<>();
        this.errorMessages = new ArrayList<>();
    }

    public DefaultExcelResult(List<T> list, List<String> errorMessages) {
        this.list = list;
        this.errorMessages = errorMessages;
    }

    public DefaultExcelResult(ExcelResult<T> excelResult) {
        this.list = excelResult.getList();
        this.errorMessages = excelResult.getErrorMessages();
    }

    @Override
    public List<T> getList() {
        return list;
    }

    /**
     * 获取导入回执
     *
     * @return 导入回执
     */
    @Override
    public String getAnalysis() {
        int successCount = list.size();
        int errorCount = errorMessages.size();
        if (successCount == 0) {
            return "读取失败，未解析到数据";
        } else {
            if (errorCount == 0) {
                return StrUtil.format("恭喜您，全部读取成功！共{}条", successCount);
            } else {
                return "";
            }
        }
    }
}
