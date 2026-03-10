package com.nhb.common.file.actuator;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.chain.UploadAspectChain;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.exception.FileStorageException;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.pretreatment.UploadPretreatment;
import com.nhb.common.file.wrapper.FileWrapper;

import java.util.Date;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 上传执行器
 */
public class UploadActuator {
    private final FileStorageService fileStorageService;
    private final UploadPretreatment pre;

    /**
     * 通过新的 UploadPretreatment 构造
     */
    public UploadActuator(UploadPretreatment pre) {
        this.pre = pre;
        this.fileStorageService = pre.getFileStorageService();
    }

    /**
     * 执行上传
     */
    public FileInfo execute() {
        return execute(
                fileStorageService.getFileStorage(pre.getPlatform()),
                fileStorageService.getFileRecorder(),
                fileStorageService.getAspectList());
    }

    /**
     * 上传文件，成功返回文件信息，失败返回 null
     */
    public FileInfo execute(FileStorage fileStorage, FileRecorder fileRecorder, List<FileStorageAspect> aspectList) {
        if (fileStorage == null)
            throw new FileStorageException(StrUtil.format("没有找到对应的存储平台！platform:{}", pre.getPlatform()));

        FileWrapper file = pre.getFileWrapper();
        if (file == null) throw new FileStorageException("文件不允许为 null ！");
        if (pre.getPlatform() == null) throw new FileStorageException("platform 不允许为 null ！");

        FileInfo fileInfo = new FileInfo();
        fileInfo.setCreateTime(new Date());
        fileInfo.setSize(file.getSize());
        fileInfo.setOriginalFileName(file.getName());
        fileInfo.setExt(FileNameUtil.getSuffix(file.getName()));
        fileInfo.setObjectId(pre.getObjectId());
        fileInfo.setObjectType(pre.getObjectType());
        fileInfo.setPath(pre.getPath());
        fileInfo.setPlatform(pre.getPlatform());
        fileInfo.setMetadata(pre.getMetadata());
        fileInfo.setUserMetadata(pre.getUserMetadata());
        fileInfo.setThumbnailMetadata(pre.getThumbnailMetadata());
        fileInfo.setThumbnailUserMetadata(pre.getThumbnailUserMetadata());
        fileInfo.setAttr(pre.getAttr());
        fileInfo.setFileAcl(pre.getFileAcl());
        fileInfo.setThumbnailFileAcl(pre.getThumbnailFileAcl());
        if (StrUtil.isNotBlank(pre.getSaveFileName())) {
            fileInfo.setFileName(pre.getSaveFileName());
        } else {
            fileInfo.setFileName(
                    IdUtil.objectId() + (StrUtil.isEmpty(fileInfo.getExt()) ? StrUtil.EMPTY : "." + fileInfo.getExt()));
        }
        fileInfo.setContentType(file.getContentType());

        byte[] thumbnailBytes = pre.getThumbnailBytes();
        if (thumbnailBytes != null) {
            fileInfo.setThumbnailSize((long) thumbnailBytes.length);
            if (StrUtil.isNotBlank(pre.getSaveThumbnailFileName())) {
                fileInfo.setThumbnailFileName(pre.getSaveThumbnailFileName() + pre.getThumbnailSuffix());
            } else {
                fileInfo.setThumbnailFileName(fileInfo.getFileName() + pre.getThumbnailSuffix());
            }
            if (StrUtil.isNotBlank(pre.getThumbnailContentType())) {
                fileInfo.setThumbnailContentType(pre.getThumbnailContentType());
            } else {
                fileInfo.setThumbnailContentType(
                        fileStorageService.getContentTypeDetect().detect(thumbnailBytes, fileInfo.getThumbnailFileName()));
            }
        }

        // 处理切面
        return new UploadAspectChain(aspectList, (_fileInfo, _pre, _fileStorage, _fileRecorder) -> {
                    // 真正开始保存
                    if (_fileStorage.save(_fileInfo, _pre)) {
                        _fileInfo.setHashInfo(_pre.getHashCalculatorManager().getHashInfo());
                        if (_fileRecorder.save(_fileInfo)) {
                            return _fileInfo;
                        }
                    }
                    return null;
                })
                .next(fileInfo, pre, fileStorage, fileRecorder);
    }
}
