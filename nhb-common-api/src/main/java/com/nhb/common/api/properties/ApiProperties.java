package com.nhb.common.api.properties;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/1 22:55
 * @description api 文档配置
 */
@Data
@ConfigurationProperties(prefix = ApiProperties.PREFIX)
public class ApiProperties {
    /**
     * 兼顾 以下配置项
     *
     * @see org.springdoc.core.properties.SpringDocConfigProperties
     */
    public static final String PREFIX = "springdoc";
    /**
     * 文档基本信息
     */
    @NestedConfigurationProperty
    private InfoProperties info = new InfoProperties();

    /**
     * 扩展文档地址
     */
    @NestedConfigurationProperty
    private ExternalDocumentation externalDocs;

    /**
     * 标签
     */
    private List<Tag> tags = null;

    /**
     * 路径
     */
    @NestedConfigurationProperty
    private Paths paths = null;

    /**
     * 组件
     */
    @NestedConfigurationProperty
    private Components components = null;

    /**
     * <p>
     * 文档的基础属性信息
     * </p>
     *
     * @see io.swagger.v3.oas.models.info.Info
     * <p>
     * 为了 springboot 自动生产配置提示信息，所以这里复制一个类出来
     */
    @Data
    public static class InfoProperties {

        /**
         * 标题
         */
        private String title = null;

        /**
         * 描述
         */
        private String description = null;

        /**
         * 联系人信息
         */
        @NestedConfigurationProperty
        private Contact contact = null;

        /**
         * 许可证
         */
        @NestedConfigurationProperty
        private License license = null;

        /**
         * 版本
         */
        private String version = null;

    }

}
