package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.InitiateMultipartUploadAspectChainCallback;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.pretreatment.InitiateMultipartUploadPretreatment;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 手动分片上传-初始化的切面调用链
 */
@Getter
@Setter
public class InitiateMultipartUploadAspectChain {

    private InitiateMultipartUploadAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public InitiateMultipartUploadAspectChain(
            Iterable<FileStorageAspect> aspects, InitiateMultipartUploadAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public FileInfo next(
            FileInfo fileInfo,
            InitiateMultipartUploadPretreatment pre,
            FileStorage fileStorage,
            FileRecorder fileRecorder) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().initiateMultipartUploadAround(this, fileInfo, pre, fileStorage, fileRecorder);
        } else {
            return callback.run(fileInfo, pre, fileStorage, fileRecorder);
        }
    }
}
