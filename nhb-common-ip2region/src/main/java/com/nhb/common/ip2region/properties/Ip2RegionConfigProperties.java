package com.nhb.common.ip2region.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/25 13:31
 * @description:
 */
@Getter
@Setter
@RefreshScope
@ConfigurationProperties(prefix = Ip2RegionConfigProperties.PREFIX)
public class Ip2RegionConfigProperties {
    public static final String PREFIX = "ip2region";
    /**
     * 是否使用该服务 默认为true
     */
    private boolean enabled = true;
    /**
     * 1: VIndexCache（按需读取并缓存）
     * 2: BufferCache（全量读取xdb到内存中）
     * 默认为2 可直接从jar包中获取资源文件 但弊端是耗内存
     * 如果资源文件希望从外部读取的话可配置为1
     */
    private int cacheType = 2;
    /**
     * 分片缓存大小 默认15M
     */
    private int cacheSliceSize = 15;
    /**
     * IPV4的地区解析库
     * 当前配置是按照全量缓存配置(从jar中获取资源文件)
     * 若使用按需读取则需配置实际的外部资源文件
     */
    private String v4DbPath = "ip2region_v4.xdb";
    /**
     * IPV6的地区解析库
     * 当前配置是按照全量缓存配置(从jar中获取资源文件)
     * 若使用按需读取则需配置实际的外部资源文件
     */
    private String v6DbPath =  "ip2region_v6.xdb";
}
