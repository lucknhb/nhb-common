package com.nhb.common.file.platform.factory;

import com.nhb.common.file.core.FileStorageProperties;
import com.nhb.common.file.platform.FileStorageClientFactory;
import io.minio.MinioClient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: MinIO 存储平台的 Client 工厂
 */
@Getter
@Setter
@NoArgsConstructor
public class MinioFileStorageClientFactory implements FileStorageClientFactory<MinioClient> {
    private String platform;
    private String accessKey;
    private String secretKey;
    private String endPoint;
    private volatile MinioClient client;

    public MinioFileStorageClientFactory(FileStorageProperties.MinioConfig config) {
        platform = config.getPlatform();
        accessKey = config.getAccessKey();
        secretKey = config.getSecretKey();
        endPoint = config.getEndPoint();
    }

    @Override
    public MinioClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = new MinioClient.Builder()
                            .credentials(accessKey, secretKey)
                            .endpoint(endPoint)
                            .build();
                }
            }
        }
        return client;
    }

    @Override
    public void close() {
        client = null;
    }
}
