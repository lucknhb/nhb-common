package com.nhb.common.generator.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.*;
import com.github.therapi.runtimejavadoc.ClassJavadoc;
import com.github.therapi.runtimejavadoc.CommentFormatter;
import com.github.therapi.runtimejavadoc.OtherJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.generator.properties.GeneratorConfigProperties;
import com.nhb.common.generator.utils.TableConvertUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
import java.util.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/30 13:15
 * @description: 实体类转表结构
 */
public class MybatisEntityTableGenerator implements TableEntityGenerator {

    private static final CommentFormatter COMMENT_FORMATTER = new CommentFormatter();

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
        Map<String, String> commentMap = TableConvertUtil.parseFieldComments(entityClass);
        // 获取表名
        TableName tableNameAnno = entityClass.getAnnotation(TableName.class);
        String tableName = (tableNameAnno != null) ? tableNameAnno.value() : entityClass.getSimpleName().toLowerCase();
        Table table = new Table(tableName);
        ClassJavadoc classDoc = RuntimeJavadoc.getJavadoc(entityClass);
        String comment = COMMENT_FORMATTER.format(classDoc.getComment());
        if (StringUtil.isBlank(comment) && CollUtil.isNotEmpty(classDoc.getOther())) {
            List<String> otherJavadocs = new ArrayList<>();
            for (OtherJavadoc otherJavadoc : classDoc.getOther()) {
                otherJavadocs.add(StrUtil.format("{} {}", otherJavadoc.getName(), otherJavadoc.getComment()));
            }
            comment = String.join(StrPool.LF,otherJavadocs);
        }
        table.setComment(comment);
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
            Column column = fillColumn(field, columnName, commentMap);
            table.addColumn(column);
        }

        return table;
    }

    /**
     * 根据实体类属性名转换为表对应的字段
     *
     * @param field      实体属性
     * @param columnName 字段名
     * @param commentMap 备注信息
     * @return
     */
    private Column fillColumn(Field field, String columnName, Map<String, String> commentMap) {
        // 映射 Java 类型到 Anyline DataType
        String dataType = TableConvertUtil.javaTypeToJdbcType(field.getType());
        // 创建 Column 对象
        Column column = new Column(columnName, dataType);
        column.setComment(commentMap.getOrDefault(field.getName(),field.getName())); // 可替换为更友好的注释
        // 处理主键
        if (field.isAnnotationPresent(TableId.class)) {
            column.setPrimaryKey(true);
            column.setUnique(true);
            TableId idAnno = field.getAnnotation(TableId.class);
            if (idAnno.type() == IdType.AUTO) {
                column.setAutoIncrement(true);
            }
        }
        // 可根据需要设置其他属性
        if (field.isAnnotationPresent(NotBlank.class) || field.isAnnotationPresent(NotEmpty.class) || field.isAnnotationPresent(NotNull.class)) {
            column.setNullable(false);
        }
        if (field.isAnnotationPresent(Size.class)) {
            Size size = field.getAnnotation(Size.class);
            column.setLength(size.max());
        }
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
