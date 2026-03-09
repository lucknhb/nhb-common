package com.nhb.common.file.tika;

import com.nhb.common.file.core.ContentTypeDetect;
import lombok.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 14:56
 * @description: 基于 Tika 识别文件的 MIME 类型
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TikaContentTypeDetect implements ContentTypeDetect {
    private TikaFactory tikaFactory;

    @Override
    public String detect(File file) throws IOException {
        return tikaFactory.getTika().detect(file);
    }

    @Override
    public String detect(byte[] bytes) {
        return tikaFactory.getTika().detect(bytes);
    }

    @Override
    public String detect(byte[] bytes, String filename) {
        return tikaFactory.getTika().detect(bytes, filename);
    }

    @Override
    public String detect(InputStream in, String filename) throws IOException {
        return tikaFactory.getTika().detect(in, filename);
    }
}
