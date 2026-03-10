package com.nhb.common.file.platform.factory;

import cn.hutool.core.util.URLUtil;
import com.github.sardine.Sardine;
import com.github.sardine.SardineFactory;
import com.nhb.common.file.core.FileStorageProperties;
import com.nhb.common.file.exception.FileStorageException;
import com.nhb.common.file.platform.FileStorageClientFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/9 15:01
 * @description: WebDAV 存储平台的 Client 工厂
 */
@Getter
@Setter
@NoArgsConstructor
public class WebDavFileStorageClientFactory implements FileStorageClientFactory<Sardine> {
    private String platform;
    private String server;
    private String user;
    private String password;
    private volatile Sardine client;

    public WebDavFileStorageClientFactory(FileStorageProperties.WebDavConfig config) {
        platform = config.getPlatform();
        server = config.getServer();
        user = config.getUser();
        password = config.getPassword();
    }

    @Override
    public Sardine getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    client = SardineFactory.begin(user, password);
                    client.enablePreemptiveAuthentication(URLUtil.url(server));
                }
            }
        }
        return client;
    }

    @Override
    public void close() {
        if (client != null) {
            try {
                client.shutdown();
            } catch (IOException e) {
                throw new FileStorageException("关闭 WebDAV Client 失败！", e);
            }
            client = null;
        }
    }
}
