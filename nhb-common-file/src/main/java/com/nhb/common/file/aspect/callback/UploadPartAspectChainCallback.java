package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.core.FilePartInfo;
import com.nhb.common.file.pretreatment.UploadPartPretreatment;

/**
 * 手动分片上传-上传分片切面调用链结束回调
 */
public interface UploadPartAspectChainCallback {
    FilePartInfo run(UploadPartPretreatment pre, FileStorage fileStorage, FileRecorder fileRecorder);
}
