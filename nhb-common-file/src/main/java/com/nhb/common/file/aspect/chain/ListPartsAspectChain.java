package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.ListPartsAspectChainCallback;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.core.FilePartInfoList;
import com.nhb.common.file.pretreatment.ListPartsPretreatment;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * 手动分片上传-列举已上传的分片的切面调用链
 */
@Getter
@Setter
public class ListPartsAspectChain {

    private ListPartsAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public ListPartsAspectChain(Iterable<FileStorageAspect> aspects, ListPartsAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public FilePartInfoList next(ListPartsPretreatment pre, FileStorage fileStorage) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().listParts(this, pre, fileStorage);
        } else {
            return callback.run(pre, fileStorage);
        }
    }
}
