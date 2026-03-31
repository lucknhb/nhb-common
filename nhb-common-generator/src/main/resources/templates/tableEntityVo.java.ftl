package ${packageName}.vo;

<#list imports as import>
import ${import};
</#list>
import ${packageName}.entity.${entityName};
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
* @author ${author}
* @date ${dateTime}
* @description: ${functionInfo}视图对象 ${tableName}
*/
@Data
@AutoMapper(target = ${entityName}.class)
public class ${entityName}Vo implements Serializable {
<#list columns as column>
    /**
    * ${column.columnComment}
    */
    private ${column.javaType} ${column.javaField};
</#list>

}