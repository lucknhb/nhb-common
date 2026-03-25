package com.nhb.common.excel.listener;

import cn.hutool.core.util.StrUtil;
import com.nhb.common.core.utils.JacksonUtil;
import com.nhb.common.core.utils.StreamUtil;
import com.nhb.common.core.utils.ValidatorUtil;
import com.nhb.common.excel.core.DefaultExcelResult;
import com.nhb.common.excel.core.ExcelListener;
import com.nhb.common.excel.core.ExcelResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fesod.sheet.context.AnalysisContext;
import org.apache.fesod.sheet.event.AnalysisEventListener;
import org.apache.fesod.sheet.exception.ExcelAnalysisException;
import org.apache.fesod.sheet.exception.ExcelDataConvertException;

import java.util.Map;
import java.util.Set;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 16:13
 * @description: Excel 导入监听
 */
@Slf4j
@NoArgsConstructor
public class DefaultExcelListener<T> extends AnalysisEventListener<T> implements ExcelListener<T> {

    /**
     * 是否Validator检验，默认为是
     */
    private Boolean isValidate = Boolean.TRUE;

    /**
     * excel 表头数据
     */
    private Map<Integer, String> headMap;

    /**
     * 导入回执
     */
    private ExcelResult<T> excelResult;

    public DefaultExcelListener(boolean isValidate) {
        this.excelResult = new DefaultExcelResult<>();
        this.isValidate = isValidate;
    }

    /**
     * 处理异常
     *
     * @param exception ExcelDataConvertException
     * @param context   Excel 上下文
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        String errMsg = null;
        if (exception instanceof ExcelDataConvertException excelDataConvertException) {
            // 如果是某一个单元格的转换异常 能获取到具体行号
            Integer rowIndex = excelDataConvertException.getRowIndex();
            Integer columnIndex = excelDataConvertException.getColumnIndex();
            errMsg = StrUtil.format("第{}行-第{}列-表头{}: 解析异常<br/>",
                    rowIndex + 1, columnIndex + 1, headMap.get(columnIndex));
            if (log.isDebugEnabled()) {
                log.error(errMsg);
            }
        }
        if (exception instanceof ConstraintViolationException constraintViolationException) {
            Set<ConstraintViolation<?>> constraintViolations = constraintViolationException.getConstraintViolations();
            String constraintViolationsMsg = StreamUtil.join(constraintViolations, ConstraintViolation::getMessage, ", ");
            errMsg = StrUtil.format("第{}行数据校验异常: {}", context.readRowHolder().getRowIndex() + 1, constraintViolationsMsg);
            if (log.isDebugEnabled()) {
                log.error(errMsg);
            }
        }
        excelResult.getErrorList().add(errMsg);
        throw new ExcelAnalysisException(errMsg);
    }

    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        this.headMap = headMap;
        log.debug("解析到一条表头数据: {}", JacksonUtil.toJsonString(headMap));
    }

    @Override
    public void invoke(T data, AnalysisContext context) {
        if (isValidate) {
            ValidatorUtil.validate(data);
        }
        excelResult.getList().add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.debug("所有数据解析完成！");
    }

    @Override
    public ExcelResult<T> getExcelResult() {
        return excelResult;
    }

}
