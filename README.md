### 版本
```
java17，jdk17，mvn3.9.9
```


### 接口规范
https://xvzjebnt164.feishu.cn/wiki/UmJmwvMaoiU7oNkOR4EcUncLnJb

### 运行后端项目

后端配置文件被git忽略了，请自行添加 src/main/resources/application.properties
```application
# 数据库连接（端口和密码根据实际情况调整）
spring.datasource.url=jdbc:mysql://localhost:13306/test_database?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=12345678

# Hibernate 配置
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Shanghai
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode= never
# 第一次运行时将以下两行注释去掉，以初始化数据库
# spring.sql.init.mode=always
# spring.sql.init.schema-locations=classpath:schema.sql

# 日志级别
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=DEBUG

# 应用服务 WEB 访问端口
server.port=8088

# MyBatis 配置
mybatis.mapper-locations=classpath:mappers/*xml
mybatis.type-aliases-package=com.example.webproject2.mybatis.entity

# 禁用二级缓存
spring.jpa.properties.hibernate.cache.use_second_level_cache=false
spring.jpa.properties.hibernate.cache.use_query_cache=false

# 文件上传相关配置
file.upload-dir=D:/program/web/webproject2/uploads
# 文件对外访问的 URL 前缀（根据实际服务器配置调整）
file.access-url=http://localhost:8088/uploads/
# 单个文件最大大小
spring.servlet.multipart.max-file-size=100MB
# 单次请求最大大小
spring.servlet.multipart.max-request-size=200MB

# 静态资源访问路径
spring.web.resources.static-locations=file:D:/program/web/webproject2/uploads/

```

直接运行下面的文件即可
```
src/main/java/com/example/webproject2/Webproject2Application.java
```