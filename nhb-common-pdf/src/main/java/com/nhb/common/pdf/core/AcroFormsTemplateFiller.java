package com.nhb.common.pdf.core;

import lombok.extern.slf4j.Slf4j;
import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.AcroFields;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfStamper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/25 9:29
 * @description: OpenPDF AcroForms 填充服务
 * 适用于固定表单类PDF模板
 */
@Slf4j
public class AcroFormsTemplateFiller {
    /**
     * 填充PDF模板
     *
     * @param templateStream 模板输入流
     * @param data           填充数据（字段名 -> 值）
     * @param flatten        是否扁平化（扁平后字段不可编辑）
     * @return 填充后的PDF字节数组
     */
    public byte[] renderTemplateToPDF(InputStream templateStream,
                            Map<String, String> data,
                            boolean flatten) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             PdfReader reader = new PdfReader(templateStream);
             PdfStamper stamper = new PdfStamper(reader, outputStream)) {
            AcroFields form = stamper.getAcroFields();
            // 注册中文字体（解决中文乱码）
            registerChineseFont(form);
            // 填充表单字段
            fillFormFields(form, data);
            // 可选：扁平化处理
            if (flatten) {
                stamper.setFormFlattening(true);
            }
            log.info("PDF Fill Finished，Field Total Size {}", data.size());
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            log.error("Handle PDF Fill Fail", e);
            throw new IOException("Handle PDF Fill Fail", e);
        }
    }

    /**
     * 注册中文字体（解决中文乱码问题）
     */
    private void registerChineseFont(AcroFields form) {
        try {
            // OpenPDF 3.x 中 BaseFont 类路径为 org.openpdf.text.pdf.BaseFont
            // 使用内置中文字体映射
            BaseFont chineseFont = BaseFont.createFont(
                    "STSong-Light",      // 字体名称
                    "UniGB-UCS2-H",      // 编码
                    BaseFont.NOT_EMBEDDED
            );
            form.addSubstitutionFont(chineseFont);
            log.info("Chinese Fonts Are Successfully Registered");
        } catch (Exception e) {
            log.warn("Chinese Font Registration Fails, Which May Affect The Display Of Chinese Fonts", e);
        }
    }

    /**
     * 填充表单字段
     */
    private void fillFormFields(AcroFields form, Map<String, String> data) {
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            try {
                if (form.getField(fieldName) != null) {
                    form.setField(fieldName, fieldValue);
                    log.debug("填充字段: {} = {}", fieldName, fieldValue);
                } else {
                    log.warn("模板中不存在字段: {}", fieldName);
                }
            } catch (Exception e) {
                log.error("填充字段失败: {}", fieldName, e);
            }
        }
    }
}
