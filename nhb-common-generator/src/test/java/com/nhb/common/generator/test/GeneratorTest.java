package com.nhb.common.generator.test;

import com.nhb.common.core.factory.YamlPropertySourceFactory;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.generator.core.MapStructPlusGenerator;
import com.nhb.common.generator.core.MybatisEntityTableGenerator;
import com.nhb.common.generator.core.MybatisTableEntityGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/27 15:47
 * @description: 增强的生成器元数据测试类
 */
@SpringBootTest
public class GeneratorTest {

    /**
     * 内嵌启动类，用于启动 Spring Boot 上下文
     */
    @SpringBootApplication
    @PropertySource(value = "classpath:test.yaml",factory = YamlPropertySourceFactory.class)
    public static class TestApplication {
        // 空类即可，用于触发自动配置扫描
    }

    @Test
    public void testMybatisTableEntityGenerator() {
        MybatisTableEntityGenerator mybatisTableEntityGenerator = SpringContextUtil.getBean(MybatisTableEntityGenerator.class);
        mybatisTableEntityGenerator.generate(List.of("config_info"));
    }

    @Test
    public void testMybatisEntityTableGenerator() {
        MybatisEntityTableGenerator mybatisEntityTableGenerator = SpringContextUtil.getBean(MybatisEntityTableGenerator.class);
        mybatisEntityTableGenerator.generate(null);
    }

    @Test
    public void testMapStructPlusGenerator(){
        MapStructPlusGenerator mapStructPlusGenerator = SpringContextUtil.getBean(MapStructPlusGenerator.class);
        mapStructPlusGenerator.generate(null);
    }
}
