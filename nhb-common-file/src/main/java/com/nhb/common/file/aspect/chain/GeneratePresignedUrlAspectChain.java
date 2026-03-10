package com.nhb.common.file.aspect.chain;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.callback.GeneratePresignedUrlAspectChainCallback;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.pretreatment.GeneratePresignedUrlPretreatment;
import com.nhb.common.file.core.GeneratePresignedUrlResult;
import lombok.Getter;
import lombok.Setter;

import java.util.Iterator;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 对文件生成可以签名访问的 URL 的切面调用链
 */
@Getter
@Setter
public class GeneratePresignedUrlAspectChain {

    private GeneratePresignedUrlAspectChainCallback callback;
    private Iterator<FileStorageAspect> aspectIterator;

    public GeneratePresignedUrlAspectChain(
            Iterable<FileStorageAspect> aspects, GeneratePresignedUrlAspectChainCallback callback) {
        this.aspectIterator = aspects.iterator();
        this.callback = callback;
    }

    /**
     * 调用下一个切面
     */
    public GeneratePresignedUrlResult next(GeneratePresignedUrlPretreatment pre, FileStorage fileStorage) {
        if (aspectIterator.hasNext()) { // 还有下一个
            return aspectIterator.next().generatePresignedUrlAround(this, pre, fileStorage);
        } else {
            return callback.run(pre, fileStorage);
        }
    }
}
