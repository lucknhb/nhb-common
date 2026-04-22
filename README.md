### BOM模块

该模块为功能模块清单依赖管理，可通过引入该模块进行以下所有模块版本的管理

```xml
<dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.nhb</groupId>
                <artifactId>nhb-common-bom</artifactId>
                <version>实际版本号</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
```

### Core模块

依赖如下：

```xml
<dependency>
    <groupId>com.nhb</groupId>
    <artifactId>nhb-common-core</artifactId>
    <version>${version}/使用引入BOM方式时无需填入版本号</version>
</dependency>
```

提供了通用的工具类、配置类、常量定义、枚举类型、异常处理等核心功能

#### 1. 配置类 (config)

提供 Spring Boot 自动配置功能，包括：

- **ThreadPoolAutoConfiguration** - 线程池自动配置
  - 自动创建 ScheduledExecutorService 定时任务线程池
  - 支持虚拟线程（Virtual Threads）
  - 包含异常处理和优雅关闭机制
  
- **JacksonAutoConfiguration** - Jackson JSON 序列化配置
- **ValidatorAutoConfiguration** - 参数校验器配置
- **TransactionTemplateAutoConfiguration** - 事务模板配置
- **ApplicationAutoConfiguration** - 应用配置

#### 2. 常量类 (constant)

定义系统通用常量：

- **CacheConstants** - 缓存相关常量
- **GlobalConstants** - 全局通用常量
- **RegexConstants** - 正则表达式常量
- **StringPoolConstants** - 字符串常量池

#### 3. 领域模型

- **ResultMessage<T>** - 统一 API 响应结果封装
  - 支持泛型数据返回
  - 内置成功 (200)/失败 (500) 状态码
  - 提供丰富的静态构造方法

#### 4. 枚举类 (enums)

- **DateTimeFormatType** - 日期时间格式化类型
- **DeviceType** - 设备类型枚举
- **FileContentType** - 文件内容类型枚举
- **LoginType** - 登录类型枚举

#### 5. 异常处理 (exception)

- **ServiceException** - 业务异常类
  - 支持错误码和错误消息
  - 支持详细错误信息
  - 支持参数化消息格式化
- **I18nBaseException** - 国际化基础异常

#### 6. YamlPropertySourceFactory

yaml类型配置文件加载转换为spring环境中可通过@value取值类型

```java
//例如
@PropertySource(value = "classpath:XXXX.yaml", factory = YamlPropertySourceFactory.class)
```

#### 7.ManualTransactionManager

手动事务工具类
	支持在任意地方手动开启、提交、回滚事务，且不会影响 Spring 默认的声明式事务。
	每个线程最多只能有一个活跃的手动事务，重复开启会抛出异常

#### 8.工具类

##### BeanUtil

Bean对象相关操作 继承自org.springframework.beans.BeanUtils并进行了功能扩展

##### CharsetKitUtil

字符集工具

##### ConvertUtil

对象转换工具 例如转字符串、转BigDecimal等

##### DesensitizeUtil

脱敏工具 继承自cn.hutool.core.util.DesensitizedUtil 并进行了扩展

##### FileExportUtil

文件导出基础工具类 设置下载响应头

##### FreeMarkerTemplateUtil

FreeMarker模板操作工具类  继承自org.springframework.ui.freemarker.FreeMarkerTemplateUtils并功能扩展两个基础方法

```java
    /**
     * 根据模板获取内容
     *
     * @param template 模板
     * @param params   参数
     * @return 内容
     */
    public static String getContent(String template, Map<String, Object> params)

    /**
     * 获取模板
     *
     * @param template 模板名称
     * @return 模板
     * @throws IOException 异常
     */
    public static Template getTemplate(String template)
```

##### I18MessageUtil

国际化工具类 切记不可在初始化中使用该方法 此时容器中的数据并未初始化完成 无法获取到 MessageSource

##### IpUtil

继承自cn.hutool.core.net.NetUtil

IP工具类 进行IP转换  注意该工具类不包含通过HTTP请求获取请求方IP地址

##### JacksonUtil

jackson操作json数据工具类 包含转换json字符串、字符串转对象等

##### MapStructUtil

对MapStructPlus的操作工具类 可代替BeanUtil进行对象属性复制 性能原高于BeanUtil

参考文档：<a href="https://mapstruct.plus/introduction/quick-start.html">mapstruct-plus</a>

##### MapUtil

Map集合操作工具类

##### ObjectSelfUtil

对象工具类 继承自cn.hutool.core.util.ObjectUtil

##### ReflectSelfUtil

反射工具类 继承自cn.hutool.core.util.ReflectUtil 并 实现获取getter setter

##### ResourceFileUtil

Spring环境下 获取文件工具类 继承自org.springframework.util.ResourceUtils

##### SpringContextUtil

Spring容器工具类

##### SpringExpressionUtil

表达式工具类
提供高效、线程安全的表达式解析、求值、模板处理等功能
内部缓存已编译的表达式，避免重复解析开销

##### SqlUtil

防止SQL注入语句检测工具类

##### SslUtil

SSL 工具类

##### StreamUtil

Stream流工具类

##### StringUtil

String相关操作类 继承自org.apache.commons.lang3.StringUtils

##### ThreadPoolUtil

线程池操作工具类 继承自cn.hutool.core.thread.ThreadUtil

##### TreeBuildUtil

构建树工具

##### ValidatorUtil

校验工具类

##### YamlUtil

YAML文件解析

#### 9.validate校验处理

EnumPattern：枚举类型注解校验

JsonPattern：JSON 格式校验注解

Xss : 自定义xss校验注解 用于校验是否存在脚本注入风险

#### 注意事项

1. **线程池使用**：`ThreadPoolAutoConfiguration` 会自动创建线程池，无需手动配置
2. **虚拟线程支持**：当 Spring Boot 3.2+ 启用虚拟线程时，线程池会自动适配
3. **Stream 操作**：避免使用 `.toList()` 新语法，使用 `Collectors.toList()` 以保证可序列化性
4. **JSON 处理**：`JacksonUtil` 依赖 Spring 容器中的 ObjectMapper Bean
5. **异常处理**：`ServiceException` 支持国际化消息，建议配合 `I18nBaseException` 使用

#### 最佳实践

1. **类型转换优先使用 ConvertUtil**：所有转换都是安全的，不会抛出异常
2. **统一返回格式**：所有接口返回值都应使用 `ResultMessage` 包装
4. **Stream 工具简化代码**：优先使用 `StreamUtil` 提供的便捷方法
5. **树形结构复用**：使用 `TreeBuildUtil` 统一树形结构构建逻辑

### Api模块

依赖如下

```xml
<dependency>
    <groupId>com.nhb</groupId>
    <artifactId>nhb-common-api</artifactId>
    <version>${version}/使用引入BOM方式时无需填入版本号</version>
</dependency>
```

实现将JavaDoc文档转换为接口文档(javaDoc注释需规范编写)，支持SA-TOKEN的权限/角色注解信息的获取

### Security模块

依赖如下：

```xml
<dependency>
    <groupId>com.nhb</groupId>
    <artifactId>nhb-common-security</artifactId>
    <version>${version}/使用引入BOM方式时无需填入版本号</version>
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
    <version>${version}/使用引入BOM方式时无需填入版本号</version>
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

### Nacos模块

依赖如下

```xml
<dependency>
   <groupId>com.nhb</groupId>
   <artifactId>nhb-common-nacos</artifactId>
   <version>${version}/使用引入BOM方式时无需填入版本号</version>
</dependency>
```

由于使用的是SpringBoot3.X版本环境 引用的Nacos需符合相应版本，在此版本上的配置方式与SpringBoot2.X时存在差异

<font color='red'>application.yml是配置项入口文件</font>

```yaml
spring:
  #如果在application.yml 文件中 配置了该配置项 对应的 application-${spring.profiles.active}.yml会自动加载(如果存在该配置文件的)
  profiles:
    active: dev
  cloud:
    nacos:
      discovery:
      #该模块默认值 使用模块自己实现的注册方式(服务完全启动后再进行注册) 如果使用nacos提供的服务注册功能则需要显示的更改为 true
        register-enabled: false
#查看配置文件加载情况可使用该配置项开启
logging:
  level:
    org:
      springframework:
        boot:
          context:
            config: trace
```

```java
//从以下日志中可看出 带有spring.profiles.active的配置文件里面的配置项优先级最高 其次是Nacos配置文件 然后是使用 classpath加入的本地文件 最后是入口配置文件
Adding imported property source 'Config resource 'class path resource [application-dev.yaml]' via location 'optional:classpath:/''
Adding imported property source 'test@common-test.yml'
Adding imported property source 'Config resource 'class path resource [nacos-local.yaml]' via location 'optional:classpath:nacos-local.yaml''
Adding imported property source 'Config resource 'class path resource [application.yaml]' via location 'optional:classpath:/''
```

参考文档 <a href="https://nacos.io/docs/v3.1/ecology/use-nacos-with-spring-cloud">use-nacos-with-spring-cloud</a>  以及 [SpringCloud应用Nacos配置中心注解 ](https://nacos.io/blog/nacos-gvr7dx_awbbpb_mmufdmayp5dfozci/?spm=5238cd80.4ec37b78.0.0.56537e84O5kyNg)

```yaml
#配置示例
spring:
  cloud:
    nacos:
    #该配置为在微服务下 可打印路由配置文件
      router:
        generate-enabled: true
      # nacos 服务地址
      server-addr: 127.0.0.1:8848
      username:
      password:
      config:
        namespace: #Nacos中的Namespce名称
        group: #对应的分组名称
       
  config:
    import:
    #以下配置项中 optional:nacos代表nacos中没有对应application-dev.yml配置项时 也不报错  如果需要校验是否存在可将optional:去掉
    #另外除了可使用nacos: 还可以使用 classpath:(resouces目录下的配置文件) file:(自指定目录文件下的配置文件)
      - optional:nacos:application-dev.yml
```



### Dubbo模块

依赖如下

```xml
<dependency>
   <groupId>com.nhb</groupId>
   <artifactId>nhb-common-dubbo</artifactId>
   <version>${version}/使用引入BOM方式时无需填入版本号</version>
</dependency>
```

提供以下功能：

1. 异常处理器，提示信息自定义化
2. dubbo自定义IP注入(避免IP不正确问题)
3. 设置dubbo的项目名称 可在Nacos 服务管理-订阅者列表中 应用名 体现
4. 消费方/服务方日志打印

```yaml
# 默认配置 如需调整可自定义覆盖
dubbo:
  protocol:
    # 使用 dubbo 协议通信
    name: dubbo
    # dubbo 协议端口(-1表示自增端口,从20880开始)
    port: -1
    # 开启虚拟线程
    threadpool: virtual
  # 消费者相关配置
  application:
    name: ${spring.application.name}
    logger: slf4j
    # 可选值 interface、instance、all，默认是 all，即接口级地址、应用级地址都注册
    register-mode: instance
    service-discovery:
      # FORCE_INTERFACE，只消费接口级地址，如无地址则报错，单订阅 2.x 地址
      # APPLICATION_FIRST，智能决策接口级/应用级地址，双订阅
      # FORCE_APPLICATION，只消费应用级地址，如无地址则报错，单订阅 3.x 地址
      migration: FORCE_APPLICATION
  # 注册中心配置
  registry:
    address: nacos://${spring.cloud.nacos.server-addr}
    username: ${spring.cloud.nacos.username}
    password: ${spring.cloud.nacos.password}
    #命名空间及分组与Nacos配置中心相关配置保持一致 
    group: ${spring.cloud.nacos.config.group:DEFAULT_GROUP}
    parameters:
      namespace: ${spring.cloud.nacos.config.namespace}
  # 消费者相关配置
  consumer:
    # 结果缓存(LRU算法)
    # 会有数据不一致问题 建议在注解局部开启
    cache: false
    # 支持校验注解
    validation: jvalidationNew
    # 调用重试 不包括第一次 0为不需要重试
    retries: 0
    # 初始化检查
    check: false
  # 自定义配置
  custom:
  	#开启消费方/生产方 参数日志
    log-enabled: true
    #开启使用项目名称
    project-name-enabled: true
    #开启全局异常统一处理
    global-error-enabled: true
    #RPC调用异常返回信息 需开启global-error-enabled
    fail-message: "服务处理异常,请联系管理员"

```

### Encrypt模块

依赖如下

```xml
<dependency>
   <groupId>com.nhb</groupId>
   <artifactId>nhb-common-encrypt</artifactId>
   <version>${version}/使用引入BOM方式时无需填入版本号</version>
</dependency>
```

提供以下两个注解

1. @ApiEncrypt注解 用于接口及参数属性上  

   参数加解密过程：<font color='red'>注意: 如果接口上存在加密处理注解，则请求/返回实体类中的注解则不会被处理,接口上的加密时请参考{"data":"加密数据""}</font>
   
   ```mermaid
   graph TD
       Start([开始]) --> CheckAnnotation{是否存在ApiEncrypt注解}
       CheckAnnotation -- 否 --> ReturnValue[直接返回值] --> End([结束])
       CheckAnnotation -- 是 --> ParallelStart[过滤器分别判断]
   
       ParallelStart --> RequestBranch{request 属性是否为 true}
       ParallelStart --> ResponseBranch{response 属性是否为 true}
   
       RequestBranch -- 是 --> GetHeader[从请求头获取 RSA加密并BASE64编码后的AES密钥及向量 格式 密钥:向量]
       GetHeader --> DecryptKeyIV[分别使用 RSA 私钥解密得到 AES 密钥和向量 其中AES秘钥为32位 向量为12位]
       DecryptKeyIV --> DecryptParams[使用解密后的 AES 密钥和向量对请求参数进行解密] --> RequestEnd[request处理完成]
       RequestBranch -- 否 --> RequestEnd
   
       ResponseBranch -- 是 --> GenAES[从请求头获取 RSA加密并BASE64编码后的AES密钥及向量 格式 密钥:向量]
       GenAES --> EncryptResponse[分别使用 RSA 私钥解密得到 AES 密钥和向量 其中AES秘钥为32位 向量为12位]
       EncryptResponse --> EncryptKeyIV[使用获取到的秘钥及向量加密响应结果值]
       EncryptKeyIV --> SetHeader[将请求头中的获取到的AES秘钥写入至响应头中] --> ResponseEnd[response处理完成]
       ResponseBranch -- 否 --> ResponseEnd
   
       RequestEnd --> Join(生成结果值)
       ResponseEnd --> Join
       Join --> End
   ```

   ```yaml
   api-encrypt:
     #默认请求头标识
     headerFlag: nhb-encrypt
     #默认公钥
     publicKey: #可使用默认值
     #默认私钥
     privateKey: #可使用默认值
   ```

   

2. @FieldEncrypt注解 用于属性字段上

   数据库内容加解密 在mybatis模块已实现相应功能 若使用其他ORM框架 需自行实现功能



### Signature模块

依赖如下

```xml
<dependency>
   <groupId>com.nhb</groupId>
   <artifactId>nhb-common-signature</artifactId>
   <version>${version}/使用引入BOM方式时无需填入版本号</version>
</dependency>
```

签名生成方式使用SHA256withRSA算法，签名内容包含所有参数(参数为空的需要过滤掉)及请求头中的nonce/timestamp/clientId，按照参数名(key)的自然顺序排序使用&拼接 key=value，然后将拼接出来的字符串并SHA-256进行签名然后使用RSA秘钥进行加密。

<font color='red'>如果同时使用的Encrypt模块，已明确Signature数据获取早于Encrypt，且Encrypt的加密数据不影响Signature验签(使用原始数据验签)</font>

1. 在接口上使用@ApiSign 进行标识  可指定接口验签允许时间戳间隔 单位毫秒
2. 提供SignatureUtil工具类 可生成秘钥对(RSA/ECB/PKCS1Padding 算法)/构建参数顺序/生成签名/验签函数

### Excel模块

依赖如下

```xml
<dependency>
   <groupId>com.nhb</groupId>
   <artifactId>nhb-common-excel</artifactId>
   <version>${version}/使用引入BOM方式时无需填入版本号</version>
</dependency>
```

注解

1. @CellMerge 注解

   该注解用于列数据相同情况下，合并单元格，index 指定具体需要合并的列序号(从0开始)，mergeByField 指定属性相同时，才进行合并单元格(依赖关系)

2. @ExcelEnumFormat注解

   该注解用于枚举翻译，enumClass 指定翻译的枚举

   codeField  枚举类中对应的code属性名称，默认为code  翻译前的原始值

   valueField 枚举类中对应的属性名称，默认为value  该属性指定为翻译后的值

3. @ExcelNotation注解

   该注解用于指定批注信息

4. @ExcelRequired注解

   该注解用于列表是否必填

ExcelUtil工具类 包含Excel 导入/模板导出/实体类导出/多列表导出/多Sheet导出/表达式解析-反解析

### Fory模块

依赖如下

```xml
<dependency>
   <groupId>com.nhb</groupId>
   <artifactId>nhb-common-fory</artifactId>
   <version>${version}/使用引入BOM方式时无需填入版本号</version>
</dependency>
```

ForyFactory工厂类 提供序列化及反序列化函数

### Gateway模块

依赖如下

```xml
<dependency>
   <groupId>com.nhb</groupId>
   <artifactId>nhb-common-gateway</artifactId>
   <version>${version}/使用引入BOM方式时无需填入版本号</version>
</dependency>
```

已实现通过nacos进行动态感知实例上下线，减少网关层缓存导致一定时间的服务无法请求问题

```yaml
#默认配置
sa-token:
  #不打印sa-token logo
  is-print: false
  #开启该注解 需配合下游服务实例同时配置才能生效  生效后 无法绕过gateway直接请求实例
  checkSameToken: true
```

