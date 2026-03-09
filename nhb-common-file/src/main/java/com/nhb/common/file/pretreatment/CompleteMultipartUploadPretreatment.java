package com.nhb.common.file.pretreatment;

import com.nhb.common.file.actuator.CompleteMultipartUploadActuator;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.core.ProgressListenerSetter;
import com.nhb.common.file.core.ProgressListener;
import com.nhb.common.file.core.FilePartInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 手动分片上传-完成预处理器
 */
@Getter
@Setter
@Accessors(chain = true)
public class CompleteMultipartUploadPretreatment
        implements ProgressListenerSetter<CompleteMultipartUploadPretreatment> {
    /**
     * 文件存储服务类
     */
    private FileStorageService fileStorageService;
    /**
     * 文件信息
     */
    private FileInfo fileInfo;
    /**
     * 文件分片信息，不传则自动使用全部已上传的分片
     */
    private List<FilePartInfo> partInfoList;
    /**
     * 完成进度监听
     */
    private ProgressListener progressListener;

    /**
     * 如果条件为 true 则：设置文件存储服务类
     * @param flag 条件
     * @param fileStorageService 文件存储服务类
     * @return 手动分片上传-完成预处理器
     */
    public CompleteMultipartUploadPretreatment setFileStorageService(
            boolean flag, FileStorageService fileStorageService) {
        if (flag) setFileStorageService(fileStorageService);
        return this;
    }

    /**
     * 如果条件为 true 则：设置文件信息
     * @param flag 条件
     * @param fileInfo 文件信息
     * @return 手动分片上传-完成预处理器
     */
    public CompleteMultipartUploadPretreatment setFileInfo(boolean flag, FileInfo fileInfo) {
        if (flag) setFileInfo(fileInfo);
        return this;
    }

    /**
     * 如果条件为 true 则：设置文件分片信息，不传则自动使用全部已上传的分片
     * @param flag 条件
     * @param partInfoList 文件分片信息，不传则自动使用全部已上传的分片
     * @return 手动分片上传-完成预处理器
     */
    public CompleteMultipartUploadPretreatment setFileInfo(boolean flag, List<FilePartInfo> partInfoList) {
        if (flag) setPartInfoList(partInfoList);
        return this;
    }

    /**
     * 执行完成
     */
    public FileInfo complete() {
        return new CompleteMultipartUploadActuator(this).execute();
    }
}
