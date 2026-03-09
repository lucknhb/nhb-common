package com.nhb.common.file.platform.factory;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.file.datalake.DataLakeServiceClient;
import com.azure.storage.file.datalake.DataLakeServiceClientBuilder;
import com.nhb.common.file.core.FileStorageProperties;
import com.nhb.common.file.platform.FileStorageClientFactory;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
public class AzureBlobStorageFileStorageClientFactory implements FileStorageClientFactory<AzureBlobStorageFileStorageClientFactory.AzureBlobStorageClient> {

    private FileStorageProperties.AzureBlobStorageConfig config;
    private volatile AzureBlobStorageClient client;

    public AzureBlobStorageFileStorageClientFactory(FileStorageProperties.AzureBlobStorageConfig config) {
        this.config = config;
    }

    @Override
    public String getPlatform() {
        return config.getPlatform();
    }

    @Override
    public AzureBlobStorageClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = new AzureBlobStorageClient(config);
                }
            }
        }
        return client;
    }

    @Override
    public void close() {
        client = null;
    }

    @Getter
    @Setter
    public static final class AzureBlobStorageClient {
        private FileStorageProperties.AzureBlobStorageConfig config;
        private volatile BlobServiceClient blobServiceClient;
        private volatile DataLakeServiceClient dataLakeServiceClient;

        public AzureBlobStorageClient(FileStorageProperties.AzureBlobStorageConfig config) {
            this.config = config;
        }

        public BlobServiceClient getBlobServiceClient() {
            if (blobServiceClient == null) {
                synchronized (this) {
                    if (blobServiceClient == null) {
                        blobServiceClient = new BlobServiceClientBuilder()
                                .endpoint(config.getEndPoint())
                                .connectionString(config.getConnectionString())
                                .buildClient();
                    }
                }
            }
            return blobServiceClient;
        }

        public DataLakeServiceClient getDataLakeServiceClient() {
            if (dataLakeServiceClient == null) {
                synchronized (this) {
                    if (dataLakeServiceClient == null) {
                        dataLakeServiceClient = new DataLakeServiceClientBuilder()
                                .endpoint(config.getEndPoint())
                                .connectionString(config.getConnectionString())
                                .buildClient();
                    }
                }
            }
            return dataLakeServiceClient;
        }
    }
}
