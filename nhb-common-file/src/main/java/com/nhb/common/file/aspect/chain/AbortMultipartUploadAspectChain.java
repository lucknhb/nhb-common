package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.AbortMultipartUploadAspectChainCallback;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.pretreatment.AbortMultipartUploadPretreatment;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * 手动分片上传-取消的切面调用链
 */
@Getter
@Setter
public class AbortMultipartUploadAspectChain {

    private AbortMultipartUploadAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public AbortMultipartUploadAspectChain(
            Iterable<FileStorageAspect> aspects, AbortMultipartUploadAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public FileInfo next(AbortMultipartUploadPretreatment pre, FileStorage fileStorage, FileRecorder fileRecorder) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().abortMultipartUploadAround(this, pre, fileStorage, fileRecorder);
        } else {
            return callback.run(pre, fileStorage, fileRecorder);
        }
    }
}
