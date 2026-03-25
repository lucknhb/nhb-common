package com.nhb.common.pdf.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.openpdf.pdf.ITextFontResolver;
import org.openpdf.pdf.ITextRenderer;
import org.openpdf.text.pdf.BaseFont;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/25 10:18
 * @description: 通过FreeMaker模板进行PDF生成
 */
@Slf4j
public class FreeMakerTemplateFiller {

    private static Configuration configuration = null;

    static {
        configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        configuration.setClassForTemplateLoading(FreeMakerTemplateFiller.class, "/templates");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(true);
        configuration.setWrapUncheckedExceptions(true);
        log.info("FreeMarker Configuration Is Initialized");
    }


    /**
     * 根据模板和数据生成PDF
     * @param templateName 模板名称(如 invoice.ftl)
     * @param dataModel 数据模型
     * @return PDF字节数组
     */
    public byte[] renderTemplateToPDF(String templateName, Map<String, Object> dataModel) {
        try {
            // 1. 渲染HTML
            String htmlContent = renderTemplateToString(templateName, dataModel);
            // 2. 使用 openpdf-html 转换PDF
            return convertHtmlToPDF(htmlContent);
        } catch (Exception e) {
            log.error("PDF Build Failed", e);
            throw new RuntimeException("PDF Build Failed", e);
        }
    }

    /**
     * 渲染FreeMarker模板
     * @param templateName  模板名称(如 invoice.ftl)
     * @param dataModel     数据模型
     * @return              渲染后的字符串
     */
    public String renderTemplateToString(String templateName, Map<String, Object> dataModel) {
        try (StringWriter writer = new StringWriter()) {
            Template template = configuration.getTemplate(templateName);
            template.process(dataModel, writer);
            writer.flush();
            return writer.toString();
        } catch (Exception e) {
            log.error("Template Rendering Failed: {}", templateName, e);
            throw new RuntimeException("Template Rendering Failed", e);
        }
    }

    /**
     * HTML转PDF
     * 使用 openpdf-html 模块的 ITextRenderer
     */
    private byte[] convertHtmlToPDF(String htmlContent) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            // 添加自定义字体
            ITextFontResolver fontResolver = renderer.getFontResolver();
            // 从 classpath 加载字体
            ClassPathResource fontResource = new ClassPathResource("fonts/simsun.ttc");
            if (!fontResource.exists()) {
                throw new RuntimeException("Font File Not Exist: fonts/simsun.ttc");
            }
            try (InputStream fontStream = fontResource.getInputStream()) {
                // 创建临时文件
                File tempFontFile = File.createTempFile("SimSun", ".ttf");
                tempFontFile.deleteOnExit();
                Files.copy(fontStream, tempFontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                // 使用临时文件路径加载字体
                renderer.getFontResolver().addFont(tempFontFile.getAbsolutePath(),
                        BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            }
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            log.info("HTML Convert PDF Successful，Size: {} bytes", outputStream.size());
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("HTML Convert PDF Failed", e);
            throw new RuntimeException("HTML Convert PDF Failed", e);
        }
    }
}
