package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.pretreatment.InitiateMultipartUploadPretreatment;


/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 手动分片上传-初始化切面调用链结束回调
 */
public interface InitiateMultipartUploadAspectChainCallback {
    FileInfo run(
            FileInfo fileInfo,
            InitiateMultipartUploadPretreatment pre,
            FileStorage fileStorage,
            FileRecorder fileRecorder);
}
