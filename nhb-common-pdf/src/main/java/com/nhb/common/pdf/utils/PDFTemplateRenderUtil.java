package com.nhb.common.pdf.utils;

import com.nhb.common.core.enums.FileContentType;
import com.nhb.common.core.utils.FileExportUtil;
import com.nhb.common.pdf.core.AcroFormsTemplateFiller;
import com.nhb.common.pdf.core.FreeMakerTemplateFiller;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/25 9:44
 * @description: PDF 模板渲染工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PDFTemplateRenderUtil {
    private static final AcroFormsTemplateFiller acroFormsTemplateFiller = new AcroFormsTemplateFiller();
    @Getter
    private static final FreeMakerTemplateFiller freeMakerTemplateFiller = new FreeMakerTemplateFiller();

    /**
     * 该方法对于固定表单模板比较友好 例如合同
     *
     * @param inputStream 模板输入流
     * @param modelData   填充数据（字段名 -> 值）
     * @param flatten     是否扁平化（扁平后字段不可编辑）
     * @return 生成的PDF字节数组
     * @throws IOException 异常信息
     */
    public static byte[] renderFormTemplate(InputStream inputStream, Map<String, String> modelData, boolean flatten) throws IOException {
        return acroFormsTemplateFiller.renderTemplateToPDF(inputStream, modelData, flatten);
    }

    /**
     * 根据模板和数据生成PDF
     *
     * @param templateName 模板名称(如 invoice.ftl)
     * @param modelData    数据模型
     * @return PDF字节数组
     */
    public static byte[] renderFreeMarkerTemplate(String templateName, Map<String, Object> modelData) {
        return freeMakerTemplateFiller.renderTemplateToPDF(templateName, modelData);
    }

    /**
     * 将字节数组通过HTTP导出
     * @param response  返回对象
     * @param fileName  文件名
     * @param data      字节数组
     */
    public static void exportPDFForHttp(HttpServletResponse response, String fileName, byte[] data) {
        FileExportUtil.setAttachmentResponseHeader(response, fileName, FileContentType.PDF);
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(data);
            outputStream.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
