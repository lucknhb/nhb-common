package com.nhb.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/14 11:02
 * @description:  文件导出返回格式类型
 */
@Getter
@AllArgsConstructor
public enum FileContentType {
    /**
     * 高版本Excel
     */
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    /**
     * 低版本Excel
     */
    EXCEL_OLD("application/vnd.ms-excel", ".xls"),
    /**
     * PDF
     */
    PDF("application/pdf", ".pdf"),
    /**
     * CSV
     */
    CSV("text/csv", ".csv"),
    /**
     * 高版本WORD
     */
    WORD("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"),
    /**
     * 低版本WORD
     */
    WORD_OLD("application/msword", ".doc"),
    /**
     * TXT
     */
    TXT("text/plain", ".txt"),
    /**
     * JSON
     */
    JSON("application/json", ".json"),
    /**
     * XML
     */
    XML("application/xml", ".xml"),
    /**
     * ZIP
     */
    ZIP("application/zip", ".zip");

    /**
     * MIME类型
     */
    private final String mimeType;
    /**
     * 扩展名
     */
    private final String extension;

    /**
     * 根据文件扩展名获取对应的枚举
     */
    public static FileContentType fromExtension(String extension) {
        for (FileContentType type : values()) {
            if (type.getExtension().equalsIgnoreCase(extension)) {
                return type;
            }
        }
        return null;
    }
}
