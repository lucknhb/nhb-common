package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.CopyAspectChainCallback;
import com.nhb.common.file.pretreatment.CopyPretreatment;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * 复制的切面调用链
 */
@Getter
@Setter
public class CopyAspectChain {

    private CopyAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public CopyAspectChain(Iterable<FileStorageAspect> aspects, CopyAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public FileInfo next(
            FileInfo srcFileInfo, CopyPretreatment pre, FileStorage fileStorage, FileRecorder fileRecorder) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().copyAround(this, srcFileInfo, pre, fileStorage, fileRecorder);
        } else {
            return callback.run(srcFileInfo, pre, fileStorage, fileRecorder);
        }
    }
}
