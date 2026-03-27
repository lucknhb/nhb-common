package com.nhb.common.generator.test;

import org.anyline.metadata.Column;
import org.anyline.metadata.PrimaryKey;
import org.anyline.metadata.Table;
import org.anyline.proxy.ServiceProxy;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
    public static class TestApplication {
        // 空类即可，用于触发自动配置扫描
    }

    /**
     * 测试获取所有表的基本信息
     */
    @Test
    public void testGetAllTables() {
        LinkedHashMap<String, Table<?>> tablesMap = ServiceProxy.metadata().tables();

        // 验证返回结果不为null
        assertNotNull(tablesMap, "表映射不应该为空");

        // 输出表的数量
        System.out.println("发现表数量: " + tablesMap.size());

        // 遍历并打印每个表的信息
        for (Map.Entry<String, Table<?>> entry : tablesMap.entrySet()) {
            String tableName = entry.getKey();
            Table<?> table = entry.getValue();

            System.out.println("表名: " + tableName);
            System.out.println("  - 表注释: " + table.getComment());
            System.out.println("  - 表类型: " + table.getType());
            System.out.println("  - 列数量: " + table.getColumns().size());

            // 打印前几个列的信息（如果有的话）
            LinkedHashMap<String, Column> columns = table.getColumns();
            columns.forEach((key, value) -> {
                System.out.println("    - 列名: " + key +
                        ", 类型: " + value.getTypeName() +
                        ", 注释: " + value.getComment());
            });

            // 检查主键信息
            PrimaryKey primaryKey = table.getPrimaryKey();
            if (primaryKey != null) {
                System.out.println("  - 主键: " + primaryKey.getName());
            } else {
                System.out.println("  - 主键: 无");
            }

            System.out.println("---");
        }
    }

    /**
     * 测试获取特定表
     */
    @Test
    public void testGetSpecificTable() {
        LinkedHashMap<String, Table<?>> tablesMap = ServiceProxy.metadata().tables();

        if (!tablesMap.isEmpty()) {
            // 获取第一个表进行详细测试
            Map.Entry<String, Table<?>> firstEntry = tablesMap.entrySet().iterator().next();
            String tableName = firstEntry.getKey();
            Table<?> table = firstEntry.getValue();

            assertNotNull(tableName, "表名不应为空");
            assertNotNull(table, "表对象不应为空");

            System.out.println("测试表: " + tableName);
            System.out.println("表注释: " + table.getComment());
            System.out.println("列总数: " + table.getColumns().size());

            // 验证表的基本属性
            assertFalse(table.getName().isEmpty(), "表名不应为空字符串");
        } else {
            System.out.println("没有找到任何表");
        }
    }

    /**
     * 测试表映射的基本属性
     */
    @Test
    public void testTablesMapProperties() {
        LinkedHashMap<String, Table<?>> tablesMap = ServiceProxy.metadata().tables();

        // 验证返回的是LinkedHashMap类型，保持插入顺序
        assertTrue(tablesMap instanceof LinkedHashMap, "应该返回LinkedHashMap实例");

        // 验证基本集合操作
        int size = tablesMap.size();
        System.out.println("表映射大小: " + size);

        if (size > 0) {
            // 测试containsKey方法
            String firstKey = tablesMap.keySet().iterator().next();
            assertTrue(tablesMap.containsKey(firstKey), "应该包含第一个键");

            // 测试get方法
            Table<?> table = tablesMap.get(firstKey);
            assertNotNull(table, "通过键获取的值不应为空");
        }
    }

    /**
     * 测试元数据查询性能
     */
    @Test
    public void testMetadataQueryPerformance() {
        long startTime = System.currentTimeMillis();
        LinkedHashMap<String, Table<?>> tablesMap = ServiceProxy.metadata().tables();
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        System.out.println("获取元数据耗时: " + duration + "ms");
        System.out.println("获取到表数量: " + tablesMap.size());

        // 简单性能断言：确保查询在合理时间内完成（假设小于30秒）
        assertTrue(duration < 30000, "元数据查询应在30秒内完成");
    }
}
