package com.nhb.common.file.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 13:07
 * @description: 识别文件的 MIME 类型
 */
public interface ContentTypeDetect {

    String detect(File file) throws IOException;

    String detect(byte[] bytes);

    String detect(byte[] bytes, String filename);

    String detect(InputStream in, String filename) throws IOException;
}
