package com.nhb.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ResourceUtils;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/6 9:53
 * @description: spring环境下 获取文件工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceFileUtil extends ResourceUtils {

    /**
     * 资源解析器.
     */
    private static final ResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();

    /**
     * 根据路径获取资源.
     * @param location 路径
     * @return 资源
     */
    public static Resource getResource(String location) {
        return RESOLVER.getResource(location);
    }

}
