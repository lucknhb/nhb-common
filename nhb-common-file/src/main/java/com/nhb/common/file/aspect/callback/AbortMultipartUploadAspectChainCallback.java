package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.pretreatment.AbortMultipartUploadPretreatment;

/**
 * 手动分片上传-取消切面调用链结束回调
 */
public interface AbortMultipartUploadAspectChainCallback {
    FileInfo run(AbortMultipartUploadPretreatment pre, FileStorage fileStorage, FileRecorder fileRecorder);
}
