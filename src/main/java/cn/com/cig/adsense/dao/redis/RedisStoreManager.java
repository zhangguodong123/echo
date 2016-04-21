package cn.com.cig.adsense.dao.redis;



import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.utils.Constant;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;



public class RedisStoreManager {
	private Map<String, ConnectConfig> configs;
	private static final Logger logger= LoggerFactory.getLogger(RedisStoreManager.class);
	private JedisPool jedisPool;
	
	public RedisStoreManager(JedisPoolConfig jedisPoolConfig, String config) {
		generateConfigsMap(config);
		ConnectConfig def = configs.get(Constant.MASTER);
		jedisPool = new JedisPool(jedisPoolConfig, def.getIp(), def.getPort());	
	}
	
	
	public Map<String, ConnectConfig> getConfigs() {
		return configs;
	}

	public Jedis getConnection() {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
		} catch (JedisConnectionException e) {
			logger.error(e.getMessage(), e);
			if (null != jedis) {
		    	jedis.close();
		    }
		}
		return jedis;
	}
	
	public void closeConnection(Jedis jedis) {
		if (null != jedis) {
			try {
				jedis.close();
			} catch (Exception e) {
				logger.error("redis Datasource：" + e);//销毁连接  
			}
		}
	}
	/**
	 * 配置表设置def:ip:port;sal1:ip:port
	 * @author zhaoying
	 * @param String
	 * 根据给定参数生成redis连接列表
	 * */
	private void generateConfigsMap(String str) {
		configs = new HashMap<String, ConnectConfig>();
		String[] conns = str.split(";");
		for (String conn : conns) {
			ConnectConfig config = new ConnectConfig();
			String[] details = conn.split(":");
			config.setIp(details[1]);
			config.setPort(Integer.valueOf(details[2]));
			configs.put(details[0], config);
		}
	}
	private  class ConnectConfig{
		@Override
		public String toString() {
			return ip+":"+port;
		}
		
		private String ip;
		private int port;
		
		public String getIp() {
			return ip;
		}
		public void setIp(String ip) {
			this.ip = ip;
		}
		public int getPort() {
			return port;
		}
		public void setPort(int port) {
			this.port = port;
		}
	}

	public void close(){
		jedisPool.destroy();
	}
}
