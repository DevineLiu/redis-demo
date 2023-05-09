
FROM maven:3.6.3-openjdk-8 AS build

# 设置工作目录
WORKDIR /build

# 将项目 pom.xml 文件复制到容器中
COPY pom.xml .

# 下载项目依赖
RUN mvn dependency:go-offline

# 将项目源代码复制到容器中
COPY src /build/src

# 构建项目
RUN mvn clean package


FROM openjdk:8-jre-slim

# 设置工作目录
WORKDIR /app
COPY --from=build /build/target/redis-demo-1.0-SNAPSHOT.jar /app/app.jar

# 设置容器的默认命令
CMD ["java", "-jar", "app.jar"]