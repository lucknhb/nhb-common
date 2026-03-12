package com.nhb.common.file.actuator;


import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.chain.UploadPartAspectChain;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.core.FilePartInfo;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.exception.ExceptionCheck;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.pretreatment.UploadPartPretreatment;
import com.nhb.common.file.recorder.FileRecorder;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 手动分片上传-上传分片执行器
 */
public class UploadPartActuator {
    private final FileStorageService fileStorageService;
    private final UploadPartPretreatment pre;

    public UploadPartActuator(UploadPartPretreatment pre) {
        this.pre = pre;
        this.fileStorageService = pre.getFileStorageService();
    }

    /**
     * 执行上传
     */
    public FilePartInfo execute() {
        FileInfo fileInfo = pre.getFileInfo();
        ExceptionCheck.uploadPart(fileInfo);

        FileStorage fileStorage = fileStorageService.getFileStorageVerify(fileInfo.getPlatform());
        CopyOnWriteArrayList<FileStorageAspect> aspectList = fileStorageService.getAspectList();
        FileRecorder fileRecorder = fileStorageService.getFileRecorder();

        return new UploadPartAspectChain(aspectList, (_pre, _fileStorage, _fileRecorder) -> {
                    FilePartInfo filePartInfo = _fileStorage.uploadPart(_pre);
                    filePartInfo.setHashInfo(_pre.getHashCalculatorManager().getHashInfo());
                    _fileRecorder.saveFilePart(filePartInfo);
                    return filePartInfo;
                })
                .next(pre, fileStorage, fileRecorder);
    }
}
