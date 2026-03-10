package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.SameCopyAspectChainCallback;
import com.nhb.common.file.pretreatment.CopyPretreatment;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 同存储平台复制的切面调用链
 */
@Getter
@Setter
public class SameCopyAspectChain {

    private SameCopyAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public SameCopyAspectChain(Iterable<FileStorageAspect> aspects, SameCopyAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public FileInfo next(
            FileInfo srcFileInfo,
            FileInfo destFileInfo,
            CopyPretreatment pre,
            FileStorage fileStorage,
            FileRecorder fileRecorder) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator
                    .next()
                    .sameCopyAround(this, srcFileInfo, destFileInfo, pre, fileStorage, fileRecorder);
        } else {
            return callback.run(srcFileInfo, destFileInfo, pre, fileStorage, fileRecorder);
        }
    }
}
