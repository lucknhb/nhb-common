package com.nhb.common.file.platform.storage;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ftp.Ftp;
import com.nhb.common.file.core.*;
import com.nhb.common.file.exception.ExceptionCheck;
import com.nhb.common.file.exception.ExceptionFactory;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.platform.FileStorageClientFactory;
import com.nhb.common.file.pretreatment.GetFilePretreatment;
import com.nhb.common.file.pretreatment.ListFilesPretreatment;
import com.nhb.common.file.pretreatment.MovePretreatment;
import com.nhb.common.file.pretreatment.UploadPretreatment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: FTP 存储
 */
@Getter
@Setter
@NoArgsConstructor
public class FtpFileStorage implements FileStorage {
    private String platform;
    private String domain;
    private String basePath;
    private String storagePath;
    private FileStorageClientFactory<Ftp> clientFactory;

    public FtpFileStorage(FileStorageProperties.FtpConfig config, FileStorageClientFactory<Ftp> clientFactory) {
        platform = config.getPlatform();
        domain = config.getDomain();
        basePath = config.getBasePath();
        storagePath = config.getStoragePath();
        this.clientFactory = clientFactory;
    }

    /**
     * 获取 Client ，使用完后需要归还
     */
    public Ftp getClient() {
        return clientFactory.getClient();
    }

    /**
     * 归还 Client
     */
    public void returnClient(Ftp client) {
        clientFactory.returnClient(client);
    }

    @Override
    public void close() {
        clientFactory.close();
    }

    /**
     * 获取远程绝对路径
     */
    public String getAbsolutePath(String path) {
        return storagePath + path;
    }

    @Override
    public boolean save(FileInfo fileInfo, UploadPretreatment pre) {
        fileInfo.setBasePath(basePath);
        String newFileKey = getFileKey(fileInfo);
        fileInfo.setUrl(domain + newFileKey);
        ExceptionCheck.uploadNotSupportAcl(platform, fileInfo, pre);
        ExceptionCheck.uploadNotSupportMetadata(platform, fileInfo, pre);

        Ftp client = getClient();
        try (InputStreamPlus in = pre.getInputStreamPlus()) {
            client.upload(getAbsolutePath(basePath + fileInfo.getPath()), fileInfo.getFileName(), in);
            if (fileInfo.getSize() == null) fileInfo.setSize(in.getProgressSize());

            byte[] thumbnailBytes = pre.getThumbnailBytes();
            if (thumbnailBytes != null) { // 上传缩略图
                String newThFileKey = getThFileKey(fileInfo);
                fileInfo.setThumbnailUrl(domain + newThFileKey);
                client.upload(
                        getAbsolutePath(basePath + fileInfo.getPath()),
                        fileInfo.getThumbnailFileName(),
                        new ByteArrayInputStream(thumbnailBytes));
            }

            return true;
        } catch (Exception e) {
            try {
                client.delFile(getAbsolutePath(newFileKey));
            } catch (Exception ignored) {
            }
            throw ExceptionFactory.upload(fileInfo, platform, e);
        } finally {
            returnClient(client);
        }
    }

    @Override
    public ListFilesSupportInfo isSupportListFiles() {
        return ListFilesSupportInfo.supportAll().setSupportMaxFiles(Integer.MAX_VALUE);
    }

    @Override
    public ListFilesResult listFiles(ListFilesPretreatment pre) {
        Ftp client = getClient();
        try {
            String path = getAbsolutePath(basePath + pre.getPath());
            List<FTPFile> fileList = Arrays.stream(client.isDir(path) ? client.lsFiles(path) : new FTPFile[0])
                    .filter(f -> f.isFile() || f.isDirectory())
                    .filter(f -> !(".".equals(f.getName()) || "..".equals(f.getName())))
                    .collect(Collectors.toList());
            ListFilesMatchResult<FTPFile> matchResult = listFilesMatch(fileList, FTPFile::getName, pre, true);
            ListFilesResult list = new ListFilesResult();
            list.setDirList(matchResult.getList().stream()
                    .filter(FTPFile::isDirectory)
                    .map(item -> {
                        RemoteDirInfo dir = new RemoteDirInfo();
                        dir.setPlatform(pre.getPlatform());
                        dir.setBasePath(basePath);
                        dir.setPath(pre.getPath());
                        dir.setName(item.getName());
                        dir.setOriginal(item);
                        return dir;
                    })
                    .collect(Collectors.toList()));
            list.setFileList(matchResult.getList().stream()
                    .filter(FTPFile::isFile)
                    .map(item -> {
                        RemoteFileInfo info = new RemoteFileInfo();
                        info.setPlatform(pre.getPlatform());
                        info.setBasePath(basePath);
                        info.setPath(pre.getPath());
                        info.setFilename(item.getName());
                        info.setUrl(domain + getFileKey(new FileInfo(basePath, info.getPath(), info.getFilename())));
                        info.setSize(item.getSize());
                        info.setExt(FileNameUtil.extName(info.getFilename()));
                        info.setLastModified(DateUtil.date(item.getTimestamp()).toLocalDateTime());
                        info.setOriginal(item);
                        return info;
                    })
                    .collect(Collectors.toList()));
            list.setPlatform(pre.getPlatform());
            list.setBasePath(basePath);
            list.setPath(pre.getPath());
            list.setFilenamePrefix(pre.getFilenamePrefix());
            list.setMaxFiles(pre.getMaxFiles());
            list.setIsTruncated(matchResult.getIsTruncated());
            list.setMarker(pre.getMarker());
            list.setNextMarker(matchResult.getNextMarker());
            return list;
        } catch (Exception e) {
            throw ExceptionFactory.listFiles(pre, basePath, e);
        } finally {
            returnClient(client);
        }
    }

    @Override
    public RemoteFileInfo getFile(GetFilePretreatment pre) {
        String fileKey = getFileKey(new FileInfo(basePath, pre.getPath(), pre.getFilename()));
        Ftp client = getClient();
        try {
            String path = getAbsolutePath(basePath + pre.getPath());
            FTPFile file;
            try {
                client.cd(path);
                file = client.getClient().listFiles(pre.getFilename())[0];
            } catch (Exception e) {
                return null;
            }
            if (file == null) return null;

            RemoteFileInfo info = new RemoteFileInfo();
            info.setPlatform(pre.getPlatform());
            info.setBasePath(basePath);
            info.setPath(pre.getPath());
            info.setFilename(file.getName());
            info.setUrl(domain + fileKey);
            info.setSize(file.getSize());
            info.setExt(FileNameUtil.extName(info.getFilename()));
            info.setLastModified(DateUtil.date(file.getTimestamp()).toLocalDateTime());
            info.setOriginal(file);
            return info;
        } catch (Exception e) {
            throw ExceptionFactory.getFile(pre, basePath, e);
        } finally {
            returnClient(client);
        }
    }

    @Override
    public boolean delete(FileInfo fileInfo) {
        Ftp client = getClient();
        try {
            if (fileInfo.getThumbnailFileName() != null) { // 删除缩略图
                client.delFile(getAbsolutePath(getThFileKey(fileInfo)));
            }
            client.delFile(getAbsolutePath(getFileKey(fileInfo)));
            return true;
        } catch (Exception e) {
            throw ExceptionFactory.delete(fileInfo, platform, e);
        } finally {
            returnClient(client);
        }
    }

    @Override
    public boolean exists(FileInfo fileInfo) {
        Ftp client = getClient();
        try {
            client.cd(getAbsolutePath(fileInfo.getBasePath() + fileInfo.getPath()));
            return client.existFile(fileInfo.getFileName());
        } catch (Exception e) {
            throw ExceptionFactory.exists(fileInfo, platform, e);
        } finally {
            returnClient(client);
        }
    }

    @Override
    public void download(FileInfo fileInfo, Consumer<InputStream> consumer) {
        Ftp client = getClient();
        try {
            client.cd(getAbsolutePath(fileInfo.getBasePath() + fileInfo.getPath()));
            FTPClient ftpClient = client.getClient();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            try (InputStream in = ftpClient.retrieveFileStream(fileInfo.getFileName())) {
                if (in == null) {
                    throw ExceptionFactory.download(fileInfo, platform, null);
                }
                consumer.accept(in);
                ftpClient.completePendingCommand();
            }
        } catch (Exception e) {
            throw ExceptionFactory.download(fileInfo, platform, e);
        } finally {
            returnClient(client);
        }
    }

    @Override
    public void downloadTh(FileInfo fileInfo, Consumer<InputStream> consumer) {
        ExceptionCheck.downloadThBlankThFilename(platform, fileInfo);

        Ftp client = getClient();
        try {
            client.cd(getAbsolutePath(fileInfo.getBasePath() + fileInfo.getPath()));
            FTPClient ftpClient = client.getClient();
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            try (InputStream in = ftpClient.retrieveFileStream(fileInfo.getThumbnailFileName())) {
                if (in == null) {
                    throw ExceptionFactory.downloadTh(fileInfo, platform, null);
                }
                consumer.accept(in);
                ftpClient.completePendingCommand();
            }
        } catch (Exception e) {
            throw ExceptionFactory.downloadTh(fileInfo, platform, e);
        } finally {
            returnClient(client);
        }
    }

    @Override
    public boolean isSupportSameMove() {
        return true;
    }

    @Override
    public void sameMove(FileInfo srcFileInfo, FileInfo destFileInfo, MovePretreatment pre) {
        ExceptionCheck.sameMoveNotSupportAcl(platform, srcFileInfo, destFileInfo, pre);
        ExceptionCheck.sameMoveNotSupportMetadata(platform, srcFileInfo, destFileInfo, pre);
        ExceptionCheck.sameMoveBasePath(platform, basePath, srcFileInfo, destFileInfo);

        String srcPath = getAbsolutePath(srcFileInfo.getBasePath() + srcFileInfo.getPath());
        String destPath = getAbsolutePath(destFileInfo.getBasePath() + destFileInfo.getPath());
        String relativizePath =
                Paths.get(srcPath).relativize(Paths.get(destPath)).toString().replace("\\", "/") + "/";

        Ftp client = getClient();
        try {
            FTPClient ftpClient = client.getClient();
            client.cd(srcPath);

            FTPFile srcFile;
            try {
                srcFile = ftpClient.listFiles(srcFileInfo.getFileName())[0];
            } catch (Exception e) {
                throw ExceptionFactory.sameMoveNotFound(srcFileInfo, destFileInfo, platform, e);
            }

            // 移动缩略图文件
            String destThFileRelativizeKey = null;
            if (StrUtil.isNotBlank(srcFileInfo.getThumbnailFileName())) {
                destFileInfo.setThumbnailUrl(domain + getThFileKey(destFileInfo));
                destThFileRelativizeKey = relativizePath + destFileInfo.getThumbnailFileName();
                try {
                    client.mkDirs(destPath);
                    ftpClient.rename(srcFileInfo.getThumbnailFileName(), destThFileRelativizeKey);
                } catch (Exception e) {
                    throw ExceptionFactory.sameMoveTh(srcFileInfo, destFileInfo, platform, e);
                }
            }

            // 移动文件
            String destFileKey = getFileKey(destFileInfo);
            destFileInfo.setUrl(domain + destFileKey);
            String destFileRelativizeKey = relativizePath + destFileInfo.getFileName();
            try {
                ProgressListener.quickStart(pre.getProgressListener(), srcFile.getSize());
                ftpClient.rename(srcFileInfo.getFileName(), destFileRelativizeKey);
                ProgressListener.quickFinish(pre.getProgressListener(), srcFile.getSize());
            } catch (Exception e) {
                if (destThFileRelativizeKey != null) {
                    try {
                        ftpClient.rename(destThFileRelativizeKey, srcFileInfo.getThumbnailFileName());
                    } catch (Exception ignored) {
                    }
                }
                try {
                    if (client.existFile(srcFileInfo.getFileName())) {
                        client.delFile(destFileRelativizeKey);
                    } else {
                        ftpClient.rename(destFileRelativizeKey, srcFileInfo.getFileName());
                    }
                } catch (Exception ignored) {
                }
                throw ExceptionFactory.sameMove(srcFileInfo, destFileInfo, platform, e);
            }
        } finally {
            returnClient(client);
        }
    }
}
