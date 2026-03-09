package com.nhb.common.file.recorder;

import com.nhb.common.file.core.FileInfo;
import com.nhb.common.file.core.FilePartInfo;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 16:49
 * @description: 文件记录记录者接口 业务方可实现里面的方式
 */
public interface FileRecorder {
    /**
     * 保存文件记录
     */
    boolean save(FileInfo fileInfo);

    /**
     * 更新文件记录，可以根据文件 ID 或 URL 来更新文件记录，
     * 主要用在手动分片上传文件-完成上传，作用是更新文件信息
     */
    void update(FileInfo fileInfo);

    /**
     * 根据 url 获取文件记录
     */
    FileInfo getByUrl(String url);

    /**
     * 根据 url 删除文件记录
     */
    boolean delete(String url);

    /**
     * 保存文件分片信息
     * @param filePartInfo 文件分片信息
     */
    void saveFilePart(FilePartInfo filePartInfo);

    /**
     * 删除文件分片信息
     */
    void deleteFilePartByUploadId(String uploadId);
}
