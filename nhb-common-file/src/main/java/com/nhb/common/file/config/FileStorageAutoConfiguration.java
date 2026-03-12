package com.nhb.common.file.config;

import com.nhb.common.file.adapter.FileWrapperAdapter;
import com.nhb.common.file.adapter.MultipartFileWrapperAdapter;
import com.nhb.common.file.aspect.FileStorageAspect;
import com.nhb.common.file.core.ContentTypeDetect;
import com.nhb.common.file.core.FileStorageService;
import com.nhb.common.file.core.FileStorageServiceBuilder;
import com.nhb.common.file.platform.FileStorage;
import com.nhb.common.file.platform.FileStorageClientFactory;
import com.nhb.common.file.properties.FileStorageConfigProperties;
import com.nhb.common.file.recorder.DefaultFileRecorder;
import com.nhb.common.file.recorder.FileRecorder;
import com.nhb.common.file.tika.DefaultTikaFactory;
import com.nhb.common.file.tika.TikaContentTypeDetect;
import com.nhb.common.file.tika.TikaFactory;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.nhb.common.file.core.FileStorageServiceBuilder.doesNotExistClass;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/10 16:09
 * @description
 */
@Slf4j
@AutoConfiguration
@ConditionalOnMissingBean(FileStorageService.class)
@EnableConfigurationProperties(FileStorageConfigProperties.class)
public class FileStorageAutoConfiguration {
    @Resource
    private ApplicationContext applicationContext;

    /**
     * 当没有找到 FileRecorder 时使用默认的 FileRecorder
     */
    @Bean
    @ConditionalOnMissingBean(FileRecorder.class)
    public FileRecorder fileRecorder() {
        log.warn("没有找到 FileRecorder 的实现类，文件上传之外的部分功能无法正常使用，必须实现该接口才能使用完整功能！");
        return new DefaultFileRecorder();
    }

    /**
     * Tika 工厂类型，用于识别上传的文件的 MINE
     */
    @Bean
    @ConditionalOnMissingBean(TikaFactory.class)
    public TikaFactory tikaFactory() {
        return new DefaultTikaFactory();
    }

    /**
     * 识别文件的 MIME 类型
     */
    @Bean
    @ConditionalOnMissingBean(ContentTypeDetect.class)
    public ContentTypeDetect contentTypeDetect(TikaFactory tikaFactory) {
        return new TikaContentTypeDetect(tikaFactory);
    }

    /**
     * 文件存储服务
     */
    @Bean(destroyMethod = "destroy")
    public FileStorageService fileStorageService(
            FileRecorder fileRecorder,
            ContentTypeDetect contentTypeDetect,
            FileStorageConfigProperties fileStorageConfigProperties,
            @Autowired(required = false) List<List<? extends FileStorage>> fileStorageLists,
            @Autowired(required = false) List<FileStorageAspect> aspectList,
            @Autowired(required = false) List<FileWrapperAdapter> fileWrapperAdapterList,
            @Autowired(required = false) List<List<FileStorageClientFactory<?>>> clientFactoryList) {

        if (fileStorageLists == null) {
            fileStorageLists = new ArrayList<>();
        }
        if (aspectList == null) {
            aspectList = new ArrayList<>();
        }
        if (fileWrapperAdapterList == null) {
            fileWrapperAdapterList = new ArrayList<>();
        }
        if (clientFactoryList == null) {
            clientFactoryList = new ArrayList<>();
        }

        FileStorageServiceBuilder builder = FileStorageServiceBuilder.create(fileStorageConfigProperties.toFileStorageProperties())
                .setFileRecorder(fileRecorder)
                .setAspectList(aspectList)
                .setContentTypeDetect(contentTypeDetect)
                .setFileWrapperAdapterList(fileWrapperAdapterList)
                .setClientFactoryList(clientFactoryList);

        fileStorageLists.forEach(builder::addFileStorage);

        if (fileStorageConfigProperties.getEnableByteFileWrapper()) {
            builder.addByteFileWrapperAdapter();
        }
        if (fileStorageConfigProperties.getEnableUriFileWrapper()) {
            builder.addUriFileWrapperAdapter();
        }
        if (fileStorageConfigProperties.getEnableInputStreamFileWrapper()) {
            builder.addInputStreamFileWrapperAdapter();
        }
        if (fileStorageConfigProperties.getEnableLocalFileWrapper()) {
            builder.addLocalFileWrapperAdapter();
        }
        if (fileStorageConfigProperties.getEnableHttpServletRequestFileWrapper()) {
            if (doesNotExistClass("jakarta.servlet.http.HttpServletRequest")) {
                log.warn("当前未检测到 Servlet 环境，无法加载 HttpServletRequest 的文件包装适配器，请将参数【enableHttpServletRequestFileWrapper】设置为 【false】来消除此警告");
            } else {
                builder.addHttpServletRequestFileWrapperAdapter();
            }
        }
        if (fileStorageConfigProperties.getEnableMultipartFileWrapper()) {
            if (doesNotExistClass("org.springframework.web.multipart.MultipartFile")) {
                log.warn(
                        "当前未检测到 SpringWeb 环境，无法加载 MultipartFile 的文件包装适配器，请将参数【enableMultipartFileWrapper】设置为 【false】来消除此警告");
            } else {
                builder.addFileWrapperAdapter(new MultipartFileWrapperAdapter());
            }
        }

        if (doesNotExistClass("org.springframework.web.servlet.config.annotation.WebMvcConfigurer")) {
            long localAccessNum = fileStorageConfigProperties.getLocal().stream()
                    .filter(FileStorageConfigProperties.LocalConfigProperties::getEnableStorage)
                    .filter(FileStorageConfigProperties.LocalConfigProperties::getEnableAccess)
                    .count();
            long localPlusAccessNum = fileStorageConfigProperties.getLocal().stream()
                    .filter(FileStorageConfigProperties.LocalConfigProperties::getEnableStorage)
                    .filter(FileStorageConfigProperties.LocalConfigProperties::getEnableAccess)
                    .count();

            if (localAccessNum + localPlusAccessNum > 0) {
                log.warn("当前未检测到 SpringWeb 环境，无法开启本地存储平台的本地访问功能，请将关闭本地访问来消除此警告");
            }
        }

        return builder.build();
    }

    /**
     * 对 FileStorageService 注入自己的代理对象，不然会导致针对 FileStorageService 的代理方法不生效
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onContextRefreshedEvent() {
        FileStorageService service = applicationContext.getBean(FileStorageService.class);
        service.setSelf(service);
    }

    /**
     * 本地存储文件访问自动配置类
     */
    @Configuration
    @ConditionalOnClass(name = "org.springframework.web.servlet.config.annotation.WebMvcConfigurer")
    public static class FileStorageLocalFileAccessAutoConfiguration {
        @Resource
        private FileStorageConfigProperties properties;

        /**
         * 配置本地存储的访问地址
         */
        @Bean
        public WebMvcConfigurer fileStorageWebMvcConfigurer() {
            return new WebMvcConfigurer() {
                @Override
                public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
                    for (FileStorageConfigProperties.LocalConfigProperties local : properties.getLocal()) {
                        if (local.getEnableStorage() && local.getEnableAccess()) {
                            registry.addResourceHandler(local.getPathPatterns())
                                    .addResourceLocations("file:" + local.getBasePath());
                        }
                    }
                    for (FileStorageConfigProperties.LocalConfigProperties local : properties.getLocal()) {
                        if (local.getEnableStorage() && local.getEnableAccess()) {
                            registry.addResourceHandler(local.getPathPatterns())
                                    .addResourceLocations("file:" + local.getStoragePath());
                        }
                    }
                }
            };
        }
    }

}
