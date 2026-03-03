package com.nhb.common.ip2region.config;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.nhb.common.core.exception.ServiceException;
import com.nhb.common.ip2region.properties.Ip2RegionConfigProperties;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.ConfigBuilder;
import org.lionsoul.ip2region.service.Ip2Region;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.InputStream;
import java.util.Objects;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/25 11:31
 * @description: IP转换为对应地址自动配置类
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(Ip2RegionConfigProperties.class)
@ConditionalOnBooleanProperty(prefix = Ip2RegionConfigProperties.PREFIX, value = "enabled", havingValue = true, matchIfMissing = true)
public class Ip2RegionAutoConfiguration {

    private Ip2Region ip2Region;

    /**
     * 支持IPV4/IPV6的地区查询
     * 使用方式例如: ip2Region.search()  返回的是
     *
     * @param ip2RegionConfigProperties iP2Region配置类
     * @return Ip2Region对象用于查询
     */
    @Bean
    public Ip2Region ip2Region(Ip2RegionConfigProperties ip2RegionConfigProperties) {
        try {
            final int DEFAULT_CACHE_SLICE_BYTES = 1024 * 1024 * ip2RegionConfigProperties.getCacheSliceSize();
            //IPV4相关配置
            ConfigBuilder v4ConfigBuilder = Config.custom()
                    .setSearchers(15)                       // 设置初始化的查询器数量
                    .setCacheSliceBytes(DEFAULT_CACHE_SLICE_BYTES);// 设置缓存的分片字节数
            Config v4Config = null;
            //IPV6相关配置
            ConfigBuilder v6ConfigBuilder = Config.custom()
                    .setSearchers(15)                       // 设置初始化的查询器数量
                    .setCacheSliceBytes(DEFAULT_CACHE_SLICE_BYTES);// 设置缓存的分片字节数
            Config v6Config = null;
            //先判断缓存类型是什么？
            if (Config.VIndexCache == ip2RegionConfigProperties.getCacheType()) {
                //判断资源文件是否存在
                if (FileUtil.exist(ip2RegionConfigProperties.getV4DbPath())) {
                    throw new ServiceException("Not fount ipv4'xdb:{} file.Please check the file is exist?", ip2RegionConfigProperties.getV4DbPath());
                }
                v4Config = v4ConfigBuilder
                        .setCachePolicy(Config.VIndexCache)
                        .setXdbPath(ip2RegionConfigProperties.getV4DbPath()) // 设置 v6 xdb 文件的路径
                        .asV4();
                if (FileUtil.exist(ip2RegionConfigProperties.getV6DbPath())) {
                    log.warn("Not fount ipv6'xdb:{} file.Please check the file is exist?", ip2RegionConfigProperties.getV6DbPath());
                } else {
                    v6Config = v6ConfigBuilder
                            .setCachePolicy(Config.VIndexCache)
                            .setXdbPath(ip2RegionConfigProperties.getV6DbPath())
                            .asV6();
                }
            } else {//使用全缓存模式兜底(默认模式)
                InputStream v4DbInputStream = ResourceUtil.getStreamSafe(ip2RegionConfigProperties.getV4DbPath());
                if (Objects.isNull(v4DbInputStream)) {
                    throw new ServiceException("Not fount ipv4'xdb:{} file at jar.Please check the file is exist?", ip2RegionConfigProperties.getV4DbPath());
                }
                v4Config = v4ConfigBuilder
                        .setCachePolicy(Config.BufferCache)
                        .setXdbInputStream(v4DbInputStream) // 设置 v4 xdb 文件的 inputStream 对象
                        .asV4();
                InputStream v6DbInputStream = ResourceUtil.getStreamSafe(ip2RegionConfigProperties.getV6DbPath());
                if (Objects.isNull(v6DbInputStream)) {
                    log.warn("Not load IPv6'xdb resource：not found the file {} . If you should use for ipv6，please save ip2region_v6.xdb to resources", ip2RegionConfigProperties.getV6DbPath());
                } else {
                    v6Config = Config.custom()
                            .setCachePolicy(Config.BufferCache)
                            .setXdbInputStream(v6DbInputStream)
                            .asV6();
                }
            }
            //通过上述配置创建 Ip2Region 查询服务
            ip2Region = Ip2Region.create(v4Config, v6Config);
            return ip2Region;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServiceException("Config ip2Region is fail.Please check detail error message", e);
        }
    }

    /**
     * 关闭Ip2Region服务
     */
    @PreDestroy
    public void close() {
        if (Objects.nonNull(ip2Region)) {
            try {
                ip2Region.close(10000);
            } catch (Exception e) {
                log.error("Ip2Region close error", e);
            }
        }
    }
}
