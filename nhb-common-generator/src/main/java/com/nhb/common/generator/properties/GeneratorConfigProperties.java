package com.nhb.common.generator.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/27 14:11
 * @description:
 */
@Data
@ConfigurationProperties(prefix = GeneratorConfigProperties.PREFIX)
public class GeneratorConfigProperties {
    public static final String PREFIX = "spring.datasource.generator";

    private TableConfigProperties tableConfig;


    /**
     * 生成数据库表对应的实体以及Mapper以及VO
     */
    @Data
    public static class TableConfigProperties {
        private Boolean enabled = false;
        /**
         * 作者
         */
        private String author;
        /**
         * 生成包路径
         */
        private String packageName;
        /**
         * 表前缀(类名不会包含表前缀)
         */
        private String tablePrefix;
    }

}
