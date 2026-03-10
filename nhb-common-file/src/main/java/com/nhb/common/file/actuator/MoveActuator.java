package com.nhb.common.file.actuator;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.chain.MoveAspectChain;
import com.nhb.common.file.aspect.chain.SameMoveAspectChain;
import com.nhb.common.file.constant.FileStorageConstants;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.exception.FileStorageException;
import com.nhb.common.file.hash.HashInfo;
import com.nhb.common.file.pretreatment.MovePretreatment;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 移动执行器
 */
public class MoveActuator {
    private final FileStorageService fileStorageService;
    private final FileStorage fileStorage;
    private final FileInfo fileInfo;
    private final MovePretreatment pre;

    public MoveActuator(MovePretreatment pre) {
        this.pre = pre;
        this.fileStorageService = pre.getFileStorageService();
        this.fileInfo = pre.getFileInfo();
        this.fileStorage = fileStorageService.getFileStorageVerify(fileInfo.getPlatform());
    }

    /**
     * 移动文件，成功后返回新的 FileInfo
     */
    public FileInfo execute() {
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
        List<FileStorageAspect> aspectList = fileStorageService.getAspectList();
        FileRecorder fileRecorder = fileStorageService.getFileRecorder();
        return new MoveAspectChain(aspectList, (_srcFileInfo, _pre, _fileStorage, _fileRecorder) -> {
                    // 真正开始移动
                    FileInfo destFileInfo;
                    if (isSameMove(_srcFileInfo, _pre, _fileStorage)) {
                        destFileInfo = sameMove(_srcFileInfo, _pre, _fileStorage, _fileRecorder, aspectList);
                    } else {
                        destFileInfo = crossMove(_srcFileInfo, _pre, _fileStorage, _fileRecorder, aspectList);
                    }
                    return destFileInfo;
                })
                .next(fileInfo, pre, fileStorage, fileRecorder);
    }

    /**
     * 判断是否使用同存储平台移动
     */
    protected boolean isSameMove(FileInfo srcFileInfo, MovePretreatment pre, FileStorage fileStorage) {
        FileStorageConstants.MoveMode moveMode = pre.getMoveMode();
        if (moveMode == FileStorageConstants.MoveMode.SAME) {
            if (!fileStorageService.isSupportSameMove(fileStorage)) {
                throw new FileStorageException("存储平台【" + fileStorage.getPlatform() + "】不支持同存储平台移动");
            }
            return true;
        } else if (moveMode == FileStorageConstants.MoveMode.CROSS) {
            return false;
        } else {
            return srcFileInfo.getPlatform().equals(pre.getPlatform())
                    && fileStorageService.isSupportSameMove(fileStorage);
        }
    }

    /**
     * 同存储平台移动
     */
    protected FileInfo sameMove(
            FileInfo srcFileInfo,
            MovePretreatment pre,
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

        return new SameMoveAspectChain(aspectList, (_srcFileInfo, _destFileInfo, _pre, _fileStorage, _fileRecorder) -> {
                    _fileStorage.sameMove(_srcFileInfo, _destFileInfo, _pre);
                    _fileRecorder.save(_destFileInfo);

                    // 如果源文件删除失败，则表示移动失败
                    if (!fileStorageService.delete(_srcFileInfo, _fileStorage, _fileRecorder, aspectList)) {
                        throw new FileStorageException("移动文件失败，源文件删除失败");
                    }

                    return _destFileInfo;
                })
                .next(srcFileInfo, destFileInfo, pre, fileStorage, fileRecorder);
    }

    /**
     * 跨存储平台移动，通过从复制并删除旧文件来实现
     */
    protected FileInfo crossMove(
            FileInfo srcFileInfo,
            MovePretreatment pre,
            FileStorage fileStorage,
            FileRecorder fileRecorder,
            List<FileStorageAspect> aspectList) {

        // 复制源文件
        FileInfo destFileInfo = fileStorageService
                .copy(srcFileInfo)
                .setCopyMode(pre.getCopyMode())
                .setPlatform(pre.getPlatform())
                .setPath(pre.getPath())
                .setFileName(pre.getFileName())
                .setThumbnailFileName(pre.getThumbnailFileName())
                .setProgressListener(pre.getProgressListener())
                .setNotSupportMetadataThrowException(
                        !pre.getNotSupportMetadataThrowException(), pre.getNotSupportMetadataThrowException())
                .setNotSupportAclThrowException(
                        !pre.getNotSupportAclThrowException(), pre.getNotSupportAclThrowException())
                .copy(fileStorage, fileRecorder, aspectList);

        if (destFileInfo == null) {
            throw new FileStorageException("移动文件失败，源文件复制失败");
        }

        // 如果源文件删除失败，则表示移动失败
        if (!fileStorageService.delete(srcFileInfo, fileStorage, fileRecorder, aspectList)) {
            throw new FileStorageException("移动文件失败，源文件删除失败");
        }

        return destFileInfo;
    }
}
