package com.nhb.common.file.pretreatment;

import com.nhb.common.file.actuator.MoveActuator;
import com.nhb.common.file.constant.FileStorageConstants;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.core.ProgressListener;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/10 0:47
 * @description 移动预处理
 */
@Getter
@Setter
@Accessors(chain = true)
public class MovePretreatment {
    private final FileStorageService fileStorageService;
    private final FileInfo fileInfo;
    /**
     * 移动模式
     */
    private FileStorageConstants.MoveMode moveMode = FileStorageConstants.MoveMode.AUTO;
    /**
     * 复制模式（仅在跨平台移动模式下生效）
     */
    private FileStorageConstants.CopyMode copyMode = FileStorageConstants.CopyMode.AUTO;
    /**
     * 存储平台
     */
    private String platform;
    /**
     * 文件存储路径
     */
    private String path;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 缩略图名称
     */
    private String thumbnailFileName;
    /**
     * 移动进度监听器
     */
    private ProgressListener progressListener;
    /**
     * 不支持元数据时抛出异常
     */
    private Boolean notSupportMetadataThrowException = true;
    /**
     * 不支持 ACL 时抛出异常
     */
    private Boolean notSupportAclThrowException = true;

    /**
     * 构造文件移动器
     */
    public MovePretreatment(FileInfo fileInfo, FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        this.fileInfo = fileInfo;
        this.platform = fileInfo.getPlatform();
        this.path = fileInfo.getPath();
        this.fileName = fileInfo.getFileName();
        this.thumbnailFileName = fileInfo.getThumbnailFileName();
    }

    /**
     * 设置移动模式
     */
    public MovePretreatment setMoveMode(boolean flag, FileStorageConstants.MoveMode moveMode) {
        if (flag) this.moveMode = moveMode;
        return this;
    }

    /**
     * 设置复制模式（仅在跨平台移动模式下生效）
     */
    public MovePretreatment setCopyMode(boolean flag, FileStorageConstants.CopyMode copyMode) {
        if (flag) this.copyMode = copyMode;
        return this;
    }

    /**
     * 设置存储平台
     */
    public MovePretreatment setPlatform(boolean flag, String platform) {
        if (flag) this.platform = platform;
        return this;
    }

    /**
     * 设置文件存储路径
     */
    public MovePretreatment setPath(boolean flag, String path) {
        if (flag) this.path = path;
        return this;
    }

    /**
     * 设置文件名称
     */
    public MovePretreatment setFilename(boolean flag, String filename) {
        if (flag) this.fileName = filename;
        return this;
    }

    /**
     * 设置缩略图名称
     */
    public MovePretreatment setThFilename(boolean flag, String thFilename) {
        if (flag) this.thumbnailFileName = thFilename;
        return this;
    }

    /**
     * 设置移动进度监听器
     *
     * @param progressListener 提供一个参数，表示已传输字节数
     */
    public MovePretreatment setProgressListener(Consumer<Long> progressListener) {
        return setProgressListener((progressSize, allSize) -> progressListener.accept(progressSize));
    }

    /**
     * 设置移动进度监听器
     *
     * @param progressListener 提供一个参数，表示已传输字节数
     */
    public MovePretreatment setProgressListener(boolean flag, Consumer<Long> progressListener) {
        if (flag) setProgressListener((progressSize, allSize) -> progressListener.accept(progressSize));
        return this;
    }

    /**
     * 设置移动进度监听器
     *
     * @param progressListener 提供两个参数，第一个是 progressSize已传输字节数，第二个是 allSize总字节数
     */
    public MovePretreatment setProgressListener(BiConsumer<Long, Long> progressListener) {
        return setProgressListener(new ProgressListener() {
            @Override
            public void start() {}

            @Override
            public void progress(long progressSize, Long allSize) {
                progressListener.accept(progressSize, allSize);
            }

            @Override
            public void finish() {}
        });
    }

    /**
     * 设置移动进度监听器
     *
     * @param progressListener 提供两个参数，第一个是 progressSize已传输字节数，第二个是 allSize总字节数
     */
    public MovePretreatment setProgressListener(boolean flag, BiConsumer<Long, Long> progressListener) {
        if (flag) setProgressListener(progressListener);
        return this;
    }

    /**
     * 设置移动进度监听器
     */
    public MovePretreatment setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
        return this;
    }

    /**
     * 设置移动进度监听器
     */
    public MovePretreatment setProgressListener(boolean flag, ProgressListener progressListener) {
        if (flag) this.progressListener = progressListener;
        return this;
    }

    /**
     * 设置不支持元数据时抛出异常
     */
    public MovePretreatment setNotSupportMetadataThrowException(
            boolean flag, Boolean notSupportMetadataThrowException) {
        if (flag) this.notSupportMetadataThrowException = notSupportMetadataThrowException;
        return this;
    }

    /**
     * 设置不支持 ACL 时抛出异常
     */
    public MovePretreatment setNotSupportAclThrowException(boolean flag, Boolean notSupportAclThrowException) {
        if (flag) this.notSupportAclThrowException = notSupportAclThrowException;
        return this;
    }

    /**
     * 移动文件，成功后返回新的 FileInfo
     */
    public FileInfo move() {
        return new MoveActuator(this).execute();
    }
}
