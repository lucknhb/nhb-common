package com.nhb.common.file.wrapper;

import cn.hutool.core.io.IoUtil;
import com.nhb.common.file.core.MultipartFormDataReader;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.InputStream;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 22:27
 * @description HttpServletRequest 文件包装类
 */
@Getter
@Setter
@NoArgsConstructor
public class HttpServletRequestFileWrapper implements FileWrapper {
    private String name;
    private String contentType;
    private InputStream inputStream;
    private Long size;
    private MultipartFormDataReader.MultipartFormData multipartFormData;

    public HttpServletRequestFileWrapper(InputStream inputStream, String name, String contentType, Long size, MultipartFormDataReader.MultipartFormData multipartFormData) {
        this.name = name;
        this.contentType = contentType;
        this.inputStream = IoUtil.toMarkSupportStream(inputStream);
        this.size = size;
        this.multipartFormData = multipartFormData;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * 获取参数值
     */
    public String getParameter(String name) {
        return multipartFormData.getParameter(name);
    }

    /**
     * 获取多个参数值
     */
    public String[] getParameterValues(String name) {
        return multipartFormData.getParameterValues(name);
    }
}
