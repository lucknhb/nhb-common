package ${packageName}.entity;

<#if tenantFlag>
import com.nhb.common.mybatis.core.TenantEntity;
<#else>
import com.nhb.common.mybatis.core.BaseEntity;
</#if>
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
<#list imports as import>
import ${import};
</#list>

/**
* @author ${author}
* @date ${dateTime}
* @description: 对应表名 ${tableName}
*/
<#if tenantFlag>
    <#assign Entity = "TenantEntity">
<#else>
    <#assign Entity = "BaseEntity">
</#if>
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("${tableName}")
public class ${entityName} extends ${Entity} {
<#list columns as column>
    /**
    * ${column.columnComment}
    */
    <#if column.javaField == 'delFlag'>
    @TableLogic
    </#if>
    <#if column.javaField == 'version'>
    @Version
    </#if>
    <#if column.primaryKeyFlag && column.incrementFlag>
    @TableId(value = "${column.columnName}")
    </#if>
    <#if column.primaryKeyFlag && !column.incrementFlag>
    @TableId(value = "${column.columnName}",type = IdType.ASSIGN_ID)
    </#if>
    private ${column.javaType} ${column.javaField};
</#list>
}