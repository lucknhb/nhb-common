

### Generator模块

目前实现以下功能

1. 将数据库中的表结构转换为实体类/Vo(MapStructPlus可转换的VO)/Mapper接口/Mapper.xml文件
2. 将指定包下的实体类(@TableName 注解)转换为表结构且通过实体类属性上的@NotBlank/@NotEmpty/@NotNull来判断表字段是否必填、根据属性上@Size中max值来约定最大值



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

