package com.nhb.common.generator.core;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.generator.properties.GeneratorConfigProperties;
import com.nhb.common.generator.utils.TableConvertUtil;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.service.AnylineService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/30 13:15
 * @description: 实体类转表结构
 */
public class MybatisEntityTableGenerator implements TableEntityGenerator {
    /**
     * 生成对应的模板
     */
    @Override
    public void generate() {
        GeneratorConfigProperties generatorConfigProperties = SpringContextUtil.getBean(GeneratorConfigProperties.class);
        AnylineService anylineService = SpringContextUtil.getBean(AnylineService.class);
        Set<Class<?>> classes = scanEntities(generatorConfigProperties.getTableConfig().getPackageName());
        for (Class<?> aClass : classes) {
            Table table = buildTableFromEntity(aClass);
            try {
                anylineService.ddl().save(table);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 将实体类转换为 Anyline 的 Table 对象
     */
    private Table buildTableFromEntity(Class<?> entityClass) {
        // 获取表名
        TableName tableNameAnno = entityClass.getAnnotation(TableName.class);
        String tableName = (tableNameAnno != null) ? tableNameAnno.value() : entityClass.getSimpleName().toLowerCase();
        Table table = new Table(tableName);
        if (tableNameAnno != null && !tableNameAnno.schema().isEmpty()) {
            table.setSchema(tableNameAnno.schema());
        }
        // 遍历字段
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            // 获取字段映射的列名
            TableField fieldAnno = field.getAnnotation(TableField.class);
            String columnName = (fieldAnno != null && !fieldAnno.value().isEmpty())
                    ? fieldAnno.value()
                    : StrUtil.toUnderlineCase(field.getName());
            // 跳过逻辑删除字段（可根据需要决定是否同步）
            if (field.isAnnotationPresent(TableLogic.class)) {
                continue;
            }
            //填充获取字段
            Column column = fillColumn(field, columnName);
            table.addColumn(column);
        }

        return table;
    }

    /**
     * 根据实体类属性名转换为表对应的字段
     * @param field        实体属性
     * @param columnName   字段名
     * @return
     */
    private Column fillColumn(Field field, String columnName) {
        // 映射 Java 类型到 Anyline DataType
        String dataType = TableConvertUtil.javaTypeToJdbcType(field.getType());
        // 创建 Column 对象
        Column column = new Column(columnName, dataType);
        column.setComment(field.getName()); // 可替换为更友好的注释
        // 处理主键
        if (field.isAnnotationPresent(TableId.class)) {
            column.setPrimaryKey(true);
            TableId idAnno = field.getAnnotation(TableId.class);
            if (idAnno.type() == IdType.AUTO) {
                column.setAutoIncrement(true);
            }
        }
        // 可根据需要设置其他属性，如长度、是否可空等
        // column.setLength(50);
        // column.setNullable(false);
        return column;
    }

    /**
     * 扫描包下所有带有 @TableName 注解的类
     *
     * @param basePackage 包名
     * @return 实体类集合
     */
    private Set<Class<?>> scanEntities(String basePackage) {
        try {
            Set<Class<?>> classes = new HashSet<>();
            ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourceResolver);
            // 将包名转换为资源路径
            String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                    ClassUtils.convertClassNameToResourcePath(basePackage) + "/**/*.class";
            Resource[] resources = resourceResolver.getResources(packageSearchPath);
            for (Resource resource : resources) {
                if (resource.isReadable()) {
                    MetadataReader reader = readerFactory.getMetadataReader(resource);
                    String className = reader.getClassMetadata().getClassName();
                    Class<?> clazz;
                    try {
                        clazz = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        continue;
                    }
                    // 过滤：不是接口、不是抽象类，且有 @TableName 注解
                    if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())
                            && clazz.isAnnotationPresent(TableName.class)) {
                        classes.add(clazz);
                    }
                }
            }
            return classes;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
