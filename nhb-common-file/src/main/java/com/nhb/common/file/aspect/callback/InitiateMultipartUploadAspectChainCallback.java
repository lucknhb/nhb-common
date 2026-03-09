package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.pretreatment.InitiateMultipartUploadPretreatment;

/**
 * 手动分片上传-初始化切面调用链结束回调
 */
public interface InitiateMultipartUploadAspectChainCallback {
    FileInfo run(
            FileInfo fileInfo,
            InitiateMultipartUploadPretreatment pre,
            FileStorage fileStorage,
            FileRecorder fileRecorder);
}
