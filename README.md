#GUIDE

---

## docker env

- `REDIS_HOST`: The hostname or IP address of the Redis server (applicable for standalone deployment).
- `REDIS_PORT`: The port number on which the Redis server is running (applicable for standalone deployment).
- `REDIS_USERNAME`: The username for Redis ACL (Access Control List). Provide the username if ACL authentication is enabled, otherwise leave it blank.
- `REDIS_PASSWORD`: The password for the Redis server. Provide the password if password authentication is enabled, otherwise leave it blank.
- `REDIS_ARCH`: The architecture type for Redis. Supported values are: `cluster` (cluster mode), `standalone` (single instance mode), and `sentinel` (sentinel mode).
- `REDIS_ADDRESS`: A comma-separated list of Redis addresses, applicable for cluster and sentinel modes. For example: "127.0.0.1:6379,127.0.0.1:6380,[::1]:6381".
- `REDIS_MASTER_NAME`: The master node name in Redis sentinel mode. The default value is "mymaster". Required only when `REDIS_ARCH` is set to "sentinel".

example:
```
# standalone
docker run -it -e REDIS_HOST=192.168.68.121  -e REDIS_PORT=6379  -e REDIS_PASSWORD="123" ghcr.io/devineliu/redis-demo:v0.0.1

# cluster 
docker run -it -e REDIS_ADDRESS=192.168.68.121:30001,192.168.68.121:30002,192.168.68.121:30003  -e REDIS_ARCH=cluster   ghcr.io/devineliu/redis-demo:v0.0.1

#sentinel
docker run -it -e REDIS_ADDRESS=192.168.68.121:26379,192.168.68.120:26379,192.168.68.122:26379  -e REDIS_ARCH=sentinel   ghcr.io/devineliu/redis-demo:v0.0.1



```