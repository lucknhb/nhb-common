package com.nhb.common.mybatis.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.core.handlers.PostInitTableInfoHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.nhb.common.core.utils.ReflectSelfUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.mybatis.aspectj.DataPermissionPointcutAdvisorAspect;
import com.nhb.common.mybatis.core.FieldEncryptorManager;
import com.nhb.common.mybatis.core.TenantSaTokenDao;
import com.nhb.common.mybatis.handler.*;
import com.nhb.common.mybatis.interceptor.DataPermissionInterceptor;
import com.nhb.common.mybatis.interceptor.MybatisPlusDecryptInterceptor;
import com.nhb.common.mybatis.interceptor.MybatisPlusEncryptInterceptor;
import com.nhb.common.mybatis.manager.TenantSpringCacheManager;
import com.nhb.common.mybatis.properties.FieldEncryptorConfigProperties;
import com.nhb.common.mybatis.properties.TenantConfigProperties;
import com.nhb.common.redis.config.RedissonAutoConfiguration;
import com.nhb.common.redis.properties.RedissonConfigProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.SingleServerConfig;
import org.redisson.spring.starter.RedissonAutoConfigurationCustomizer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/19 16:27
 * @description:
 */
@AutoConfigureAfter(RedissonAutoConfiguration.class)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@EnableTransactionManagement(proxyTargetClass = true)
@MapperScan("${mybatis-plus.mapperPackage}")
@EnableConfigurationProperties({FieldEncryptorConfigProperties.class, TenantConfigProperties.class, MybatisPlusProperties.class})
public class MyBatisPlusAutoConfiguration {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 多租户插件 必须放到第一位
        try {
            TenantLineInnerInterceptor tenant = SpringContextUtil.getBean(TenantLineInnerInterceptor.class);
            interceptor.addInnerInterceptor(tenant);
        } catch (BeansException ignore) {
        }
        // 数据权限处理
        interceptor.addInnerInterceptor(dataPermissionInterceptor());
        // 分页插件
        interceptor.addInnerInterceptor(paginationInnerInterceptor());
        // 乐观锁插件
        interceptor.addInnerInterceptor(optimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * 数据权限拦截器
     */
    public DataPermissionInterceptor dataPermissionInterceptor() {
        return new DataPermissionInterceptor();
    }

    /**
     * 数据权限切面处理器
     */
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public DataPermissionPointcutAdvisorAspect dataPermissionPointcutAdvisor() {
        return new DataPermissionPointcutAdvisorAspect();
    }

    /**
     * 分页插件，自动识别数据库类型
     */
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        // 分页合理化
        paginationInnerInterceptor.setOverflow(true);
        return paginationInnerInterceptor;
    }

    /**
     * 乐观锁插件
     */
    public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
        return new OptimisticLockerInnerInterceptor();
    }

    /**
     * 元对象字段填充控制器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new InjectionMetaObjectHandler();
    }

    /**
     * 异常处理器
     */
    @Bean
    public MybatisPlusExceptionHandler mybatisPlusExceptionHandler() {
        return new MybatisPlusExceptionHandler();
    }

    /**
     * 初始化表对象处理器
     */
    @Bean
    public PostInitTableInfoHandler postInitTableInfoHandler() {
        return new PostInitTableInfoPlusHandler();
    }

    @Bean
    @ConditionalOnBooleanProperty(prefix = FieldEncryptorConfigProperties.PREFIX, name = "enabled", matchIfMissing = true)
    public FieldEncryptorManager fieldEncryptorManager(MybatisPlusProperties mybatisPlusProperties) {
        return new FieldEncryptorManager(mybatisPlusProperties.getTypeAliasesPackage());
    }

    @Bean
    @ConditionalOnBooleanProperty(prefix = FieldEncryptorConfigProperties.PREFIX, name = "enabled", matchIfMissing = true)
    public MybatisPlusEncryptInterceptor mybatisEncryptInterceptor(FieldEncryptorManager fieldEncryptorManager,
                                                                   FieldEncryptorConfigProperties fieldEncryptorConfigProperties) {
        return new MybatisPlusEncryptInterceptor(fieldEncryptorManager, fieldEncryptorConfigProperties);
    }

    @Bean
    @ConditionalOnBooleanProperty(prefix = FieldEncryptorConfigProperties.PREFIX, name = "enabled", matchIfMissing = true)
    public MybatisPlusDecryptInterceptor mybatisDecryptInterceptor(FieldEncryptorManager fieldEncryptorManager,
                                                                   FieldEncryptorConfigProperties fieldEncryptorConfigProperties) {
        return new MybatisPlusDecryptInterceptor(fieldEncryptorManager, fieldEncryptorConfigProperties);
    }


    @AutoConfiguration
    @ConditionalOnClass(TenantLineInnerInterceptor.class)
    @ConditionalOnBooleanProperty(prefix = TenantConfigProperties.PREFIX, name = "enabled", matchIfMissing = true)
    static class MybatisPlusConfig {
        /**
         * 多租户插件
         */
        @Bean
        public TenantLineInnerInterceptor tenantLineInnerInterceptor(TenantConfigProperties tenantConfigProperties) {
            return new TenantLineInnerInterceptor(new TenantLinePlusHandler(tenantConfigProperties));
        }

    }

    @Bean
    @ConditionalOnBooleanProperty(prefix = TenantConfigProperties.PREFIX, name = "enabled", matchIfMissing = true)
    public RedissonAutoConfigurationCustomizer tenantRedissonCustomizer(RedissonConfigProperties redissonConfigProperties) {
        return config -> {
            TenantKeyPrefixHandler nameMapper = new TenantKeyPrefixHandler(redissonConfigProperties.getKeyPrefix());
            SingleServerConfig singleServerConfig = ReflectSelfUtil.invokeGetter(config, "singleServerConfig");
            if (ObjectUtil.isNotNull(singleServerConfig)) {
                // 使用单机模式
                // 设置多租户 redis key前缀
                singleServerConfig.setNameMapper(nameMapper);
            }
            ClusterServersConfig clusterServersConfig = ReflectSelfUtil.invokeGetter(config, "clusterServersConfig");
            // 集群配置方式 参考下方注释
            if (ObjectUtil.isNotNull(clusterServersConfig)) {
                // 设置多租户 redis key前缀
                clusterServersConfig.setNameMapper(nameMapper);
            }
        };
    }

    /**
     * 多租户缓存管理器
     */
    @Bean
    @Primary
    @ConditionalOnBooleanProperty(prefix = TenantConfigProperties.PREFIX, name = "enabled", matchIfMissing = true)
    public CacheManager tenantCacheManager() {
        return new TenantSpringCacheManager();
    }

    /**
     * 多租户鉴权dao实现
     */
    @Bean
    @Primary
    @ConditionalOnBooleanProperty(prefix = TenantConfigProperties.PREFIX, name = "enabled", matchIfMissing = true)
    public SaTokenDao tenantSaTokenDao() {
        return new TenantSaTokenDao();
    }


}
