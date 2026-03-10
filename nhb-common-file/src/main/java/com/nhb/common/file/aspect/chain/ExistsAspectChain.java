package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.ExistsAspectChainCallback;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 文件是否存在的切面调用链
 */
@Getter
@Setter
public class ExistsAspectChain {

    private ExistsAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public ExistsAspectChain(Iterable<FileStorageAspect> aspects, ExistsAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public boolean next(FileInfo fileInfo, FileStorage fileStorage) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().existsAround(this, fileInfo, fileStorage);
        } else {
            return callback.run(fileInfo, fileStorage);
        }
    }
}
