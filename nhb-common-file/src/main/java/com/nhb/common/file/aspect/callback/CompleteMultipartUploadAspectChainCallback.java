package com.nhb.common.file.aspect.callback;


import com.nhb.common.file.core.ContentTypeDetect;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.pretreatment.CompleteMultipartUploadPretreatment;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 手动分片上传-完成切面调用链结束回调
 */
public interface CompleteMultipartUploadAspectChainCallback {
    FileInfo run(
            CompleteMultipartUploadPretreatment pre,
            FileStorage fileStorage,
            FileRecorder fileRecorder,
            ContentTypeDetect contentTypeDetect);
}
