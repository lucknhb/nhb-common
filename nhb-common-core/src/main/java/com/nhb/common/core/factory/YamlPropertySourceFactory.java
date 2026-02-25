package com.nhb.common.core.factory;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;
import java.util.Properties;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/2/25 8:45
 * @description: yaml类型配置文件加载转换为sping环境中可通过@value取值类型
 */
@Slf4j
public class YamlPropertySourceFactory extends DefaultPropertySourceFactory {
    /**
     * 将yaml/yml类型配置文件转换为properties类型且加载到Spring环境中
     * 使用方式例如： @PropertySource(value = "classpath:XXXX.yml/yaml", factory = YamlPropertySourceFactory.class)
     * @param name
     * @param resource
     * @return
     * @throws IOException
     */
    @Override
    public PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        String sourceName = resource.getResource().getFilename();
        if (StringUtils.isNotBlank(sourceName) && StringUtils.endsWithAny(sourceName, ".yml", ".yaml")) {
            YamlPropertiesFactoryBean factory = new YamlPropertiesFactoryBean();
            factory.setResources(resource.getResource());
            factory.afterPropertiesSet();
            Properties properties = factory.getObject();
            return new PropertiesPropertySource(sourceName, properties);
        }
        return super.createPropertySource(name, resource);
    }
}
