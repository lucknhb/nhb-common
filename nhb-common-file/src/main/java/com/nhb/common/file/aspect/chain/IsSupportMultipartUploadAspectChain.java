package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.IsSupportMultipartUploadChainCallback;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.core.MultipartUploadSupportInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 是否支持手动分片上传的切面调用链
 */
@Getter
@Setter
public class IsSupportMultipartUploadAspectChain {

    private IsSupportMultipartUploadChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public IsSupportMultipartUploadAspectChain(
            Iterable<FileStorageAspect> aspects, IsSupportMultipartUploadChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public MultipartUploadSupportInfo next(FileStorage fileStorage) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().isSupportMultipartUpload(this, fileStorage);
        } else {
            return callback.run(fileStorage);
        }
    }
}
