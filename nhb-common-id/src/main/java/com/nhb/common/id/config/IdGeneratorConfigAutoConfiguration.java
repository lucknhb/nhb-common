package com.nhb.common.id.config;

import com.nhb.common.core.factory.YamlPropertySourceFactory;
import com.nhb.common.id.constants.MachineIdGeneratorType;
import com.nhb.common.id.core.WorkerNodeRepository;
import com.nhb.common.id.properties.IdGeneratorConfigProperties;
import com.nhb.common.id.service.*;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/18 11:44
 * @description: 雪花算法自动配置<BR />
 * 以下为雪花算法图示<BR/>
 * | 1 bit (符号位) | 41 bit (时间戳) | 10 bit (机器ID) | 12 bit (序列号) |
 * |      0        |  毫秒级时间差    |  节点编号       |  同一毫秒内序号  |
 */
@AutoConfiguration
@PropertySource(value = "classpath:id-generator-default.yaml",factory = YamlPropertySourceFactory.class)
@EnableConfigurationProperties(IdGeneratorConfigProperties.class)
public class IdGeneratorConfigAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "id-generator.machine-id-generator-type",havingValue = MachineIdGeneratorType.POD)
    public PodWorkerIdNodeRepository podWorkerIdNodeRepository() {
        return new PodWorkerIdNodeRepository();
    }

    @Configuration
    @ConditionalOnClass(JdbcTemplate.class)
    @ConditionalOnProperty(name = "id-generator.machine-id-generator-type", havingValue = MachineIdGeneratorType.DB)
    static class DatabaseConfig {

        @Bean
        @ConditionalOnBean(DataSource.class)
        public JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean(initMethod = "init")
        @ConditionalOnBean(DataSource.class)
        public DbWorkerNodeRepository dbWorkerNodeRepository(JdbcTemplate jdbcTemplate, DataSource dataSource) {
            return new DbWorkerNodeRepository(jdbcTemplate, dataSource);
        }
    }

    @Bean(initMethod = "startHeartbeat")
    @ConditionalOnProperty(name = "id-generator.machine-id-generator-type",havingValue = MachineIdGeneratorType.REDIS)
    @ConditionalOnClass(RedissonClient.class)
    public RedisWorkerNodeRepository redisWorkerNodeRepository(RedissonClient redissonClient) {
        return new RedisWorkerNodeRepository(redissonClient);
    }

    @Bean
    public DisposableWorkerIdAssigner disposableWorkerIdAssigner(WorkerNodeRepository workerNodeRepository) {
        return new DisposableWorkerIdAssigner(workerNodeRepository);
    }

    @Bean
    public CachedIdGenerator cachedIdGenerator(DisposableWorkerIdAssigner disposableWorkerIdAssigner, IdGeneratorConfigProperties idGeneratorConfigProperties) {
        return new CachedIdGenerator(disposableWorkerIdAssigner, idGeneratorConfigProperties);
    }


}
