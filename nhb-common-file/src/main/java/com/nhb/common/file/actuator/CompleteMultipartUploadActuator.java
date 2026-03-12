package com.nhb.common.file.actuator;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.aspect.chain.CompleteMultipartUploadAspectChain;
import com.nhb.common.file.constant.FileStorageConstants;
import com.nhb.common.file.core.ContentTypeDetect;
import com.nhb.common.file.core.Downloader;
import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.exception.ExceptionCheck;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.pretreatment.CompleteMultipartUploadPretreatment;
import com.nhb.common.file.core.FilePartInfoList;
import com.nhb.common.file.core.MultipartUploadSupportInfo;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 手动分片上传-完成执行器
 */
public class CompleteMultipartUploadActuator {
    private final FileStorageService fileStorageService;
    private final CompleteMultipartUploadPretreatment pre;

    public CompleteMultipartUploadActuator(CompleteMultipartUploadPretreatment pre) {
        this.pre = pre;
        this.fileStorageService = pre.getFileStorageService();
    }

    /**
     * 执行完成
     */
    public FileInfo execute() {
        FileInfo fileInfo = pre.getFileInfo();
        ExceptionCheck.completeMultipartUpload(fileInfo);
        FileStorage fileStorage = fileStorageService.getFileStorageVerify(fileInfo.getPlatform());
        fileInfo.setUploadStatus(FileStorageConstants.FileInfoUploadStatus.COMPLETE);
        CopyOnWriteArrayList<FileStorageAspect> aspectList = fileStorageService.getAspectList();
        FileRecorder fileRecorder = fileStorageService.getFileRecorder();
        ContentTypeDetect contentTypeDetect = fileStorageService.getContentTypeDetect();

        // 处理切面
        return new CompleteMultipartUploadAspectChain(
                        aspectList, (_pre, _fileStorage, _fileRecorder, _contentTypeDetect) -> {
                            FileInfo _fileInfo = _pre.getFileInfo();
                            MultipartUploadSupportInfo supportInfo =
                                    fileStorageService.isSupportMultipartUpload(_fileStorage);

                            // 如果未传入分片信息，则获取全部分片
                            if (_pre.getPartInfoList() == null && supportInfo.getIsSupportListParts()) {
                                FilePartInfoList partInfoList =
                                        fileStorageService.listParts(_fileInfo).listParts(_fileStorage, aspectList);
                                _pre.setPartInfoList(partInfoList.getList());
                            }

                            _fileStorage.completeMultipartUpload(_pre);
                            _fileRecorder.update(_fileInfo);
                            _fileRecorder.deleteFilePartByUploadId(_fileInfo.getUploadId());

                            // 文件上传完成，识别文件 ContentType
                            if (StrUtil.isNotBlank(_fileInfo.getContentType())) {
                                try {
                                    new Downloader(_fileInfo, aspectList, _fileStorage, Downloader.TARGET_FILE)
                                            .inputStream(in -> {
                                                try {
                                                    _fileInfo.setContentType(_contentTypeDetect.detect(
                                                            in, _fileInfo.getOriginalFileName()));
                                                    // 这里静默关闭流，防止出现 Premature end of Content-Length
                                                    // delimited message body 错误
                                                    IoUtil.close(in);
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e);
                                                }
                                            });
                                    _fileRecorder.update(_fileInfo);
                                } catch (Exception ignored) {
                                }
                            }
                            return _fileInfo;
                        })
                .next(pre, fileStorage, fileRecorder, contentTypeDetect);
    }
}
