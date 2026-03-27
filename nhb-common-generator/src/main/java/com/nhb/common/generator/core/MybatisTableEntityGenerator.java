package com.nhb.common.generator.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.anyline.metadata.Table;
import org.anyline.proxy.ServiceProxy;

import java.util.LinkedHashMap;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/27 16:15
 * @description: 生成mybatis对应的文件
 */
@Slf4j
@RequiredArgsConstructor
public class MybatisTableEntityGenerator implements TableEntityGenerator {

    /**
     * 生成对应的模板
     */
    @Override
    public void generate() {
        LinkedHashMap<String, Table<?>> tablesMap = ServiceProxy.metadata().tables();



    }
}
