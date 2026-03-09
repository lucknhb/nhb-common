package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.GetFileAspectChainCallback;
import com.nhb.common.file.pretreatment.GetFilePretreatment;
import com.nhb.common.file.core.RemoteFileInfo;
import com.nhb.common.file.platform.FileStorage;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * 获取文件的切面调用链
 */
@Getter
@Setter
public class GetFileAspectChain {

    private GetFileAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public GetFileAspectChain(Iterable<FileStorageAspect> aspects, GetFileAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public RemoteFileInfo next(GetFilePretreatment pre, FileStorage fileStorage) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().getFile(this, pre, fileStorage);
        } else {
            return callback.run(pre, fileStorage);
        }
    }
}
