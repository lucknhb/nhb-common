package com.nhb.common.file.platform.factory;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.nhb.common.file.core.FileStorageProperties;
import com.nhb.common.file.platform.FileStorageClientFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 阿里云 OSS 存储平台的 Client 工厂
 */
@Getter
@Setter
@NoArgsConstructor
public class AliyunOssFileStorageClientFactory implements FileStorageClientFactory<OSS> {
    private String platform;
    private String accessKey;
    private String secretKey;
    private String endPoint;
    private volatile OSS client;

    public AliyunOssFileStorageClientFactory(FileStorageProperties.AliyunOssConfig config) {
        platform = config.getPlatform();
        accessKey = config.getAccessKey();
        secretKey = config.getSecretKey();
        endPoint = config.getEndPoint();
    }

    @Override
    public OSS getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = new OSSClientBuilder().build(endPoint, accessKey, secretKey);
                }
            }
        }
        return client;
    }

    @Override
    public void close() {
        if (client != null) {
            client.shutdown();
            client = null;
        }
    }
}
