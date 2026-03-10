package com.nhb.common.file.actuator;


import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.chain.AbortMultipartUploadAspectChain;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.exception.Check;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.pretreatment.AbortMultipartUploadPretreatment;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 手动分片上传-取消执行器
 */
public class AbortMultipartUploadActuator {
    private final FileStorageService fileStorageService;
    private final AbortMultipartUploadPretreatment pre;

    public AbortMultipartUploadActuator(AbortMultipartUploadPretreatment pre) {
        this.pre = pre;
        this.fileStorageService = pre.getFileStorageService();
    }

    /**
     * 执行取消
     */
    public FileInfo execute() {
        FileInfo fileInfo = pre.getFileInfo();
        Check.abortMultipartUpload(fileInfo);

        FileStorage fileStorage = fileStorageService.getFileStorageVerify(fileInfo.getPlatform());
        CopyOnWriteArrayList<FileStorageAspect> aspectList = fileStorageService.getAspectList();
        FileRecorder fileRecorder = fileStorageService.getFileRecorder();

        return new AbortMultipartUploadAspectChain(aspectList, (_pre, _fileStorage, _fileRecorder) -> {
                    FileInfo _fileInfo = _pre.getFileInfo();
                    _fileStorage.abortMultipartUpload(_pre);
                    _fileRecorder.deleteFilePartByUploadId(_fileInfo.getUploadId());
                    _fileRecorder.delete(_fileInfo.getUrl());
                    return _fileInfo;
                })
                .next(pre, fileStorage, fileRecorder);
    }
}
