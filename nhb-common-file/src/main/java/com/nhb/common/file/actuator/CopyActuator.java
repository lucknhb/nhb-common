package com.nhb.common.file.actuator;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.chain.CopyAspectChain;
import com.nhb.common.file.aspect.chain.SameCopyAspectChain;
import com.nhb.common.file.constant.FileStorageConstants;
import com.nhb.common.file.pretreatment.CopyPretreatment;
import com.nhb.common.file.core.Downloader;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.exception.FileStorageException;
import com.nhb.common.file.hash.HashInfo;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 复制执行器
 */
public class CopyActuator {
    private final FileStorageService fileStorageService;
    private final FileInfo fileInfo;
    private final CopyPretreatment pre;

    public CopyActuator(CopyPretreatment pre) {
        this.pre = pre;
        this.fileStorageService = pre.getFileStorageService();
        this.fileInfo = pre.getFileInfo();
    }

    /**
     * 复制文件，成功后返回新的 FileInfo
     */
    public FileInfo execute() {
        return execute(
                fileStorageService.getFileStorageVerify(fileInfo.getPlatform()),
                fileStorageService.getFileRecorder(),
                fileStorageService.getAspectList());
    }

    /**
     * 复制文件，成功后返回新的 FileInfo
     */
    public FileInfo execute(FileStorage fileStorage, FileRecorder fileRecorder, List<FileStorageAspect> aspectList) {
        if (fileInfo == null) throw new FileStorageException("fileInfo 不能为 null");
        if (fileInfo.getPlatform() == null) throw new FileStorageException("fileInfo 的 platform 不能为 null");
        if (fileInfo.getPath() == null) throw new FileStorageException("fileInfo 的 path 不能为 null");
        if (StrUtil.isBlank(fileInfo.getFileName())) {
            throw new FileStorageException("fileInfo 的 filename 不能为空");
        }
        if (StrUtil.isNotBlank(fileInfo.getThumbnailFileName()) && StrUtil.isBlank(pre.getThumbnailFileName())) {
            throw new FileStorageException("目标缩略图文件名不能为空");
        }

        // 处理切面
        return new CopyAspectChain(aspectList, (_srcFileInfo, _pre, _fileStorage, _fileRecorder) -> {
                    // 真正开始复制
                    FileInfo destFileInfo;
                    if (isSameCopy(_srcFileInfo, _pre, _fileStorage)) {
                        destFileInfo = sameCopy(_srcFileInfo, _pre, _fileStorage, _fileRecorder, aspectList);
                    } else {
                        destFileInfo = crossCopy(_srcFileInfo, _pre, _fileStorage, _fileRecorder, aspectList);
                    }
                    return destFileInfo;
                })
                .next(fileInfo, pre, fileStorage, fileRecorder);
    }

    /**
     * 判断是否使用同存储平台复制
     */
    protected boolean isSameCopy(FileInfo srcFileInfo, CopyPretreatment pre, FileStorage fileStorage) {
        FileStorageConstants.CopyMode copyMode = pre.getCopyMode();
        if (copyMode == FileStorageConstants.CopyMode.SAME) {
            if (!fileStorageService.isSupportSameCopy(fileStorage)) {
                throw new FileStorageException("存储平台【" + fileStorage.getPlatform() + "】不支持同存储平台复制");
            }
            return true;
        } else if (copyMode == FileStorageConstants.CopyMode.CROSS) {
            return false;
        } else {
            return srcFileInfo.getPlatform().equals(pre.getPlatform())
                    && fileStorageService.isSupportSameCopy(fileStorage);
        }
    }

    /**
     * 同存储平台复制
     */
    protected FileInfo sameCopy(
            FileInfo srcFileInfo,
            CopyPretreatment pre,
            FileStorage fileStorage,
            FileRecorder fileRecorder,
            List<FileStorageAspect> aspectList) {
        // 检查文件名是否与原始的相同
        if ((srcFileInfo.getPath() + srcFileInfo.getFileName()).equals(pre.getPath() + pre.getFileName())) {
            throw new FileStorageException("源文件与目标文件路径相同");
        }
        // 检查缩略图文件名是否与原始的相同
        if (StrUtil.isNotBlank(srcFileInfo.getThumbnailFileName())
                && (srcFileInfo.getPath() + srcFileInfo.getThumbnailFileName()).equals(pre.getPath() + pre.getThumbnailFileName())) {
            throw new FileStorageException("源缩略图文件与目标缩略图文件路径相同");
        }

        FileInfo destFileInfo = new FileInfo();
        destFileInfo.setSize(srcFileInfo.getSize());
        destFileInfo.setFileName(pre.getFileName());
        destFileInfo.setOriginalFileName(srcFileInfo.getOriginalFileName());
        destFileInfo.setBasePath(srcFileInfo.getBasePath());
        destFileInfo.setPath(pre.getPath());
        destFileInfo.setExt(FileNameUtil.extName(pre.getFileName()));
        destFileInfo.setContentType(srcFileInfo.getContentType());
        destFileInfo.setPlatform(pre.getPlatform());
        destFileInfo.setThumbnailFileName(pre.getThumbnailFileName());
        destFileInfo.setThumbnailSize(srcFileInfo.getThumbnailSize());
        destFileInfo.setThumbnailContentType(srcFileInfo.getThumbnailContentType());
        destFileInfo.setObjectId(srcFileInfo.getObjectId());
        destFileInfo.setObjectType(srcFileInfo.getObjectType());
        if (srcFileInfo.getMetadata() != null) {
            destFileInfo.setMetadata(new LinkedHashMap<>(srcFileInfo.getMetadata()));
        }
        if (srcFileInfo.getUserMetadata() != null) {
            destFileInfo.setUserMetadata(new LinkedHashMap<>(srcFileInfo.getUserMetadata()));
        }
        if (srcFileInfo.getThumbnailMetadata() != null) {
            destFileInfo.setThumbnailMetadata(new LinkedHashMap<>(srcFileInfo.getThumbnailMetadata()));
        }
        if (srcFileInfo.getThumbnailUserMetadata() != null) {
            destFileInfo.setThumbnailUserMetadata(new LinkedHashMap<>(srcFileInfo.getThumbnailUserMetadata()));
        }
        if (srcFileInfo.getAttr() != null) {
            destFileInfo.setAttr(new Dict(srcFileInfo.getAttr()));
        }
        if (srcFileInfo.getHashInfo() != null) {
            destFileInfo.setHashInfo(new HashInfo(srcFileInfo.getHashInfo()));
        }
        destFileInfo.setFileAcl(srcFileInfo.getFileAcl());
        destFileInfo.setThumbnailFileAcl(srcFileInfo.getThumbnailFileAcl());
        destFileInfo.setCreateTime(new Date());

        return new SameCopyAspectChain(aspectList, (_srcfileInfo, _destFileInfo, _pre, _fileStorage, _fileRecorder) -> {
                    _fileStorage.sameCopy(_srcfileInfo, _destFileInfo, _pre);
                    _fileRecorder.save(_destFileInfo);
                    return _destFileInfo;
                })
                .next(srcFileInfo, destFileInfo, pre, fileStorage, fileRecorder);
    }

    /**
     * 跨存储平台复制，通过从下载并重新上传来实现
     */
    protected FileInfo crossCopy(
            FileInfo srcFileInfo,
            CopyPretreatment pre,
            FileStorage fileStorage,
            FileRecorder fileRecorder,
            List<FileStorageAspect> aspectList) {
        // 下载缩略图
        byte[] thBytes = StrUtil.isNotBlank(srcFileInfo.getThumbnailFileName())
                ? new Downloader(srcFileInfo, aspectList, fileStorage, Downloader.TARGET_TH_FILE).bytes()
                : null;

        final FileInfo[] destFileInfoArr = new FileInfo[1];
        new Downloader(srcFileInfo, aspectList, fileStorage, Downloader.TARGET_FILE).inputStream(in -> {
            String thumbnailSuffix = FileNameUtil.extName(pre.getThumbnailFileName());
            if (StrUtil.isNotBlank(thumbnailSuffix)) thumbnailSuffix = "." + thumbnailSuffix;

            destFileInfoArr[0] = fileStorageService
                    .of(in, srcFileInfo.getOriginalFileName(), srcFileInfo.getContentType(), srcFileInfo.getSize())
                    .setPlatform(pre.getPlatform())
                    .setPath(pre.getPath())
                    .setSaveFileName(pre.getFileName())
                    .setContentType(srcFileInfo.getContentType())
                    .setSaveThumbnailFileName(thBytes != null, FileNameUtil.mainName(pre.getThumbnailFileName()))
                    .setThumbnailSuffix(thBytes != null, thumbnailSuffix)
                    .thumbnailOf(thBytes != null, thBytes)
                    .setThumbnailContentType(srcFileInfo.getThumbnailContentType())
                    .setObjectType(srcFileInfo.getObjectType())
                    .setObjectId(srcFileInfo.getObjectId())
                    .setNotSupportAclThrowException(
                            pre.getNotSupportAclThrowException() != null, pre.getNotSupportAclThrowException())
                    .setFileAcl(srcFileInfo.getFileAcl() != null, srcFileInfo.getFileAcl())
                    .setThumbnailFileAcl(srcFileInfo.getThumbnailFileAcl() != null, srcFileInfo.getThumbnailFileAcl())
                    .setNotSupportMetadataThrowException(
                            pre.getNotSupportMetadataThrowException() != null,
                            pre.getNotSupportMetadataThrowException())
                    .putMetadataAll(srcFileInfo.getMetadata() != null, srcFileInfo.getMetadata())
                    .putThumbnailMetadataAll(srcFileInfo.getThumbnailMetadata() != null, srcFileInfo.getThumbnailMetadata())
                    .putUserMetadataAll(srcFileInfo.getMetadata() != null, srcFileInfo.getUserMetadata())
                    .putThumbnailUserMetadataAll(srcFileInfo.getThumbnailUserMetadata() != null, srcFileInfo.getThumbnailUserMetadata())
                    .setProgressListener(pre.getProgressListener())
                    .putAttrAll(srcFileInfo.getAttr() != null, srcFileInfo.getAttr())
                    .upload(fileStorageService.getFileStorageVerify(pre.getPlatform()), fileRecorder, aspectList);
        });
        return destFileInfoArr[0];
    }
}
