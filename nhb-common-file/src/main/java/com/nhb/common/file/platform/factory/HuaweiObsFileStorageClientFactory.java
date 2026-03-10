package com.nhb.common.file.platform.factory;

import cn.hutool.core.io.IoUtil;
import com.nhb.common.file.core.FileStorageProperties;
import com.nhb.common.file.platform.FileStorageClientFactory;
import com.obs.services.ObsClient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: 华为云 ObsClient 存储平台的 Client 工厂
 */
@Getter
@Setter
@NoArgsConstructor
public class HuaweiObsFileStorageClientFactory implements FileStorageClientFactory<ObsClient> {
    private String platform;
    private String accessKey;
    private String secretKey;
    private String endPoint;
    private volatile ObsClient client;

    public HuaweiObsFileStorageClientFactory(FileStorageProperties.HuaweiObsConfig config) {
        platform = config.getPlatform();
        accessKey = config.getAccessKey();
        secretKey = config.getSecretKey();
        endPoint = config.getEndPoint();
    }

    @Override
    public ObsClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = new ObsClient(accessKey, secretKey, endPoint);
                }
            }
        }
        return client;
    }

    @Override
    public void close() {
        IoUtil.close(client);
        client = null;
    }
}
