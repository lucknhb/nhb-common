package com.nhb.common.generator.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhb.common.core.utils.FreeMarkerTemplateUtil;
import com.nhb.common.core.utils.ResourceFileUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.generator.properties.GeneratorConfigProperties;
import com.nhb.common.generator.utils.TableConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/31 11:15
 * @description: 根据实体类生成可通过MapStructPlus 自由转换的Vo类
 */
@Slf4j
public class MapStructPlusGenerator {

    /**
     * 根据实体类转换为MapStructPlus 自由转换的Vo类
     * @param classes
     */
    public void generate(Set<Class<?>> classes) {
        GeneratorConfigProperties generatorConfigProperties = SpringContextUtil.getBean(GeneratorConfigProperties.class);
        if (CollUtil.isEmpty(classes)) {
            Assert.hasLength(generatorConfigProperties.getTableConfig().getPackageName(),"spring.datasource.generator.table-config.package-name 配置项为空");
            classes = ClassUtil.scanPackage(generatorConfigProperties.getTableConfig().getPackageName());
        }
        for (Class<?> aClass : classes) {
            TableInfo tableInfo = new TableInfo();
            Set<String> imports = new HashSet<>();
            List<TableColumnInfo> columnInfos = new ArrayList<>();
            tableInfo.setImports(imports);
            tableInfo.setColumns(columnInfos);
            tableInfo.setPackageName(generatorConfigProperties.getTableConfig().getPackageName());
            tableInfo.setTableName(aClass.getSimpleName());
            tableInfo.setEntityName(aClass.getSimpleName());
            tableInfo.setFunctionInfo("");
            tableInfo.setDateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
            tableInfo.setAuthor(StrUtil.blankToDefault(generatorConfigProperties.getTableConfig().getAuthor(), ""));
            Map<String, String> commentMap = TableConvertUtil.parseFieldComments(aClass);
            for (Field field : aClass.getDeclaredFields()) {
                // 跳过静态字段
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                String fieldType = field.getType().getSimpleName();
                String fieldName = field.getName();
                TableColumnInfo tableColumnInfo = new TableColumnInfo();
                tableColumnInfo.setJavaField(fieldName);
                tableColumnInfo.setJavaType(fieldType);
                tableColumnInfo.setColumnComment(commentMap.getOrDefault(fieldName, fieldName));
                columnInfos.add(tableColumnInfo);
                // 记录需要导入的类型（java.lang 包下的类无需导入）
                if (!field.getType().isPrimitive() && !field.getType().getName().startsWith("java.lang")) {
                    imports.add(field.getType().getCanonicalName());
                }
            }
            String templateName = "templates/tableEntityVo.java.ftl";
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> paramMap = mapper.convertValue(tableInfo, new TypeReference<>() {});
            try {
                String content = FreeMarkerTemplateUtil.getContent(ResourceFileUtil.getResource(templateName)
                        .getContentAsString(StandardCharsets.UTF_8)
                        .trim(), paramMap);
                String voPath = TableConvertUtil.writePath(templateName, tableInfo);
                FileUtil.writeUtf8String(content,voPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
