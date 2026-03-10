package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.DownloadAspectChainCallback;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.platform.FileStorage;
import lombok.Getter;
import lombok.Setter;

import java.io.InputStream;
import java.util.Iterator;
import java.util.function.Consumer;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 下载的切面调用链
 */
@Getter
@Setter
public class DownloadAspectChain {

    private DownloadAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public DownloadAspectChain(Iterable<FileStorageAspect> aspects, DownloadAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public void next(FileInfo fileInfo, FileStorage fileStorage, Consumer<InputStream> consumer) {
        if (aspectIterator.hasNext()) { // 还有下一个
            aspectIterator.next().downloadAround(this, fileInfo, fileStorage, consumer);
        } else {
            callback.run(fileInfo, fileStorage, consumer);
        }
    }
}
