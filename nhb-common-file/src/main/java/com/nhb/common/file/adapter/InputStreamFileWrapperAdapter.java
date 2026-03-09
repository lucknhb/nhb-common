package com.nhb.common.file.adapter;

import com.nhb.common.file.core.ContentTypeDetect;
import com.nhb.common.file.wrapper.FileWrapper;
import com.nhb.common.file.wrapper.InputStreamFileWrapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 22:43
 * @description InputStream 文件包装适配器
 */
@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InputStreamFileWrapperAdapter implements FileWrapperAdapter {
    private ContentTypeDetect contentTypeDetect;

    @Override
    public boolean isSupport(Object source) {
        return source instanceof InputStream || source instanceof InputStreamFileWrapper;
    }

    @Override
    public FileWrapper getFileWrapper(Object source, String name, String contentType, Long size) throws IOException {
        if (source instanceof InputStreamFileWrapper) {
            return updateFileWrapper((InputStreamFileWrapper) source, name, contentType, size);
        } else {
            InputStream inputStream = (InputStream) source;
            if (name == null) name = "";
            InputStreamFileWrapper wrapper = new InputStreamFileWrapper(inputStream, name, contentType, size);
            if (contentType == null) {
                wrapper.getInputStreamMaskReset(
                        in -> wrapper.setContentType(contentTypeDetect.detect(in, wrapper.getName())));
            }
            return wrapper;
        }
    }
}
