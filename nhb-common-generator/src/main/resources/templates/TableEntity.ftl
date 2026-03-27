package ${packageName}.domain;

<#assign IsTenant = false>
<#list columns as column>
    <#if column.javaField == 'tenantId'>
        <#assign IsTenant = true>
    </#if>
</#list>
<#if IsTenant>
    import com.nhb.common.mybatis.core.TenantEntity;
<#else>
    import com.nhb.common.mybatis.core.BaseEntity;
</#if>
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
<#list importList as import>
    import ${import};
</#list>

/**
* 对象 ${tableName}
*
* @author ${author}
* @date ${dateTime}
*/
<#if IsTenant>
    <#assign Entity = "TenantEntity">
<#else>
    <#assign Entity = "BaseEntity">
</#if>
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("${tableName}")
public class ${entityName} extends ${Entity} {

<#list columns as column>
    <#if !table.isSuperColumn(column.javaField)>
        /**
        * ${column.columnComment}
        */
        <#if column.javaField == 'delFlag'>
            @TableLogic
        </#if>
        <#if column.javaField == 'version'>
            @Version
        </#if>
        <#if column.isPk == 1>
            @TableId(value = "${column.columnName}")
        </#if>
        private ${column.javaType} ${column.javaField};

    </#if>
</#list>

}