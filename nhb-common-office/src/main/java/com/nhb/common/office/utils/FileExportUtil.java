package com.nhb.common.office.utils;

import com.nhb.common.core.enums.FileContentType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/14 11:21
 * @description: 文件导出工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileExportUtil {
    private static final String DOWNLOAD_FILENAME = "Download-FileName";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final String ACCESS_CONTROL_EXPOSE_HEADERS = "Access-Control-Expose-Headers";
    /**
     * 下载文件名重新编码
     *
     * @param response     响应对象
     * @param realFileName 真实文件名
     */
    public static void setAttachmentResponseHeader(HttpServletResponse response, String realFileName, FileContentType fileContentType) {
        response.setContentType(fileContentType.getMimeType());
        String encodedFileName = percentEncode(realFileName);
        String contentDisposition = "attachment; filename=%s;filename*=utf-8''%s".formatted(encodedFileName, encodedFileName);
        response.addHeader(ACCESS_CONTROL_EXPOSE_HEADERS, String.join(",",CONTENT_DISPOSITION,DOWNLOAD_FILENAME));
        response.setHeader(CONTENT_DISPOSITION, contentDisposition);
        response.setHeader(DOWNLOAD_FILENAME, encodedFileName);
    }

    /**
     * 百分号编码工具方法
     *
     * @param s 需要百分号编码的字符串
     * @return 百分号编码后的字符串
     */
    public static String percentEncode(String s) {
        String encode = URLEncoder.encode(s, StandardCharsets.UTF_8);
        return encode.replaceAll("\\+", "%20");
    }
}
