
apidoc可考虑smart-doc

https://x-file-storage.xuyanwu.cn/

### Redis模块

该模块整合了<font color='red'>redisson</font>以及<font color='red'>lock4j(分布式锁)</font>  配置项如下

```yaml
# redis 单机配置(单机与集群只能开启一个另一个需要注释掉)
spring.data:
  redis:
    # 地址
    host: localhost
    # 端口，默认为6379
    port: 
    # 数据库索引
    database: 0
    # redis 密码必须配置
    password: 
    # 连接超时时间
    timeout: 10s
    # 是否开启ssl
    ssl.enabled: false
# redisson 配置
redisson:
  # redis key前缀
  keyPrefix:
  # 线程池数量
  threads: 16
  # Netty线程池数量
  nettyThreads: 32
  # 单节点配置
  singleServerConfig:
    # 客户端名称 不能用中文
    clientName: 
    # 最小空闲连接数
    connectionMinimumIdleSize: 32
    # 连接池大小
    connectionPoolSize: 64
    # 连接空闲超时，单位：毫秒
    idleConnectionTimeout: 10000
    # 命令等待超时，单位：毫秒
    timeout: 3000
    # 发布和订阅连接池大小
    subscriptionConnectionPoolSize: 50
    
 # redis 集群配置(单机与集群只能开启一个另一个需要注释掉)
 spring.data:
   redis:
     cluster:
       nodes:
         - 192.168.0.100:6379
         - 192.168.0.101:6379
         - 192.168.0.102:6379
     # 密码
     password:
     # 连接超时时间
     timeout: 10s
     # 是否开启ssl
     ssl.enabled: false
 redisson:
   # 线程池数量
   threads: 16# Netty线程池数量
   nettyThreads: 32
   # 集群配置
   clusterServersConfig:
     # 客户端名称
     clientName: 
     # master最小空闲连接数
     masterConnectionMinimumIdleSize: 32
     # master连接池大小 masterConnectionPoolSize: 64
     # slave最小空闲连接数
     slaveConnectionMinimumIdleSize: 32
     # slave连接池大小
     slaveConnectionPoolSize: 64
     # 连接空闲超时，单位：毫秒
     idleConnectionTimeout: 10000
     # 命令等待超时，单位：毫秒
     timeout: 3000
     # 发布和订阅连接池大小
     subscriptionConnectionPoolSize: 50
     # 读取模式
     readMode: "SLAVE"
     # 订阅模式
     subscriptionMode: "MASTER"
```



