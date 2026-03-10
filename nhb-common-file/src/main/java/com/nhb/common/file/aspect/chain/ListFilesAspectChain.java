package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.ListFilesAspectChainCallback;
import com.nhb.common.file.pretreatment.ListFilesPretreatment;
import com.nhb.common.file.core.ListFilesResult;
import com.nhb.common.file.platform.FileStorage;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 列举文件的切面调用链
 */
@Getter
@Setter
public class ListFilesAspectChain {

    private ListFilesAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public ListFilesAspectChain(Iterable<FileStorageAspect> aspects, ListFilesAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public ListFilesResult next(ListFilesPretreatment pre, FileStorage fileStorage) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().listFiles(this, pre, fileStorage);
        } else {
            return callback.run(pre, fileStorage);
        }
    }
}
