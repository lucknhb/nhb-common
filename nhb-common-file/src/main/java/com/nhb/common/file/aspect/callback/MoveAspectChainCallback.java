package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.pretreatment.MovePretreatment;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;

/**
 * 移动切面调用链结束回调
 */
public interface MoveAspectChainCallback {
    FileInfo run(FileInfo srcFileInfo, MovePretreatment pre, FileStorage fileStorage, FileRecorder fileRecorder);
}
