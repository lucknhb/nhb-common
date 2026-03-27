package com.nhb.common.generator.config;

import com.nhb.common.core.factory.YamlPropertySourceFactory;
import com.nhb.common.generator.core.MybatisTableEntityGenerator;
import com.nhb.common.generator.monitor.MyBatisDataSourceMonitor;
import com.nhb.common.generator.properties.GeneratorConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/27 14:13
 * @description:
 */
@AutoConfiguration
@EnableConfigurationProperties(GeneratorConfigProperties.class)
@PropertySource(value = "classpath:generator-default.yaml", factory = YamlPropertySourceFactory.class)
public class GeneratorAutoConfiguration {

    @Bean
    @ConditionalOnBooleanProperty(prefix = GeneratorConfigProperties.PREFIX, name = "tableConfig.enabled", matchIfMissing = true)
    public MyBatisDataSourceMonitor myBatisDataSourceMonitor() {
        return new MyBatisDataSourceMonitor();
    }

    @Bean
    @ConditionalOnBooleanProperty(prefix = GeneratorConfigProperties.PREFIX, name = "tableConfig.enabled", matchIfMissing = true)
    public MybatisTableEntityGenerator mybatisTableEntityGenerator() {
        return new MybatisTableEntityGenerator();
    }
}
