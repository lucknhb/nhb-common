package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.UploadPartAspectChainCallback;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.core.FilePartInfo;
import com.nhb.common.file.pretreatment.UploadPartPretreatment;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * 手动分片上传-上传分片的切面调用链
 */
@Getter
@Setter
public class UploadPartAspectChain {

    private UploadPartAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public UploadPartAspectChain(Iterable<FileStorageAspect> aspects, UploadPartAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public FilePartInfo next(UploadPartPretreatment pre, FileStorage fileStorage, FileRecorder fileRecorder) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().uploadPart(this, pre, fileStorage, fileRecorder);
        } else {
            return callback.run(pre, fileStorage, fileRecorder);
        }
    }
}
