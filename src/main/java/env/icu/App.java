package env.icu;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisSentinelPool;
import java.net.InetAddress;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class App {
    private static final String REDIS_HOST = "REDIS_HOST";
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
        String password = System.getenv(REDIS_PASSWORD);
        String arch = System.getenv(REDIS_ARCH);
        if (arch == null) {
            arch = "standalone";
        }

        switch (arch) {
            case "sentinel":
                Set<HostAndPort> s_nodes = parseHostsNameAndPorts(address);
                Set<String> sentinels = convertHostAndPortsToStrings(s_nodes);

                JedisSentinelPool sentinelPool = new JedisSentinelPool(master_name, sentinels);
                Jedis jedisFromSentinel = null;
                try {
                    logger.info("sentinal master: {}", sentinelPool.getCurrentHostMaster().toString());
                    jedisFromSentinel = sentinelPool.getResource();
                    if (password != null) {
                        jedisFromSentinel.auth(password);
                    }
                    for (int i = 1; i <= 1000; i++) {
                        String key = "key" + i;
                        jedisFromSentinel.get(key);
                    }

                    JedisPubSub jedisPubSub = new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            // Process the received message
                            System.out.println("Received message: " + message + " from channel: " + channel);
                        }

                        @Override
                        public void onSubscribe(String channel, int subscribedChannels) {
                            System.out.println("Subscribed to channel: " + channel);
                        }

                        @Override
                        public void onUnsubscribe(String channel, int subscribedChannels) {
                            System.out.println("Unsubscribed from channel: " + channel);
                        }
                    };

                    jedisFromSentinel.subscribe(jedisPubSub, "my-channel");
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

    public static Set<HostAndPort> parseHostsNameAndPorts(String addresses) {
        Set<HostAndPort> hostAndPorts = new HashSet<>();
        String[] hostPortPairs = addresses.split(",");
        Pattern hostPortPattern = Pattern.compile("^\\[?([0-9a-zA-Z\\-_.]+)]?(:\\d+)?$");

        for (String hostPortPair : hostPortPairs) {
            Matcher matcher = hostPortPattern.matcher(hostPortPair);
            if (matcher.find()) {
                String host = matcher.group(1);
                String portString = matcher.group(2);

                int port = 6379; // Default port number if not specified
                if (portString != null) {
                    port = Integer.parseInt(portString.substring(1)); // Remove the colon from the port string
                }

                try {
                    InetAddress[] inetAddresses = InetAddress.getAllByName(host);
                    for (InetAddress inetAddress : inetAddresses) {
                        hostAndPorts.add(new HostAndPort(inetAddress.getHostAddress(), port));
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unknown host: " + host);
                }
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
