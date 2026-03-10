package com.nhb.common.file.actuator;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.chain.InitiateMultipartUploadAspectChain;
import com.nhb.common.file.constant.FileStorageConstants;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.exception.ExceptionFactory;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.pretreatment.InitiateMultipartUploadPretreatment;

import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 手动分片上传-初始化执行器
 */
public class InitiateMultipartUploadActuator {
    private final FileStorageService fileStorageService;
    private final InitiateMultipartUploadPretreatment pre;

    public InitiateMultipartUploadActuator(InitiateMultipartUploadPretreatment pre) {
        this.pre = pre;
        this.fileStorageService = pre.getFileStorageService();
    }

    /**
     * 执行初始化
     */
    public FileInfo execute() {
        FileStorage fileStorage = fileStorageService.getFileStorageVerify(pre.getPlatform());
        FileInfo fileInfo = new FileInfo();
        fileInfo.setCreateTime(new Date());
        fileInfo.setSize(pre.getSize());
        fileInfo.setOriginalFileName(pre.getOriginalFilename());
        fileInfo.setExt(FileNameUtil.getSuffix(pre.getOriginalFilename()));
        fileInfo.setObjectId(pre.getObjectId());
        fileInfo.setObjectType(pre.getObjectType());
        fileInfo.setPath(pre.getPath());
        fileInfo.setPlatform(pre.getPlatform());
        fileInfo.setMetadata(pre.getMetadata());
        fileInfo.setUserMetadata(pre.getUserMetadata());
        fileInfo.setAttr(pre.getAttr());
        fileInfo.setFileAcl(pre.getFileAcl());
        fileInfo.setUploadStatus(FileStorageConstants.FileInfoUploadStatus.INITIATE);

        if (StrUtil.isNotBlank(pre.getSaveFilename())) {
            fileInfo.setFileName(pre.getSaveFilename());
        } else {
            fileInfo.setFileName(
                    IdUtil.objectId() + (StrUtil.isEmpty(fileInfo.getExt()) ? StrUtil.EMPTY : "." + fileInfo.getExt()));
        }
        fileInfo.setContentType(pre.getContentType());

        CopyOnWriteArrayList<FileStorageAspect> aspectList = fileStorageService.getAspectList();
        FileRecorder fileRecorder = fileStorageService.getFileRecorder();

        // 处理切面
        return new InitiateMultipartUploadAspectChain(aspectList, (_fileInfo, _pre, _fileStorage, _fileRecorder) -> {
                    // 真正开始保存
                    _fileStorage.initiateMultipartUpload(_fileInfo, _pre);
                    try {
                        if (!_fileRecorder.save(_fileInfo)) {
                            throw new RuntimeException("文件记录保存失败");
                        }
                    } catch (Exception e) {
                        throw ExceptionFactory.initiateMultipartUploadRecorderSave(
                                _fileInfo, _fileStorage.getPlatform(), e);
                    }
                    return _fileInfo;
                })
                .next(fileInfo, pre, fileStorage, fileRecorder);
    }
}
