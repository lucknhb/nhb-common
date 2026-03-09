package com.nhb.common.file.core;

import cn.hutool.core.map.MapBuilder;
import com.nhb.common.file.constant.FileStorageConstants;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 16:22
 * @description: 文件存储配置
 */
@Data
@Accessors(chain = true)
public class FileStorageProperties {
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
     * 本地存储
     */
    private List<? extends LocalConfig> local = new ArrayList<>();

    /**
     * 华为云 OBS
     */
    private List<? extends HuaweiObsConfig> huaweiObs = new ArrayList<>();

    /**
     * 阿里云 OSS
     */
    private List<? extends AliyunOssConfig> aliyunOss = new ArrayList<>();

    /**
     * 腾讯云 COS
     */
    private List<? extends TencentCosConfig> tencentCos = new ArrayList<>();

    /**
     * 百度云 BOS
     */
    private List<? extends BaiduBosConfig> baiduBos = new ArrayList<>();

    /**
     * MinIO USS
     */
    private List<? extends MinioConfig> minio = new ArrayList<>();

    /**
     * Amazon S3
     */
    private List<? extends AmazonS3Config> amazonS3 = new ArrayList<>();

    /**
     * Amazon S3 V2
     */
    private List<? extends AmazonS3V2Config> amazonS3V2 = new ArrayList<>();

    /**
     * FTP
     */
    private List<? extends FtpConfig> ftp = new ArrayList<>();

    /**
     * SFTP
     */
    private List<? extends SftpConfig> sftp = new ArrayList<>();

    /**
     * WebDAV
     */
    private List<? extends WebDavConfig> webdav = new ArrayList<>();

    /**
     * 谷歌云存储
     */
    private List<? extends GoogleCloudStorageConfig> googleCloudStorage = new ArrayList<>();

    /**
     * Azure Blob Storage
     */
    private List<? extends AzureBlobStorageConfig> azureBlob = new ArrayList<>();

    /**
     * Mongo GridFS
     */
    private List<? extends MongoGridFsConfig> mongoGridFs = new ArrayList<>();

    /**
     * 火山引擎 TOS
     */
    private List<? extends VolcengineTosConfig> volcengineTos = new ArrayList<>();

    /**
     * 基本的存储平台配置
     */
    @Data
    @Accessors(chain = true)
    public static class BaseConfig {

        /**
         * 存储平台
         */
        private String platform = "";
    }

    /**
     * 本地存储
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class LocalConfig extends BaseConfig {

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 存储路径，上传的文件都会存储在这个路径下面，默认"/"，注意"/"结尾
         */
        private String storagePath = "/";

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * 华为云 OBS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class HuaweiObsConfig extends BaseConfig {

        private String accessKey;

        private String secretKey;

        private String endPoint;

        private String bucketName;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 默认的 ACL，详情 {@link FileStorageConstants.HuaweiObsACL}
         */
        private String defaultAcl;

        /**
         * 自动分片上传阈值，达到此大小则使用分片上传，默认 128MB
         */
        private int multipartThreshold = 128 * 1024 * 1024;

        /**
         * 自动分片上传时每个分片大小，默认 32MB
         */
        private int multipartPartSize = 32 * 1024 * 1024;

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * 阿里云 OSS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class AliyunOssConfig extends BaseConfig {

        private String accessKey;

        private String secretKey;

        private String endPoint;

        private String bucketName;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 默认的 ACL，详情 {@link FileStorageConstants.AliyunOssACL}
         */
        private String defaultAcl;

        /**
         * 自动分片上传阈值，达到此大小则使用分片上传，默认 128MB
         */
        private int multipartThreshold = 128 * 1024 * 1024;

        /**
         * 自动分片上传时每个分片大小，默认 32MB
         */
        private int multipartPartSize = 32 * 1024 * 1024;

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * 腾讯云 COS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class TencentCosConfig extends BaseConfig {

        private String secretId;

        private String secretKey;

        private String region;

        private String bucketName;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 默认的 ACL，详情 {@link FileStorageConstants.TencentCosACL}
         */
        private String defaultAcl;

        /**
         * 自动分片上传阈值，达到此大小则使用分片上传，默认 128MB
         */
        private int multipartThreshold = 128 * 1024 * 1024;

        /**
         * 自动分片上传时每个分片大小，默认 32MB
         */
        private int multipartPartSize = 32 * 1024 * 1024;

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * 百度云 BOS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class BaiduBosConfig extends BaseConfig {

        private String accessKey;

        private String secretKey;

        private String endPoint;

        private String bucketName;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 默认的 ACL，详情 {@link FileStorageConstants.BaiduBosACL}
         */
        private String defaultAcl;

        /**
         * 自动分片上传阈值，达到此大小则使用分片上传，默认 128MB
         */
        private int multipartThreshold = 128 * 1024 * 1024;

        /**
         * 自动分片上传时每个分片大小，默认 32MB
         */
        private int multipartPartSize = 32 * 1024 * 1024;

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * MinIO
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class MinioConfig extends BaseConfig {

        private String accessKey;

        private String secretKey;

        private String endPoint;

        private String bucketName;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 自动分片上传阈值，达到此大小则使用分片上传，默认 128MB。
         * 在获取不到文件大小或达到这个阈值的情况下，会使用这里提供的分片大小，否则 MinIO 会自动分片大小
         */
        private int multipartThreshold = 128 * 1024 * 1024;
        /**
         * 自动分片上传时每个分片大小，默认 32MB
         */
        private int multipartPartSize = 32 * 1024 * 1024;
        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * Amazon S3
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class AmazonS3Config extends BaseConfig {

        private String accessKey;

        private String secretKey;

        private String region;

        private String endPoint;

        private String bucketName;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 默认的 ACL，详情 {@link FileStorageConstants.AwsS3ACL}
         */
        private String defaultAcl;

        /**
         * 自动分片上传阈值，达到此大小则使用分片上传，默认 128MB
         */
        private int multipartThreshold = 128 * 1024 * 1024;

        /**
         * 自动分片上传时每个分片大小，默认 32MB
         */
        private int multipartPartSize = 32 * 1024 * 1024;

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * Amazon S3 V2
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class AmazonS3V2Config extends BaseConfig {

        private String accessKey;

        private String secretKey;

        private String region;

        private String endPoint;

        private String bucketName;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 默认的 ACL，详情 {@link FileStorageConstants.AwsS3ACL}
         */
        private String defaultAcl;

        /**
         * 自动分片上传阈值，达到此大小则使用分片上传，默认 128MB
         */
        private int multipartThreshold = 128 * 1024 * 1024;

        /**
         * 自动分片上传时每个分片大小，默认 32MB
         */
        private int multipartPartSize = 32 * 1024 * 1024;

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * FTP
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class FtpConfig extends BaseConfig {

        /**
         * 主机
         */
        private String host;

        /**
         * 端口，默认21
         */
        private int port = 21;

        /**
         * 用户名，默认 anonymous（匿名）
         */
        private String user = "anonymous";

        /**
         * 密码，默认空
         */
        private String password = "";

        /**
         * 编码，默认UTF-8
         */
        private Charset charset = StandardCharsets.UTF_8;

        /**
         * 连接超时时长，单位毫秒，默认10秒 {@link org.apache.commons.net.SocketClient#setConnectTimeout(int)}
         */
        private long connectionTimeout = 10 * 1000;

        /**
         * Socket连接超时时长，单位毫秒，默认10秒 {@link org.apache.commons.net.SocketClient#setSoTimeout(int)}
         */
        private long soTimeout = 10 * 1000;

        /**
         * 设置服务器语言，默认空，{@link org.apache.commons.net.ftp.FTPClientConfig#setServerLanguageCode(String)}
         */
        private String serverLanguageCode;

        /**
         * 服务器标识，默认空，{@link org.apache.commons.net.ftp.FTPClientConfig#FTPClientConfig(String)}
         * 例如：org.apache.commons.net.ftp.FTPClientConfig.SYST_NT
         */
        private String systemKey;

        /**
         * 是否主动模式，默认被动模式
         */
        private Boolean isActive = false;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 存储路径，上传的文件都会存储在这个路径下面，默认"/"，注意"/"结尾
         */
        private String storagePath = "/";

        /**
         * Client 对象池配置
         */
        private CommonClientPoolConfig pool = new CommonClientPoolConfig();

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * SFTP
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class SftpConfig extends BaseConfig {

        /**
         * 主机
         */
        private String host;

        /**
         * 端口，默认22
         */
        private int port = 22;

        /**
         * 用户名
         */
        private String user;

        /**
         * 密码
         */
        private String password;

        /**
         * 私钥路径
         */
        private String privateKeyPath;

        /**
         * 编码，默认UTF-8
         */
        private Charset charset = StandardCharsets.UTF_8;

        /**
         * 连接超时时长，单位毫秒，默认10秒
         */
        private int connectionTimeout = 10 * 1000;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 存储路径，上传的文件都会存储在这个路径下面，默认"/"，注意"/"结尾
         */
        private String storagePath = "/";

        /**
         * Client 对象池配置
         */
        private CommonClientPoolConfig pool = new CommonClientPoolConfig();

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * WebDAV
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class WebDavConfig extends BaseConfig {

        /**
         * 服务器地址，注意"/"结尾，例如：http://192.168.1.105:8405/
         */
        private String server;

        /**
         * 用户名
         */
        private String user;

        /**
         * 密码
         */
        private String password;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 存储路径，上传的文件都会存储在这个路径下面，默认"/"，注意"/"结尾
         */
        private String storagePath = "/";

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class GoogleCloudStorageConfig extends BaseConfig {

        private String projectId;

        /**
         * 证书路径，兼容Spring的ClassPath路径、文件路径、HTTP路径等
         */
        private String credentialsPath;

        private String bucketName;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 默认的 ACL，详情 {@link FileStorageConstants.GoogleCloudStorageACL}
         */
        private String defaultAcl;

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }



    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class AzureBlobStorageConfig extends BaseConfig {

        /**
         * 终结点 AzureBlob控制台-设置-终结点-主终结点-Blob服务
         */
        private String endPoint;

        /**
         * 访问域名，注意"/"结尾，与 end-point 保持一致
         */
        private String domain = "";

        /**
         * 容器名称，类似于 s3 的 bucketName，AzureBlob控制台-数据存储-容器
         */
        private String containerName;

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 默认的 ACL，详情 {@link FileStorageConstants.AzureBlobStorageACL}
         */
        private String defaultAcl;

        /**
         * 连接字符串，AzureBlob控制台-安全性和网络-访问秘钥-连接字符串
         */
        private String connectionString;

        /**
         * 自动分片上传阈值，超过此大小则使用分片上传，默认值256M
         */
        private long multipartThreshold = 256 * 1024 * 1024L;
        /**
         * 自动分片上传时每个分片大小，默认 4MB
         */
        private long multipartPartSize = 4 * 1024 * 1024L;

        /**
         * 最大上传并行度
         * 分片后 同时进行上传的 数量
         * 数量太大会占用大量缓冲区
         * 默认 8
         */
        private int maxConcurrency = 8;

        /**
         * 预签名 URL 时，传入的 HTTP method 与 Azure Blob Storage 中的 SAS 权限映射表，
         * 目前默认支持 GET （获取），PUT（上传），DELETE（删除），
         * 其它可以自行扩展，例如你想自定义一个 ALL 的 method，赋予所有权限，可以写为 .put("ALL", "racwdxytlmei")
         * {@link com.azure.storage.blob.sas.BlobSasPermission}
         */
        private Map<String, String> methodToPermissionMap = MapBuilder.create(new HashMap<String, String>())
                .put(FileStorageConstants.GeneratePresignedUrl.Method.GET, "r") // 获取
                .put(FileStorageConstants.GeneratePresignedUrl.Method.PUT, "w") // 上传
                .put(FileStorageConstants.GeneratePresignedUrl.Method.DELETE, "d") // 删除
                // .put("ALL", "racwdxytlmei")    //自定义一个名为 ALL 的 method，赋予所有权限
                .build();

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * Mongo GridFS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class MongoGridFsConfig extends BaseConfig {
        /**
         * 链接字符串
         */
        private String connectionString;
        /**
         * 数据库名称
         */
        private String database;
        /**
         * 存储桶名称
         */
        private String bucketName;
        /**
         * 访问域名
         */
        private String domain = "";
        /**
         * 基础路径
         */
        private String basePath = "";
        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * 火山引擎 TOS
     */
    @Data
    @Accessors(chain = true)
    @EqualsAndHashCode(callSuper = true)
    public static class VolcengineTosConfig extends BaseConfig {

        private String accessKey;

        private String secretKey;

        private String endPoint;

        private String region;

        private String bucketName;

        /**
         * 访问域名
         */
        private String domain = "";

        /**
         * 基础路径
         */
        private String basePath = "";

        /**
         * 默认的 ACL 详情 {@link FileStorageConstants.VolcengineTosACL}
         */
        private String defaultAcl;

        /**
         * 自动分片上传阈值，达到此大小则使用分片上传，默认 128MB
         */
        private int multipartThreshold = 128 * 1024 * 1024;

        /**
         * 自动分片上传时每个分片大小，默认 32MB
         */
        private int multipartPartSize = 32 * 1024 * 1024;

        /**
         * 其它自定义配置
         */
        private Map<String, Object> attr = new LinkedHashMap<>();
    }

    /**
     * 通用的 Client 对象池配置，详情见 {@link org.apache.commons.pool2.impl.GenericObjectPoolConfig}
     */
    @Data
    @Accessors(chain = true)
    public static class CommonClientPoolConfig {

        /**
         * 取出对象前进行校验，默认开启
         */
        private Boolean testOnBorrow = true;

        /**
         * 空闲检测，默认开启
         */
        private Boolean testWhileIdle = true;

        /**
         * 最大总数量，超过此数量会进行阻塞等待，默认 16
         */
        private Integer maxTotal = 16;

        /**
         * 最大空闲数量，默认 4
         */
        private Integer maxIdle = 4;

        /**
         * 最小空闲数量，默认 1
         */
        private Integer minIdle = 1;

        /**
         * 空闲对象逐出（销毁）运行间隔时间，默认 30 秒
         */
        private Duration timeBetweenEvictionRuns = Duration.ofSeconds(30);

        /**
         * 对象空闲超过此时间将逐出（销毁），为负数则关闭此功能，默认 -1
         */
        private Duration minEvictableIdleDuration = Duration.ofMillis(-1);

        /**
         * 对象空闲超过此时间且当前对象池的空闲对象数大于最小空闲数量，将逐出（销毁），为负数则关闭此功能，默认 30 分钟
         */
        private Duration softMinEvictableIdleDuration = Duration.ofMillis(30);

        public <T> GenericObjectPoolConfig<T> toGenericObjectPoolConfig() {
            GenericObjectPoolConfig<T> config = new GenericObjectPoolConfig<>();
            config.setTestOnBorrow(testOnBorrow);
            config.setTestWhileIdle(testWhileIdle);
            config.setMaxTotal(maxTotal);
            config.setMinIdle(minIdle);
            config.setMaxIdle(maxIdle);
            config.setTimeBetweenEvictionRuns(timeBetweenEvictionRuns);
            config.setMinEvictableIdleDuration(minEvictableIdleDuration);
            config.setSoftMinEvictableIdleDuration(softMinEvictableIdleDuration);
            return config;
        }
    }
}
