package com.nhb.common.mybatis.interceptor;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.nhb.common.core.utils.StringUtil;
import com.nhb.common.encrypt.annotation.FieldEncrypt;
import com.nhb.common.encrypt.core.EncryptContext;
import com.nhb.common.encrypt.enums.AlgorithmType;
import com.nhb.common.encrypt.enums.EncodeType;
import com.nhb.common.mybatis.core.FieldEncryptorManager;
import com.nhb.common.mybatis.properties.FieldEncryptorConfigProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/3/23 14:23
 * @description: 入参加密拦截器
 */
@Slf4j
@Intercepts({@Signature(
        type = ParameterHandler.class,
        method = "setParameters",
        args = {PreparedStatement.class})
})
@AllArgsConstructor
public class MybatisPlusEncryptInterceptor implements Interceptor {

    private final FieldEncryptorManager fieldEncryptorManager;
    private final FieldEncryptorConfigProperties fieldEncryptorConfigProperties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return invocation;
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof ParameterHandler parameterHandler) {
            // 进行加密操作
            Object parameterObject = parameterHandler.getParameterObject();
            if (ObjectUtil.isNotNull(parameterObject) && !(parameterObject instanceof String)) {
                this.encryptHandler(parameterObject);
            }
        }
        return target;
    }

    /**
     * 加密对象
     *
     * @param sourceObject 待加密对象
     */
    private void encryptHandler(Object sourceObject) {
        if (ObjectUtil.isNull(sourceObject)) {
            return;
        }
        if (sourceObject instanceof Map<?, ?> map) {
            new HashSet<>(map.values()).forEach(this::encryptHandler);
            return;
        }
        if (sourceObject instanceof List<?> list) {
            if(CollUtil.isEmpty(list)) {
                return;
            }
            // 判断第一个元素是否含有注解。如果没有直接返回，提高效率
            Object firstItem = list.getFirst();
            if (ObjectUtil.isNull(firstItem) || CollUtil.isEmpty(fieldEncryptorManager.getFieldCache(firstItem.getClass()))) {
                return;
            }
            list.forEach(this::encryptHandler);
            return;
        }
        // 不在缓存中的类,就是没有加密注解的类(当然也有可能是typeAliasesPackage写错)
        Set<Field> fields = fieldEncryptorManager.getFieldCache(sourceObject.getClass());
        if(ObjectUtil.isNull(fields)){
            return;
        }
        try {
            for (Field field : fields) {
                field.set(sourceObject, this.encryptField(Convert.toStr(field.get(sourceObject)), field));
            }
        } catch (Exception e) {
            log.error("处理加密字段时出错", e);
        }
    }

    /**
     * 字段值进行加密。通过字段的批注注册新的加密算法
     *
     * @param value 待加密的值
     * @param field 待加密字段
     * @return 加密后结果
     */
    private String encryptField(String value, Field field) {
        if (ObjectUtil.isNull(value)) {
            return null;
        }
        FieldEncrypt encryptField = field.getAnnotation(FieldEncrypt.class);
        EncryptContext encryptContext = new EncryptContext();
        encryptContext.setAlgorithm(encryptField.algorithm() == AlgorithmType.DEFAULT ? fieldEncryptorConfigProperties.getAlgorithm() : encryptField.algorithm());
        encryptContext.setEncode(encryptField.encode() == EncodeType.DEFAULT ? fieldEncryptorConfigProperties.getEncode() : encryptField.encode());
        encryptContext.setPassword(StringUtil.isBlank(encryptField.password()) ? fieldEncryptorConfigProperties.getPassword() : encryptField.password());
        encryptContext.setPrivateKey(StringUtil.isBlank(encryptField.privateKey()) ? fieldEncryptorConfigProperties.getPrivateKey() : encryptField.privateKey());
        encryptContext.setPublicKey(StringUtil.isBlank(encryptField.publicKey()) ? fieldEncryptorConfigProperties.getPublicKey() : encryptField.publicKey());
        return this.fieldEncryptorManager.encrypt(value, encryptContext);
    }


    @Override
    public void setProperties(Properties properties) {
    }
}
