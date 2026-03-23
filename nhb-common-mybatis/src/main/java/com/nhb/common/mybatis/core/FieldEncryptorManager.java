package com.nhb.common.mybatis.core;

import cn.hutool.core.collection.CollUtil;
import com.nhb.common.core.utils.ObjectSelfUtil;
import com.nhb.common.encrypt.annotation.FieldEncrypt;
import com.nhb.common.encrypt.core.EncryptorManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/23 14:11
 * @description: 字段加解密注解处理
 */
@Slf4j
public class FieldEncryptorManager extends EncryptorManager {
    /**
     * 类加密字段缓存
     */
    Map<Class<?>, Set<Field>> fieldCache = new ConcurrentHashMap<>();

    /**
     * 构造方法传入类加密字段缓存
     *
     * @param typeAliasesPackage 实体类包
     */
    public FieldEncryptorManager(String typeAliasesPackage) {
        scanEncryptClasses(typeAliasesPackage);
    }


    /**
     * 获取类加密字段缓存
     */
    public Set<Field> getFieldCache(Class<?> sourceClazz) {
        return ObjectSelfUtil.notNullGetter(fieldCache, f -> f.get(sourceClazz));
    }

    /**
     * 通过 typeAliasesPackage 设置的扫描包 扫描缓存实体
     */
    private void scanEncryptClasses(String typeAliasesPackage) {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        CachingMetadataReaderFactory factory = new CachingMetadataReaderFactory();
        String[] packagePatternArray = StringUtils.splitPreserveAllTokens(typeAliasesPackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
        String classpath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;
        try {
            for (String packagePattern : packagePatternArray) {
                String path = ClassUtils.convertClassNameToResourcePath(packagePattern);
                Resource[] resources = resolver.getResources(classpath + path + "/*.class");
                for (Resource resource : resources) {
                    ClassMetadata classMetadata = factory.getMetadataReader(resource).getClassMetadata();
                    Class<?> clazz = Resources.classForName(classMetadata.getClassName());
                    Set<Field> encryptFieldSet = getEncryptFieldSetFromClazz(clazz);
                    if (CollUtil.isNotEmpty(encryptFieldSet)) {
                        fieldCache.put(clazz, encryptFieldSet);
                    }
                }
            }
        } catch (Exception e) {
            log.error("初始化数据安全缓存时出错:{}", e.getMessage());
        }
    }

    /**
     * 获得一个类的加密字段集合
     */
    private Set<Field> getEncryptFieldSetFromClazz(Class<?> clazz) {
        Set<Field> fieldSet = new HashSet<>();
        // 判断clazz如果是接口,内部类,匿名类就直接返回
        if (clazz.isInterface() || clazz.isMemberClass() || clazz.isAnonymousClass()) {
            return fieldSet;
        }
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            fieldSet.addAll(Arrays.asList(fields));
            clazz = clazz.getSuperclass();
        }
        fieldSet = fieldSet.stream().filter(field ->
                        field.isAnnotationPresent(FieldEncrypt.class) && field.getType() == String.class)
                .collect(Collectors.toSet());
        for (Field field : fieldSet) {
            field.setAccessible(true);
        }
        return fieldSet;
    }

}
