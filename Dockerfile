FROM openjdk:8-jre-slim

# 设置工作目录
WORKDIR /app

# 将项目中的可执行 JAR 文件复制到容器的工作目录
COPY target/redis-demo-1.0-SNAPSHOT.jar  /app/app.jar


# 设置容器的默认命令
CMD ["java", "-jar", "app.jar"]