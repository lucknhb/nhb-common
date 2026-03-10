package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.MoveAspectChainCallback;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.pretreatment.MovePretreatment;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 移动的切面调用链
 */
@Getter
@Setter
public class MoveAspectChain {

    private MoveAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public MoveAspectChain(Iterable<FileStorageAspect> aspects, MoveAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public FileInfo next(
            FileInfo srcFileInfo, MovePretreatment pre, FileStorage fileStorage, FileRecorder fileRecorder) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().moveAround(this, srcFileInfo, pre, fileStorage, fileRecorder);
        } else {
            return callback.run(srcFileInfo, pre, fileStorage, fileRecorder);
        }
    }
}
