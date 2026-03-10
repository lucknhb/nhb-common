package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;

import java.io.InputStream;
import java.util.function.Consumer;


/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 下载缩略图切面调用链结束回调
 */
public interface DownloadThAspectChainCallback {
    void run(FileInfo fileInfo, FileStorage fileStorage, Consumer<InputStream> consumer);
}
