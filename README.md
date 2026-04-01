

### Api模块

依赖如下

```xml
<dependency>
    <groupId>com.nhb</groupId>
    <artifactId>nhb-common-api</artifactId>
    <version>${version}</version>
</dependency>
```

实现将JavaDoc文档转换为接口文档(javaDoc注释需规范编写)，支持SA-TOKEN的权限/角色注解信息的获取

### Security模块

依赖如下：

```xml
<dependency>
    <groupId>com.nhb</groupId>
    <artifactId>nhb-common-security</artifactId>
    <version>${version}</version>
    <exclusions>
        <!--不需要redis依赖的情况下(不需要sa-token分布式会话功能) 可剔除该依赖-->
        <exclusion>
           <groupId>com.nhb</groupId>
           <artifactId>nhb-common-redis</artifactId>
        </exclusion>
     </exclusions>
</dependency>
<!--web-flux环境下 需要额外引用该依赖-->
<dependency>
     <groupId>cn.dev33</groupId>
     <artifactId>sa-token-reactor-spring-boot3-starter</artifactId>
</dependency>
<!--web-mvc环境下 需要额外引用该依赖-->
<dependency>
     <groupId>cn.dev33</groupId>
     <artifactId>sa-token-spring-boot3-starter</artifactId>
</dependency>
```

```yaml
# 内置默认配置 写相同配置覆盖
# Sa-Token配置
sa-token:
  # 允许动态设置 token 有效期
  dynamic-active-timeout: true
  # 允许从 请求参数 读取 token
  is-read-body: true
  # 允许从 header 读取 token
  is-read-header: true
  # 关闭 cookie 鉴权 从根源杜绝 csrf 漏洞风险
  is-read-cookie: false
  # token前缀
  token-prefix: "Bearer"
```

支持以下功能：

1. web-mvc/web-flux环境下白名单过滤

   ```yaml
   security:
     ignore:
       paths:
       #默认配置项 结合API模块时 需要将该路径放行(无需登录)
         - /*/v3/api-docs
         - /v3/api-docs
   ```

   

2. 提供UserContextUtil工具类

   ```java
   //登录功能 实际用于登录操作时将信息存入SA-TOKEN 会话中
   com.nhb.common.security.utils.UserContextUtil#login
       
   //更新用户信息 将SA-TOKEN中得用户当前会话更新信息 可用于权限/角色变化后的更新操作
   com.nhb.common.security.utils.UserContextUtil#reflushUserDetail
       
   //获取当前用户会话信息
   com.nhb.common.security.utils.UserContextUtil#getCurrentUserDetail
      
   //获取当前用户ID
   com.nhb.common.security.utils.UserContextUtil#getUserId
   
   //获取当前用户deptId
   com.nhb.common.security.utils.UserContextUtil#getDeptId
     
   //获取当前用户租户ID
   com.nhb.common.security.utils.UserContextUtil#getTenantId
       
   //获取当前用户 角色CODE列表
   com.nhb.common.security.utils.UserContextUtil#getRoleCodes
    
   //获取当前用户 权限列表    
   com.nhb.common.security.utils.UserContextUtil#getPermissions
       
   //当前用户是否为超级管理员   
   com.nhb.common.security.utils.UserContextUtil#isSuperAdmin
   
   //当前用户是否为租户管理员
   com.nhb.common.security.utils.UserContextUtil#isTenantAdmin
     
   //获取当前用户的额外信息
   com.nhb.common.security.utils.UserContextUtil#getExtra
   ```

### Generator模块

依赖如下：

```xml
<dependency>
    <groupId>com.nhb</groupId>
    <artifactId>nhb-common-generator</artifactId>
    <version>${version}</version>
</dependency>
```

目前实现以下功能

1. 将数据库中的表结构/指定表名 转换为实体类/Vo(MapStructPlus可转换的VO)/Mapper接口/Mapper.xml文件
2. 将指定包下的实体类(@TableName 注解)/指定实体类转换为表结构且通过实体类属性上的@NotBlank/@NotEmpty/@NotNull来判断表字段是否必填、根据属性上@Size中max值来约定最大值
3. 将指定包下的实体类/指定实体类 转换为实体类对应的MapStructPlus可转换的Vo类型

```yaml
spring:
  datasource:
    #以下为自定义配置项
    generator:
      table-config:
        enabled: false/true #是否开启表结构 <---> 实体类 互转功能
        author: luck_nhb  #默认值 可自行配置覆盖
        package-name: #如果是实体类转表结构 此处表示实体类所在包路径/ 如果是表结构转实体类 此处表示生成的实体类包路径的上一级  例如 XXX.package 则生成实体类包路径为  XXX.package.entity/XXX.package.vo
        table-prefix: #表结构转实体类时 实体类名需要剔除掉的表名前缀  tb_XXX -> XXX
    dynamic:  #支持动态数据库
      enabled: false  #在该模块中虽然支持 但是默认未不开启  需要开启可自行覆盖该配置为true 并配置动态数据源
        
```

示例

```java
//表结构生成实体类   自行注入MybatisTableEntityGenerator至容器中 
MybatisTableEntityGenerator mybatisTableEntityGenerator = SpringContextUtil.getBean(MybatisTableEntityGenerator.class);
//可提供指定表名 如果不提供则处理数据库下所有表 生成对应实体类
mybatisTableEntityGenerator.generate(List.of("config_info"));

//实体类生成表结构  自行注入MybatisEntityTableGenerator至容器中 
MybatisEntityTableGenerator mybatisEntityTableGenerator = SpringContextUtil.getBean(MybatisEntityTableGenerator.class);
//可提供指定实体类 如果不提供则处理配置项中包路径下所有实体类(类上需有@TableName 注解)来生成表结构
mybatisEntityTableGenerator.generate(null);

//实体类生成MapStructPlus对应Vo  自行注入MapStructPlusGenerator至容器中 
MapStructPlusGenerator mapStructPlusGenerator = SpringContextUtil.getBean(MapStructPlusGenerator.class);
//可提供指定实体类 如果不提供则处理配置项中包路径下所有实体类来生成MapStructPlus对应Vo
mapStructPlusGenerator.generate(null);
```

