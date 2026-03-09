package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.pretreatment.CopyPretreatment;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;

/**
 * 复制切面调用链结束回调
 */
public interface CopyAspectChainCallback {
    FileInfo run(FileInfo srcFileInfo, CopyPretreatment pre, FileStorage fileStorage, FileRecorder fileRecorder);
}
