package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 文件是否存在切面调用链结束回调
 */
public interface ExistsAspectChainCallback {
    boolean run(FileInfo fileInfo, FileStorage fileStorage);
}
