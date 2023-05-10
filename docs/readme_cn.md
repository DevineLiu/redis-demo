# 指南


## Docker 环境变量

- `REDIS_HOST`：Redis 服务器的主机名或 IP 地址（仅适用于单实例部署）。
- `REDIS_PORT`：Redis 服务器运行所在的端口号（仅适用于单实例部署）。
- `REDIS_USERNAME`：Redis ACL（访问控制列表）的用户名。如果启用了 ACL 认证，请提供用户名，否则留空。
- `REDIS_PASSWORD`：Redis 服务器的密码。如果启用了密码认证，请提供密码，否则留空。
- `REDIS_ARCH`：Redis 的架构类型。支持的值有：`cluster`（集群模式），`standalone`（单实例模式），和 `sentinel`（哨兵模式）。
- `REDIS_ADDRESS`：适用于集群和哨兵模式的 Redis 地址列表，用逗号分隔。例如："127.0.0.1:6379,127.0.0.1:6380,[::1]:6381"。
- `REDIS_MASTER_NAME`：Redis 哨兵模式中的主节点名称。默认值为 "mymaster"。仅在 `REDIS_ARCH` 设置为 "sentinel" 时需要。

示例：
```
# 单实例
docker run -it -e REDIS_HOST=192.168.68.121 -e REDIS_PORT=6379 -e REDIS_PASSWORD="123" ghcr.io/devineliu/redis-demo:v0.0.1

# 集群
docker run -it -e REDIS_ADDRESS=192.168.68.121:30001,192.168.68.121:30002,192.168.68.121:30003 -e REDIS_ARCH=cluster ghcr.io/devineliu/redis-demo:v0.0.1

# 哨兵
docker run -it -e REDIS_ADDRESS=192.168.68.121:26379,192.168.68.120:26379,192.168.68.122:26379 -e REDIS_ARCH=sentinel ghcr.io/devineliu/redis-demo:v0.0.1

```
