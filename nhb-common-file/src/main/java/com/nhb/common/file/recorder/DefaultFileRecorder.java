package com.nhb.common.file.recorder;

import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.exception.FileStorageException;
import com.nhb.common.file.upload.FilePartInfo;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 16:53
 * @description: 默认的文件记录者类，此类并不能真正保存、查询、删除记录，只是用来脱离数据库运行，保证文件上传功能可以正常使用
 */
public class DefaultFileRecorder implements FileRecorder {
    @Override
    public boolean save(FileInfo fileInfo) {
        return true;
    }

    public void update(FileInfo fileInfo) {}

    @Override
    public FileInfo getByUrl(String url) {
        //可参考保存上传记录 https://x-file-storage.xuyanwu.cn/2.3.0/#/%E5%9F%BA%E7%A1%80%E5%8A%9F%E8%83%BD?id=%E4%BF%9D%E5%AD%98%E4%B8%8A%E4%BC%A0%E8%AE%B0%E5%BD%95
        throw new FileStorageException("The FileRecorder interface has not yet been implemented, so this feature is not available for the time being");
    }

    @Override
    public boolean delete(String url) {
        return true;
    }

    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {}

    @Override
    public void deleteFilePartByUploadId(String uploadId) {}
}
