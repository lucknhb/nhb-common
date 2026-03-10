package com.nhb.common.file.adapter;

import com.nhb.common.file.wrapper.FileWrapper;
import com.nhb.common.file.wrapper.MultipartFileWrapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 22:44
 * @description MultipartFile 文件包装适配器
 */
@Getter
@Setter
public class MultipartFileWrapperAdapter implements FileWrapperAdapter {

    @Override
    public boolean isSupport(Object source) {
        return source instanceof MultipartFile || source instanceof MultipartFileWrapper;
    }

    @Override
    public FileWrapper getFileWrapper(Object source, String name, String contentType, Long size) {
        if (source instanceof MultipartFileWrapper) {
            return updateFileWrapper((MultipartFileWrapper) source, name, contentType, size);
        } else {
            MultipartFile file = (MultipartFile) source;
            if (name == null) name = file.getOriginalFilename();
            if (contentType == null) contentType = file.getContentType();
            if (size == null) size = file.getSize();
            return new MultipartFileWrapper(file, name, contentType, size);
        }
    }
}
