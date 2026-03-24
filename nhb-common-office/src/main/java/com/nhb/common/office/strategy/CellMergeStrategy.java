package com.nhb.common.office.strategy;

import cn.hutool.core.collection.CollUtil;
import com.nhb.common.office.handler.CellMergeHandler;
import org.apache.fesod.sheet.metadata.Head;
import org.apache.fesod.sheet.write.handler.SheetWriteHandler;
import org.apache.fesod.sheet.write.merge.AbstractMergeStrategy;
import org.apache.fesod.sheet.write.metadata.holder.WriteSheetHolder;
import org.apache.fesod.sheet.write.metadata.holder.WriteWorkbookHolder;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/24 16:13
 * @description: 列值重复合并策略
 */
public class CellMergeStrategy extends AbstractMergeStrategy implements SheetWriteHandler {

    private final List<CellRangeAddress> cellList;

    public CellMergeStrategy(List<CellRangeAddress> cellList) {
        this.cellList = cellList;
    }

    public CellMergeStrategy(List<?> list, boolean hasTitle) {
        this.cellList = CellMergeHandler.of(hasTitle).handle(list);
    }

    public CellMergeStrategy(List<?> list, boolean hasTitle, int rowIndex) {
        this.cellList = CellMergeHandler.of(hasTitle, rowIndex).handle(list);
    }

    @Override
    protected void merge(Sheet sheet, Cell cell, Head head, Integer relativeRowIndex) {
        if (CollUtil.isEmpty(cellList)) {
            return;
        }
        // 单元格写入了,遍历合并区域,如果该Cell在区域内,但非首行,则清空
        final int rowIndex = cell.getRowIndex();
        for (CellRangeAddress cellAddresses : cellList) {
            final int firstRow = cellAddresses.getFirstRow();
            if (cellAddresses.isInRange(cell) && rowIndex != firstRow) {
                cell.setBlank();
            }
        }
    }

    @Override
    public void afterSheetCreate(final WriteWorkbookHolder writeWorkbookHolder, final WriteSheetHolder writeSheetHolder) {
        if (CollUtil.isEmpty(cellList)) {
            return;
        }
        // 在 Sheet 创建时提前写入合并区域；后续写入只会影响首格，不会移除合并
        final Sheet sheet = writeSheetHolder.getSheet();
        for (CellRangeAddress item : cellList) {
            sheet.addMergedRegion(item);
        }
    }
}
