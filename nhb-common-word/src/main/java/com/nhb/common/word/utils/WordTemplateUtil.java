package com.nhb.common.word.utils;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import com.deepoove.poi.config.ConfigureBuilder;
import com.deepoove.poi.data.PictureRenderData;
import com.deepoove.poi.data.PictureType;
import com.deepoove.poi.data.Pictures;
import com.nhb.common.core.enums.FileContentType;
import com.nhb.common.core.utils.FileExportUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/25 11:55
 * @description:
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WordTemplateUtil {
    /**
     * 渲染模板并输出到指定文件
     *
     * @param templatePath 模板路径（支持 classpath: 前缀或绝对路径）
     * @param data         数据模型
     * @param outputFile   输出文件
     */
    public static void exportToFile(String templatePath, Map<String, Object> data, File outputFile) {
        try (OutputStream os = new FileOutputStream(outputFile)) {
            export(templatePath, data, os);
        } catch (IOException e) {
            throw new RuntimeException("导出到文件失败: " + outputFile.getAbsolutePath(), e);
        }
    }

    /**
     * 渲染模板并输出到字节数组
     *
     * @param templatePath 模板路径
     * @param data         数据模型
     * @return 生成的 Word 文档字节数组
     */
    public static byte[] exportToBytes(String templatePath, Map<String, Object> data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            export(templatePath, data, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("导出到字节数组失败", e);
        }
    }

    /**
     * 渲染模板并输出到 HttpServletResponse（用于 Web 下载）
     *
     * @param templatePath 模板路径
     * @param data         数据模型
     * @param response     HttpServletResponse
     * @param fileName     下载的文件名（不含扩展名，会自动添加 .docx）
     */
    public static void exportToResponse(String templatePath, Map<String, Object> data,
                                        HttpServletResponse response, String fileName) {
        try (OutputStream os = response.getOutputStream()) {
            FileExportUtil.setAttachmentResponseHeader(response, fileName, FileContentType.WORD);
            export(templatePath, data, os);
        } catch (IOException e) {
            throw new RuntimeException("导出到 HttpServletResponse 失败", e);
        }
    }

    /**
     * 核心导出方法：渲染模板并将结果写入输出流
     *
     * @param templatePath 模板路径（支持 classpath: 前缀或绝对路径）
     * @param data         数据模型
     * @param os           输出流（调用方负责关闭）
     */
    public static void export(String templatePath, Map<String, Object> data, OutputStream os) {
        XWPFTemplate template = loadTemplate(templatePath, null);
        try {
            template.render(data).write(os);
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException("渲染模板并写入流失败", e);
        } finally {
            closeTemplate(template);
        }
    }

    /**
     * 带配置的导出（如自定义图片渲染器、插件等）
     *
     * @param templatePath 模板路径
     * @param data         数据模型
     * @param os           输出流
     * @param config       poi-tl 配置对象
     */
    public static void exportWithConfig(String templatePath, Map<String, Object> data,
                                        OutputStream os, Configure config) {
        XWPFTemplate template = loadTemplate(templatePath, config);
        try {
            template.render(data).write(os);
            os.flush();
        } catch (IOException e) {
            throw new RuntimeException("带配置的渲染失败", e);
        } finally {
            closeTemplate(template);
        }
    }

    // ==================== 图片辅助方法 ====================

    /**
     * 从文件路径创建图片渲染数据
     *
     * @param imagePath 图片文件路径（支持 classpath: 前缀或绝对路径）
     * @param width     图片宽度（单位：毫米，可设为 null 使用原尺寸）
     * @param height    图片高度（单位：毫米，可设为 null 使用原尺寸）
     * @return PictureRenderData 对象，可直接放入数据模型
     */
    public static PictureRenderData createPictureFromFile(String imagePath, Integer width, Integer height) {
        try (InputStream is = getResourceStream(imagePath)) {
            if (width != null && height != null) {
                return Pictures.ofBytes(is.readAllBytes()).size(width, height).create();
            } else {
                return Pictures.ofBytes(is.readAllBytes()).create();
            }
        } catch (IOException e) {
            throw new RuntimeException("读取图片文件失败: " + imagePath, e);
        }
    }

    /**
     * 从字节数组创建图片渲染数据
     *
     * @param imageBytes 图片字节数组
     * @param width      宽度（毫米）
     * @param height     高度（毫米）
     * @return PictureRenderData
     */
    public static PictureRenderData createPictureFromBytes(byte[] imageBytes, Integer width, Integer height) {
        if (width != null && height != null) {
            return Pictures.ofBytes(imageBytes).size(width, height).create();
        } else {
            return Pictures.ofBytes(imageBytes).create();
        }
    }

    /**
     * 从 BufferedImage 创建图片渲染数据（需引入 java.awt）
     *
     * @param bufferedImage BufferedImage 对象
     * @param width         宽度（毫米）
     * @param height        高度（毫米）
     * @return PictureRenderData
     */
    public static PictureRenderData createPictureFromBufferedImage(BufferedImage bufferedImage,
                                                                   PictureType pictureType,
                                                                   Integer width, Integer height) {
        if (width != null && height != null) {
            return Pictures.ofBufferedImage(bufferedImage, pictureType).size(width, height).create();
        } else {
            return Pictures.ofBufferedImage(bufferedImage, pictureType).create();
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 加载模板
     *
     * @param templatePath 路径，支持 classpath: 前缀或绝对路径
     * @param config       poi-tl 配置，可为 null
     * @return XWPFTemplate 实例
     */
    private static XWPFTemplate loadTemplate(String templatePath, Configure config) {
        try (InputStream is = getResourceStream(templatePath)) {
            return config == null ? XWPFTemplate.compile(is) : XWPFTemplate.compile(is, config);
        } catch (IOException e) {
            throw new RuntimeException("加载模板失败: " + templatePath, e);
        }
    }

    /**
     * 获取资源输入流（支持 classpath 和文件系统）
     *
     * @param path 资源路径
     * @return InputStream
     * @throws FileNotFoundException 如果资源不存在
     */
    private static InputStream getResourceStream(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            String resourcePath = path.substring("classpath:".length());
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                throw new FileNotFoundException("classpath 中未找到资源: " + resourcePath);
            }
            return is;
        } else {
            return new FileInputStream(path);
        }
    }

    /**
     * 安全关闭 XWPFTemplate
     */
    private static void closeTemplate(XWPFTemplate template) {
        if (template != null) {
            try {
                template.close();
            } catch (IOException e) {
                log.warn("关闭 XWPFTemplate 时发生异常", e);
            }
        }
    }

    // ==================== 配置构建 ====================

    /**
     * 获取默认的配置构建器（用于自定义插件等）
     *
     * @return ConfigureBuilder
     */
    public static ConfigureBuilder defaultConfigBuilder() {
        return Configure.builder();
    }
}
