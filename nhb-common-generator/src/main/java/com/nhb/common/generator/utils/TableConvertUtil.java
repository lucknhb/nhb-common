package com.nhb.common.generator.utils;

import cn.hutool.core.util.StrUtil;
import com.github.therapi.runtimejavadoc.ClassJavadoc;
import com.github.therapi.runtimejavadoc.CommentFormatter;
import com.github.therapi.runtimejavadoc.FieldJavadoc;
import com.github.therapi.runtimejavadoc.RuntimeJavadoc;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.generator.core.TableInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/30 10:04
 * @description:
 */
@Slf4j
public class TableConvertUtil {
    /**
     * 项目空间路径
     */
    public static final String PROJECT_PATH = "main/java";
    /**
     * mybatis空间路径
     */
    public static final String MAPPER_PATH = "main/resources/mapper";

    private static final CommentFormatter COMMENT_FORMATTER = new CommentFormatter();


    /**
     * 根据模板名及表信息生成对应的路径
     * @param templateName  模板名称
     * @param tableInfo     表信息
     * @return
     */
    public static String writePath(String templateName, TableInfo tableInfo) {
        String javaPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + PROJECT_PATH + "/" + StringUtils.replace(tableInfo.getPackageName(), ".", "/");
        String mapperPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + MAPPER_PATH;
        String fileName = null;
        if (templateName.contains("tableEntity.java.ftl")) {
            fileName = StrUtil.format("{}/entity/{}.java", javaPath, tableInfo.getEntityName());
        }
        if (templateName.contains("tableEntityVo.java.ftl")) {
            fileName = StrUtil.format("{}/vo/{}Vo.java", javaPath, tableInfo.getEntityName());
        }
        if (templateName.contains("tableMapper.java.ftl")) {
            fileName = StrUtil.format("{}/mapper/{}Mapper.java", javaPath, tableInfo.getEntityName());
        }
        if (templateName.contains("tableMapper.xml.ftl")) {
            fileName = StrUtil.format("{}/{}Mapper.xml", mapperPath, tableInfo.getEntityName());
        }
        return fileName;
    }

    /**
     * 数据库字段类型转Java类型
     *
     * @param jdbcType 数据库字段类型
     * @return Java类型
     */
    public static String jdbcTypeToJavaType(String jdbcType) {
        if (jdbcType == null) return "Object";
        return switch (jdbcType.toUpperCase()) {
            case "VARCHAR", "CHAR", "TEXT", "LONGTEXT", "CLOB" -> "String";
            case "INT", "TINYINT", "SMALLINT" -> "Integer";
            case "BIGINT" -> "Long";
            case "DECIMAL", "NUMERIC" -> "BigDecimal";
            case "DATE" -> "LocalDate";
            case "DATETIME", "TIMESTAMP" -> "LocalDateTime";
            case "FLOAT" -> "Float";
            case "DOUBLE" -> "Double";
            case "BOOLEAN", "BIT" -> "Boolean";
            default -> "Object";
        };
    }

    /**
     * Java 类型映射到 Anyline DataType
     */
    public static String javaTypeToJdbcType(Class<?> javaType) {
        if (javaType == String.class) return "VARCHAR";
        if (javaType == Integer.class || javaType == int.class) return "INT";
        if (javaType == Long.class || javaType == long.class) return "BIGINT";
        if (javaType == Double.class || javaType == double.class) return "DOUBLE";
        if (javaType == Float.class || javaType == float.class) return "FLOAT";
        if (javaType == Boolean.class || javaType == boolean.class) return "TINYINT";
        if (javaType == LocalDateTime.class) return "DATETIME";
        if (javaType == LocalDate.class) return "DATE";
        if (javaType == BigDecimal.class) return "DECIMAL";
        return "VARCHAR";
    }

    /**
     * 表名转换成Java类名
     *
     * @param tableName 表名称
     * @return 类名
     */
    public static String convertEntityName(String tableName, String tablePrefix) {
        if (StringUtil.isNotEmpty(tablePrefix)) {
            String[] searchList = StringUtil.split(tablePrefix, StringUtil.SEPARATOR);
            tableName = replaceFirst(tableName, searchList);
        }
        return StrUtil.upperFirst(StrUtil.toCamelCase(tableName));
    }

    /**
     * 批量替换前缀
     *
     * @param replace      替换值
     * @param searchValues 替换列表
     */
    public static String replaceFirst(String replace, String[] searchValues) {
        String text = replace;
        for (String searchString : searchValues) {
            if (replace.startsWith(searchString)) {
                text = replace.replaceFirst(searchString, StringUtils.EMPTY);
                break;
            }
        }
        return text;
    }

    /**
     * 获取类中所有字段的注释（字段名 -> 注释）
     */
    public static Map<String, String> parseFieldComments(Class<?> clazz) {
        Map<String, String> map = new HashMap<>();
        ClassJavadoc classDoc = RuntimeJavadoc.getJavadoc(clazz);
        for (FieldJavadoc fieldDoc : classDoc.getFields()) {
            String comment = COMMENT_FORMATTER.format(fieldDoc.getComment());
            if (StringUtil.isNotBlank(comment)) {
                map.put(fieldDoc.getName(), comment);
            }
        }
        return map;
    }

}
