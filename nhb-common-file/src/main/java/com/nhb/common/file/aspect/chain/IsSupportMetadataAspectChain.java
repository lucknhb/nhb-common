package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.IsSupportMetadataAspectChainCallback;
import com.nhb.common.file.platform.FileStorage;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * 是否支持 Metadata 的切面调用链
 */
@Getter
@Setter
public class IsSupportMetadataAspectChain {

    private IsSupportMetadataAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public IsSupportMetadataAspectChain(
            Iterable<FileStorageAspect> aspects, IsSupportMetadataAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public boolean next(FileStorage fileStorage) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().isSupportMetadataAround(this, fileStorage);
        } else {
            return callback.run(fileStorage);
        }
    }
}
