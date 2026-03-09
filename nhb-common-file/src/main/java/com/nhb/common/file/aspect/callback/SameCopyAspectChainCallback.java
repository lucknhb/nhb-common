package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.pretreatment.CopyPretreatment;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;

/**
 * 同存储平台复制切面调用链结束回调
 */
public interface SameCopyAspectChainCallback {
    FileInfo run(
            FileInfo srcFileInfo,
            FileInfo destFileInfo,
            CopyPretreatment pre,
            FileStorage fileStorage,
            FileRecorder fileRecorder);
}
