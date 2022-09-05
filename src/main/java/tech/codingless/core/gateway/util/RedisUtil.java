package tech.codingless.core.gateway.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.Tuple;

public class RedisUtil {

	private static final Logger LOG = LoggerFactory.getLogger(RedisUtil.class);
	private static Map<String, Object> map = new HashMap<>();
	protected static JedisPool connectionPool;

	public static void init() {

		try {
			if (connectionPool != null) {
				LOG.info("Redis Connect Established, Skip Zk Conf");
				return;
			} 
			String host = null, port = null, pwd = null, db = null;
			int dbInteger = 0;
			  
			LOG.info("【配置Redis】");
			LOG.info("host:" + host);
			LOG.info("port:" + port);
			LOG.info("db:" + dbInteger);
			if (StringUtil.isNotEmpty(host) && StringUtil.isNotEmpty(port)) {
				JedisPoolConfig config = new JedisPoolConfig();
				config.setMaxIdle(50);
				config.setTestOnBorrow(true);
				connectionPool = new JedisPool(config, host, Integer.parseInt(port), Protocol.DEFAULT_TIMEOUT, pwd, dbInteger);
			} else {
				LOG.info("配置Redis失败");
			}
		} catch (Exception e) {
			e.printStackTrace();

			LOG.info("配置Redis失败");
		}

	}

	public static void init(String host, String port, String pwd, String db) {
		if (connectionPool != null) {
			LOG.info("Redis Connect Established, Skip Properties Conf");
			return;
		}
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxIdle(50);
		config.setTestOnBorrow(true);
		connectionPool = new JedisPool(config, host, Integer.parseInt(port), Protocol.DEFAULT_TIMEOUT, pwd, Integer.parseInt(db));
	}

	 

	public static String get(String key) {
		if (connectionPool == null) {
			return (String) map.get(key);
		}
		Jedis jedis = connectionPool.getResource();
		try {
			return jedis.get(key);
		} finally {
			jedis.close();
		}
	}

	/**
	 * 
	 * @param key
	 * @param seconds 秒
	 * @return
	 */
	public static boolean set(String key, String value, int seconds) {
		if (connectionPool == null) {
			map.put(key, value);
			return true;
		}
		Jedis jedis = connectionPool.getResource();
		try {
			jedis.setex(key, seconds, value);
			return true;
		} finally {
			jedis.close();
		}

	}

	public static boolean hmset(String key, Map<String, String> data) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				jedis.hmset(key, data);
				return true;
			} finally {
				jedis.close();
			}
		} else {
			map.put(key, data);
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> hgetAll(String key) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.hgetAll(key);
			} finally {
				jedis.close();
			}
		} else {
			return (Map<String, String>) map.get(key);
		}
	}

	@SuppressWarnings("unchecked")
	public static boolean hset(String key, String field, String value) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				jedis.hset(key, field, value);
				return true;
			} finally {
				jedis.close();
			}
		} else {
			Map<String, String> tmp = (Map<String, String>) map.get(key);
			if (tmp == null) {
				tmp = new HashMap<>();
				map.put(key, tmp);
			}
			tmp.put(field, value);
			return true;
		}
	}

	@SuppressWarnings("unchecked")
	public static String hget(String key, String filed) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.hget(key, filed);
			} finally {
				jedis.close();
			}
		} else {
			Map<String, String> tmp = (Map<String, String>) map.get(key);
			return tmp.get(filed);
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> hmget(String key, String... fields) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.hmget(key, fields);
			} finally {
				jedis.close();
			}
		} else {
			Map<String, String> tmp = (Map<String, String>) map.get(key);
			List<String> list = new ArrayList<>();
			for (String f : fields) {
				list.add(tmp.get(f));
			}
			return list;
		}
	}

	public static Object eval(String script, String... paremeters) {

		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.eval(script, paremeters.length, paremeters);
			} finally {
				jedis.close();
			}
		} else {
			return null;
		}
	}

	public static Long incrBy(String key, long i) {

		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.incrBy(key, i);
			} finally {
				jedis.close();
			}
		} else {
			if (map.containsKey(key)) {
				Long v = (Long) map.get(key);
				map.put(key, v + i);
				return v + i;
			} else {
				map.put(key, i);
				return i;
			}
		}
	}

	public static Long decrBy(String key, long i) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.decrBy(key, i);
			} finally {
				jedis.close();
			}
		} else {
			if (map.containsKey(key)) {
				Long v = (Long) map.get(key);
				map.put(key, v - i);
				return v - i;
			} else {
				map.put(key, -i);
				return -i;
			}
		}
	}

	public static Long del(String name) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.del(name);
			} finally {
				jedis.close();
			}
		} else {
			map.remove(name);
			return null;
		}
	}

	public static boolean set(String key, String value) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				jedis.set(key, value);
				return true;
			} finally {
				jedis.close();
			}
		} else {
			map.put(key, value);
			return true;
		}

	}

	public static long incr(String key, int seconds) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				long i = jedis.incr(key);
				if (i <= 1) {
					jedis.expire(key, seconds);
				}
				return i;
			} finally {
				jedis.close();
			}
		} else {
			Integer i = (Integer) map.get(key);
			if (i == null) {
				i = 1;
				map.put(key, i);
			} else {
				map.put(key, i + 1);
			}
			return i;
		}
	}

	public static Object getObj(String key) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.get(key);
			} finally {
				jedis.close();
			}
		} else {
			return map.get(key);
		}
	}

	/**
	 * 从列表尾部拿到一个数据
	 * 
	 * @param key
	 * @return
	 */
	public static String rpop(String key) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.rpop(key);
			} finally {
				jedis.close();
			}
		} else {
			return (String) map.get(key);
		}
	}

	public static Long hlen(String key) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.hlen(key);
			} finally {
				jedis.close();
			}
		} else {
			return null;
		}

	}

	public static Long llen(String key) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.llen(key);
			} finally {
				jedis.close();
			}
		} else {
			return null;
		}

	}

	public static Long hdel(String key, String... fields) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.hdel(key, fields);
			} finally {
				jedis.close();
			}
		} else {
			return null;
		}

	}

	public static long expire(String key, int seconds) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.expire(key, seconds);
			} finally {
				jedis.close();
			}
		}
		return 0;
	}

	public static List<String> mget(String[] keys) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.mget(keys);
			} finally {
				jedis.close();
			}
		} else {
			return null;
		}
	}

	public static Set<String> keys(String pattern) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.keys(pattern);
			} finally {
				jedis.close();
			}
		} else {
			return null;
		}
	}

	public static long zadd(String key, double score, String member) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.zadd(key, score, member);
			} finally {
				jedis.close();
			}
		} else {
			return 0;
		}
	}

	public static Set<Tuple> zrangeWithScores(String key, int start, int end) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.zrangeWithScores(key, start, end);
			} finally {
				jedis.close();
			}
		} else {
			return null;
		}
	}

	public static long zrem(String key, String... members) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.zrem(key, members);
			} finally {
				jedis.close();
			}
		} else {
			return 0;
		}
	}

	public static Double zscore(String key, String member) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.zscore(key, member);
			} finally {
				jedis.close();
			}
		} else {
			return null;
		}
	}

	public static Set<Tuple> zrangeByScoreWithScores(String key, double min, double max, int offset, int count) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.zrangeByScoreWithScores(key, min, max, offset, count);
			} finally {
				jedis.close();
			}
		} else {
			return null;
		}
	}

	/**
	 * 向一个列表尾部放入数据
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public static long rpush(String key, String... val) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.rpush(key, val);
			} finally {
				jedis.close();
			}
		} else {
			return 0;
		}
	}

	/**
	 * 向一个列表头部放入数据
	 * 
	 * @param key
	 * @param val
	 * @return
	 */
	public static long lpush(String key, String... val) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.lpush(key, val);
			} finally {
				jedis.close();
			}
		} else {
			return 0;
		}
	}

	/**
	 * 从列表头部拿到一个数据
	 * 
	 * @param key
	 * @return
	 */
	public static String lpop(String key) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.lpop(key);
			} finally {
				jedis.close();
			}
		} else {
			return (String) map.get(key);
		}
	}

	/**
	 * 获得TTL的时间
	 * @author 王鸿雁
	 * @param key
	 * @return
	 *
	 */
	public static Long ttl(String key) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try { 
				return jedis.ttl(key);
			} finally {
				jedis.close();
			}
		} else {
			return 0L;
		}
	}
	
	
	public static List<String> lrange(String key) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.lrange(key, 0, 1000);
			} finally {
				jedis.close();
			}
		} else {
			return null;
		}

	}
	
	
	public static Set<String> zrangeByScore(String key,double min,double max) {
		if (connectionPool != null) {

			Jedis jedis = connectionPool.getResource();
			try {
				return jedis.zrangeByScore(key, min, max); 
			} finally {
				jedis.close();
			}
		} else {
			return null;
		}

	}

}
