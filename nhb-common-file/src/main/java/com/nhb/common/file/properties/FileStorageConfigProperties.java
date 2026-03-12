package com.nhb.common.file.properties;

import com.nhb.common.file.core.FileStorageProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/10 16:04
 * @description 属性配置
 */
@Data
@RefreshScope
@Accessors(chain = true)
@ConfigurationProperties(prefix = FileStorageConfigProperties.PREFIX)
public class FileStorageConfigProperties {
    public static final String PREFIX = "file";

    /**
     * 默认存储平台
     */
    private String defaultPlatform = "local";
    /**
     * 缩略图后缀，例如【.min.jpg】【.png】
     */
    private String thumbnailSuffix = ".min.jpg";
    /**
     * 上传时不支持元数据时抛出异常
     */
    private Boolean uploadNotSupportMetadataThrowException = true;
    /**
     * 上传时不支持 ACL 时抛出异常
     */
    private Boolean uploadNotSupportAclThrowException = true;
    /**
     * 复制时不支持元数据时抛出异常
     */
    private Boolean copyNotSupportMetadataThrowException = true;
    /**
     * 复制时不支持 ACL 时抛出异常
     */
    private Boolean copyNotSupportAclThrowException = true;
    /**
     * 移动时不支持元数据时抛出异常
     */
    private Boolean moveNotSupportMetadataThrowException = true;
    /**
     * 移动时不支持 ACL 时抛出异常
     */
    private Boolean moveNotSupportAclThrowException = true;
    /**
     * 启用 byte[] 文件包装适配器
     */
    private Boolean enableByteFileWrapper = true;
    /**
     * 启用 URI 文件包装适配器，包含 URL 和 String
     */
    private Boolean enableUriFileWrapper = true;
    /**
     * 启用 InputStream 文件包装适配器
     */
    private Boolean enableInputStreamFileWrapper = true;
    /**
     * 启用本地文件包装适配器
     */
    private Boolean enableLocalFileWrapper = true;
    /**
     * 启用 HttpServletRequest 文件包装适配器
     */
    private Boolean enableHttpServletRequestFileWrapper = true;
    /**
     * 启用 MultipartFile 文件包装适配器
     */
    private Boolean enableMultipartFileWrapper = true;
    /**
     * 本地存储
     */
    private List<? extends LocalConfigProperties> local = new ArrayList<>();
    /**
     * 华为云 OBS
     */
    private List<? extends HuaweiObsConfigProperties> huaweiObs = new ArrayList<>();
    /**
     * 阿里云 OSS
     */
    private List<? extends AliyunOssConfigProperties> aliyunOss = new ArrayList<>();
    /**
     * 腾讯云 COS
     */
    private List<? extends TencentCosConfigProperties> tencentCos = new ArrayList<>();
    /**
     * 百度云 BOS
     */
    private List<? extends BaiduBosConfigProperties> baiduBos = new ArrayList<>();

    /**
     * MinIO USS
     */
    private List<? extends MinioConfigProperties> minio = new ArrayList<>();

    /**
     * Amazon S3
     */
    private List<? extends SpringAmazonS3Config> amazonS3 = new ArrayList<>();

    /**
     * Amazon S3
     */
    private List<? extends AmazonS3V2ConfigProperties> amazonS3V2 = new ArrayList<>();

    /**
     * FTP
     */
    private List<? extends FtpConfigProperties> ftp = new ArrayList<>();

    /**
     * FTP
     */
    private List<? extends SftpConfigProperties> sftp = new ArrayList<>();

    /**
     * WebDAV
     */
    private List<? extends SpringWebDavConfig> webdav = new ArrayList<>();

    /**
     * GoogleCloud Storage
     */
    private List<? extends GoogleCloudStorageConfigProperties> googleCloudStorage = new ArrayList<>();

    /**
     * Azure Blob Storage
     */
    private List<? extends SpringAzureBlobStorageConfig> azureBlob = new ArrayList<>();

    /**
     * Mongo GridFS
     */
    private List<? extends MongoGridFsConfigProperties> mongoGridFs = new ArrayList<>();

    /**
     * 火山引擎 TOS
     */
    private List<? extends VolcengineTosConfigProperties> volcengineTos = new ArrayList<>();

    /**
     * 转换成 FileStorageProperties ，并过滤掉没有启用的存储平台
     */
    public FileStorageProperties toFileStorageProperties() {
        FileStorageProperties properties = new FileStorageProperties();
        properties.setDefaultPlatform(defaultPlatform);
        properties.setThumbnailSuffix(thumbnailSuffix);
        properties.setUploadNotSupportMetadataThrowException(uploadNotSupportMetadataThrowException);
        properties.setUploadNotSupportAclThrowException(uploadNotSupportAclThrowException);
        properties.setCopyNotSupportMetadataThrowException(copyNotSupportMetadataThrowException);
        properties.setCopyNotSupportAclThrowException(copyNotSupportAclThrowException);
        properties.setMoveNotSupportMetadataThrowException(moveNotSupportMetadataThrowException);
        properties.setMoveNotSupportAclThrowException(moveNotSupportAclThrowException);
        properties.setLocal(
                local.stream().filter(LocalConfigProperties::getEnableStorage).collect(Collectors.toList()));
        properties.setLocal(local.stream()
                .filter(LocalConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setHuaweiObs(huaweiObs.stream()
                .filter(HuaweiObsConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setAliyunOss(aliyunOss.stream()
                .filter(AliyunOssConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setTencentCos(tencentCos.stream()
                .filter(TencentCosConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setBaiduBos(baiduBos.stream()
                 .filter(BaiduBosConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setMinio(minio.stream()
                .filter(MinioConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setAmazonS3(amazonS3.stream()
                .filter(SpringAmazonS3Config::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setAmazonS3V2(amazonS3V2.stream()
                .filter(AmazonS3V2ConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setFtp(ftp.stream()
                .filter(FtpConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setSftp(sftp.stream()
                .filter(SftpConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setWebdav(webdav.stream()
                .filter(SpringWebDavConfig::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setGoogleCloudStorage(googleCloudStorage.stream()
                .filter(GoogleCloudStorageConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setAzureBlob(azureBlob.stream()
                .filter(SpringAzureBlobStorageConfig::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setMongoGridFs(mongoGridFs.stream()
                .filter(MongoGridFsConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));
        properties.setVolcengineTos(volcengineTos.stream()
                .filter(VolcengineTosConfigProperties::getEnableStorage)
                .collect(Collectors.toList()));

        return properties;
    }

    /**
     * 本地存储
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class LocalConfigProperties extends FileStorageProperties.LocalConfig {
        /**
         * 本地存储访问路径
         */
        private String[] pathPatterns = new String[0];
        /**
         * 启用本地存储
         */
        private Boolean enableStorage = false;
        /**
         * 启用本地访问
         */
        private Boolean enableAccess = false;
    }

    /**
     * 华为云 OBS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class HuaweiObsConfigProperties extends FileStorageProperties.HuaweiObsConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }

    /**
     * 阿里云 OSS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class AliyunOssConfigProperties extends FileStorageProperties.AliyunOssConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }


    /**
     * 腾讯云 COS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class TencentCosConfigProperties extends FileStorageProperties.TencentCosConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }

    /**
     * 百度云 BOS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class BaiduBosConfigProperties extends FileStorageProperties.BaiduBosConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }


    /**
     * MinIO
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class MinioConfigProperties extends FileStorageProperties.MinioConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }

    /**
     * Amazon S3
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class SpringAmazonS3Config extends FileStorageProperties.AmazonS3Config {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }

    /**
     * Amazon S3
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class AmazonS3V2ConfigProperties extends FileStorageProperties.AmazonS3V2Config {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }

    /**
     * FTP
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class FtpConfigProperties extends FileStorageProperties.FtpConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }

    /**
     * SFTP
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class SftpConfigProperties extends FileStorageProperties.SftpConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }

    /**
     * WebDAV
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class SpringWebDavConfig extends FileStorageProperties.WebDavConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }

    /**
     * GoogleCloud Storage
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class GoogleCloudStorageConfigProperties extends FileStorageProperties.GoogleCloudStorageConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }

    /**
     * AzureBlob Storage
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class SpringAzureBlobStorageConfig extends FileStorageProperties.AzureBlobStorageConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }

    /**
     * Mongo GridFS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class MongoGridFsConfigProperties extends FileStorageProperties.MongoGridFsConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }


    /**
     * 火山引擎 TOS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class VolcengineTosConfigProperties extends FileStorageProperties.VolcengineTosConfig {
        /**
         * 启用存储
         */
        private Boolean enableStorage = false;
    }

}
