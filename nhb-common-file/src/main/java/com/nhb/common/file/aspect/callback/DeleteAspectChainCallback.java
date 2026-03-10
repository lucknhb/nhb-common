package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 删除切面调用链结束回调
 */
public interface DeleteAspectChainCallback {
    boolean run(FileInfo fileInfo, FileStorage fileStorage, FileRecorder fileRecorder);
}
