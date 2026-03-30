package com.nhb.common.generator.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhb.common.core.utils.FreeMarkerTemplateUtil;
import com.nhb.common.core.utils.ResourceFileUtil;
import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.generator.properties.GeneratorConfigProperties;
import com.nhb.common.generator.utils.TableConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.service.AnylineService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
        AnylineService anylineService = SpringContextUtil.getBean(AnylineService.class);
        GeneratorConfigProperties generatorConfigProperties = SpringContextUtil.getBean(GeneratorConfigProperties.class);
        List tableNames = anylineService.tables();
        for (Object tableName : tableNames ) {
            //获取表格详细信息
            Table table = anylineService.metadata().table(tableName.toString());
            TableInfo tableInfo = new TableInfo();
            List<TableColumnInfo> tableColumnInfos = new ArrayList<>();
            tableInfo.setColumns(tableColumnInfos);
            Set<String> imports = new HashSet<>();
            tableInfo.setImports(imports);
            log.info("Find TableName:[{}] And To Handle Columns", tableName);
            tableInfo.setTableName(tableName.toString());
            tableInfo.setEntityName(TableConvertUtil.convertEntityName(tableName.toString(),generatorConfigProperties.getTableConfig().getTablePrefix()));
            tableInfo.setPackageName(generatorConfigProperties.getTableConfig().getPackageName());
            tableInfo.setAuthor(generatorConfigProperties.getTableConfig().getAuthor());
            tableInfo.setDateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()));
            tableInfo.setFunctionInfo(table.getComment() != null ? table.getComment() : tableName.toString());
            LinkedHashMap<String, Column> columns = anylineService.metadata().columns(tableName.toString());
            columns.forEach((columnName, column) -> {
                log.info("Table[{}] ColumnName:[{}] Column Info: {}", tableName, columnName, column);
                TableColumnInfo tableColumnInfo = new TableColumnInfo();
                tableColumnInfo.setPrimaryKeyFlag(column.isPrimaryKey());
                tableColumnInfo.setColumnName(column.getName());
                tableColumnInfo.setColumnComment(column.getComment());
                tableColumnInfo.setColumnType(column.getOriginType().toLowerCase());
                tableColumnInfo.setRequiredFlag(!column.isNullable());
                tableColumnInfo.setIncrementFlag(column.isAutoIncrement());
                tableColumnInfo.setJavaType(TableConvertUtil.jdbcTypeToJavaType(column.getTypeName()));
                tableColumnInfo.setJavaField(StrUtil.toCamelCase(column.getName().toLowerCase()));
                // 特殊字段标记（模板中会根据字段名添加注解，这里不需要额外处理）
                if ("tenantId".equals(tableColumnInfo.getJavaField())) {
                    tableInfo.setTenantFlag(true);
                }
                // 收集需要导入的包
                if ("BigDecimal".equals(tableColumnInfo.getJavaType())) {
                    imports.add("java.math.BigDecimal");
                } else if ("LocalDateTime".equals(tableColumnInfo.getJavaType())) {
                    imports.add("java.time.LocalDateTime");
                } else if ("LocalDate".equals(tableColumnInfo.getJavaType())) {
                    imports.add("java.time.LocalDate");
                }
                tableColumnInfos.add(tableColumnInfo);
            });
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String,Object> paramMap = mapper.convertValue(tableInfo, new TypeReference<>() {});
                //生成实体类
                String templateName = "templates/tableEntity.java.ftl";
                String template = ResourceFileUtil.getResource(templateName)
                        .getContentAsString(StandardCharsets.UTF_8)
                        .trim();
                String content = FreeMarkerTemplateUtil.getContent(template, paramMap);
                String entityPath = TableConvertUtil.writePath(templateName, tableInfo);
                FileUtil.writeUtf8String(content,entityPath);
                //生成Mapper 接口
                templateName = "templates/tableMapper.java.ftl";
                template = ResourceFileUtil.getResource(templateName)
                        .getContentAsString(StandardCharsets.UTF_8)
                        .trim();
                content = FreeMarkerTemplateUtil.getContent(template, paramMap);
                String entityMapperPath = TableConvertUtil.writePath(templateName, tableInfo);
                FileUtil.writeUtf8String(content,entityMapperPath);
                //生成MapStruct实体类
                templateName = "templates/tableEntityVo.java.ftl";
                template = ResourceFileUtil.getResource(templateName)
                        .getContentAsString(StandardCharsets.UTF_8)
                        .trim();
                content = FreeMarkerTemplateUtil.getContent(template, paramMap);
                String voPath = TableConvertUtil.writePath(templateName, tableInfo);
                FileUtil.writeUtf8String(content,voPath);
                //生成Mapper.xml文件
                templateName = "templates/tableMapper.xml.ftl";
                template = ResourceFileUtil.getResource(templateName)
                        .getContentAsString(StandardCharsets.UTF_8)
                        .trim();
                content = FreeMarkerTemplateUtil.getContent(template, paramMap);
                String xmlPath = TableConvertUtil.writePath(templateName, tableInfo);
                FileUtil.writeUtf8String(content,xmlPath);
            } catch (Exception e) {
                log.error("Generate Table Entity Error:[{}]",tableName,e);
            }
        }
    }
}
