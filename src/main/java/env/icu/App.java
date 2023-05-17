package env.icu;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.JedisPoolConfig;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class App {
    private static final String REDIS_HOST = "REDIS_HOST";
    private static final String REDIS_PORT = "REDIS_PORT";
    private static final String REDIS_USERNAME = "REDIS_USERNAME";
    private static final String REDIS_PASSWORD = "REDIS_PASSWORD";
    private static final String REDIS_ARCH = "REDIS_ARCH";
    private static final String REDIS_ADDRESS = "REDIS_ADDRESS";
    private static final String REDIS_MASTER_NAME = "REDIS_MASTER_NAME";

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private App() {
    }

    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        String host = System.getenv(REDIS_HOST);
        if (host == null) {
            host = "127.0.0.1";
        }
        String address = System.getenv(REDIS_ADDRESS);
        if (address == null) {
            address = "127.0.0.1:6379";
        }
        String master_name = System.getenv(REDIS_MASTER_NAME);
        if (master_name == null) {
            master_name = "mymaster";
        }
        String envValue = System.getenv(REDIS_PORT);
        int port = envValue != null ? Integer.parseInt(envValue) : 6379; // provide default value
        String username = System.getenv(REDIS_USERNAME);
        String password = System.getenv(REDIS_PASSWORD);
        String arch = System.getenv(REDIS_ARCH);
        if (arch == null) {
            arch = "standalone";
        }

        switch (arch) {
            case "standalone":
                Jedis jedis = new Jedis(host, port);
                try {
                    if (username != null && password != null) {
                        jedis.auth(username, password);
                    } else if (password != null) {
                        jedis.auth(password);
                    }
                    // 获取键值对的值
                    logger.info("standalone info: {}", jedis.info());
                    for (int i = 1; i <= 1000; i++) {
                        String key = "key" + i;
                        jedis.get(key);
                    }
                    logger.info("get test success");
                } catch (JedisConnectionException e) {
                    logger.error("Failed to connect to Redis: {}", e.getMessage());
                } finally {
                    jedis.close();
                }
                break;
            case "cluster":
                Set<HostAndPort> nodes = parseHostsAndPorts(address);
                JedisCluster jedisCluster = new JedisCluster(nodes);
                if (username != null && password != null) {
                    //jedisCluster.auth(username, password);
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    jedisCluster = new JedisCluster(nodes,60,60,60,username,password,"jedis-demo",poolConfig);

                } else if (password != null) {
                    JedisPoolConfig poolConfig = new JedisPoolConfig();
                    jedisCluster = new JedisCluster(nodes,60,60,60,password,"redis-demo",poolConfig);

                }
                try {
                    logger.info("cluster nodes: {}",jedisCluster.getClusterNodes().toString());
                    for (int i = 1; i <= 1000; i++) {
                        String key = "key" + i;
                        jedisCluster.get(key);
                    }
                    logger.info("get test success");
                } finally {
                    jedisCluster.close();
                }
                break;
            case "sentinel":
                Set<HostAndPort> s_nodes = parseHostsAndPorts(address);
                Set<String> sentinels = convertHostAndPortsToStrings(s_nodes);

                JedisSentinelPool sentinelPool = new JedisSentinelPool(master_name, sentinels);
                Jedis jedisFromSentinel = null;
                try {
                    logger.info("sentinal master: {}",sentinelPool.getCurrentHostMaster().toString());
                    jedisFromSentinel = sentinelPool.getResource();
                    if (username != null && password != null) {
                        jedisFromSentinel.auth(username, password);
                    } else if (password != null) {
                        jedisFromSentinel.auth(password);
                    }
                    for (int i = 1; i <= 1000; i++) {
                        String key = "key" + i;
                        jedisFromSentinel.get(key);
                    }
                    logger.info("get test success");
                } finally {
                    if (jedisFromSentinel != null) {
                        jedisFromSentinel.close();
                    }
                    sentinelPool.close();
                }
                break;

            default:
                logger.error("err arch: {}", arch);
                break;

        }
        // 获取 Redis 连接

    }

    public static Set<HostAndPort> parseHostsAndPorts(String addresses) {
        Set<HostAndPort> hostAndPorts = new HashSet<>();
        String[] hostPortPairs = addresses.split(",");
        Pattern hostPortPattern = Pattern.compile("^\\[?([0-9a-fA-F:.]+)]?:(\\d+)$");

        for (String hostPortPair : hostPortPairs) {
            Matcher matcher = hostPortPattern.matcher(hostPortPair);
            if (matcher.find()) {
                String host = matcher.group(1);
                int port = Integer.parseInt(matcher.group(2));
                hostAndPorts.add(new HostAndPort(host, port));
            } else {
                throw new IllegalArgumentException("Invalid host:port pair: " + hostPortPair);
            }
        }

        return hostAndPorts;
    }

    public static Set<String> convertHostAndPortsToStrings(Set<HostAndPort> hostAndPorts) {
        Set<String> stringSet = new HashSet<>();

        for (HostAndPort hostAndPort : hostAndPorts) {
            String host = hostAndPort.getHost();
            int port = hostAndPort.getPort();
            String address;

            if (host.contains(":")) { // IPv6
                address = String.format("[%s]:%d", host, port);
            } else { // IPv4
                address = String.format("%s:%d", host, port);
            }

            stringSet.add(address);
        }

        return stringSet;
    }

}