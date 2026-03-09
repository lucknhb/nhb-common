package com.nhb.common.file.actuator;

import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.chain.GeneratePresignedUrlAspectChain;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.exception.Check;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.pretreatment.GeneratePresignedUrlPretreatment;
import com.nhb.common.file.core.GeneratePresignedUrlResult;

import java.util.HashMap;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 16:56
 * @description: 生成预签名 URL 执行器
 */
public class GeneratePresignedUrlActuator {
    private final FileStorageService fileStorageService;
    private final GeneratePresignedUrlPretreatment pre;

    public GeneratePresignedUrlActuator(GeneratePresignedUrlPretreatment pre) {
        this.pre = pre;
        this.fileStorageService = pre.getFileStorageService();
    }

    /**
     * 执行生成预签名 URL
     */
    public GeneratePresignedUrlResult execute() {
        return execute(fileStorageService.getFileStorageVerify(pre.getPlatform()), fileStorageService.getAspectList());
    }

    /**
     * 执行生成预签名 URL
     */
    public GeneratePresignedUrlResult execute(FileStorage fileStorage, List<FileStorageAspect> aspectList) {
        Check.generatePresignedUrl(pre);
        return new GeneratePresignedUrlAspectChain(aspectList, (_pre, _fileStorage) -> {
            GeneratePresignedUrlResult result = _fileStorage.generatePresignedUrl(_pre);
            if (result.getHeaders() == null) result.setHeaders(new HashMap<>());
            return result;
        })
                .next(pre, fileStorage);
    }
}
