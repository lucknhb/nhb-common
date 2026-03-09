package com.nhb.common.file.adapter;

import com.nhb.common.file.core.MultipartFormDataReader;
import com.nhb.common.file.wrapper.FileWrapper;
import com.nhb.common.file.wrapper.HttpServletRequestFileWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 22:44
 * @description 针对 jakarta 的 HttpServletRequest 文件包装适配器
 */
@Slf4j
@Getter
@Setter
public class HttpServletRequestFileWrapperAdapter implements FileWrapperAdapter {

    @Override
    public boolean isSupport(Object source) {
        if (source instanceof HttpServletRequest request) {
            String contentType = request.getContentType();
            return contentType != null && contentType.toLowerCase().trim().startsWith("multipart/form-data");
        }
        return source instanceof HttpServletRequestFileWrapper;
    }

    @Override
    public FileWrapper getFileWrapper(Object source, String name, String contentType, Long size) throws IOException {
        if (source instanceof HttpServletRequestFileWrapper) {
            return updateFileWrapper((HttpServletRequestFileWrapper) source, name, contentType, size);
        } else {
            HttpServletRequest request = (HttpServletRequest) source;
            Charset charset = Charset.forName(request.getCharacterEncoding());
            MultipartFormDataReader.MultipartFormData data = MultipartFormDataReader.read(
                    request.getContentType(), request.getInputStream(), charset, request.getContentLengthLong());

            if (name == null) name = data.getFileOriginalFilename();
            if (contentType == null) contentType = data.getFileContentType();
            if (size == null) size = data.getFileSize();
            return new HttpServletRequestFileWrapper(data.getInputStream(), name, contentType, size, data);
        }
    }
}
