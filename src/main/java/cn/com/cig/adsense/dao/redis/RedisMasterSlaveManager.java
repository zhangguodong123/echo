package cn.com.cig.adsense.dao.redis;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;

import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.json.JsonBinder;
import cn.com.cig.adsense.vo.dyn.AdvisterFrequency;
import cn.com.cig.adsense.vo.fix.NetAddress;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**   
 * @File: RedisMasterSlaveManager.java 
 * @Package cn.com.cig.adsense.dao.redis 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年10月21日 下午1:40:14 
 * @version V1.0   
 */
public class RedisMasterSlaveManager {
	private static Logger logger = LoggerFactory.getLogger(RedisMasterSlaveManager.class);
	private static JedisPool pool;
	private static final JsonBinder binder = JsonBinder.buildNonNullBinder();
	public static KeyValueClient kv = Consul.builder().withUrl(Constant.CONSUL_URL).build().keyValueClient();
	private static volatile RedisMasterSlaveManager instance = null;
	
	private  RedisMasterSlaveManager(){
		String redis_v3 = kv.getValueAsString(Constant.REDISV3_CONFIG_KEY).get();
		logger.info("redis_v3:"+redis_v3);
		init(redis_v3);
	}
	
	public static void init(String content){
		Properties prop = new Properties();
		StringReader reader = new StringReader(content);
		try {
			prop.load(reader);
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxIdle(Integer.valueOf(prop.getProperty("redis.pool.maxIdle")));
			config.setMaxTotal(Integer.valueOf(prop.getProperty("redis.pool.maxActive")));
			config.setMaxWaitMillis(Integer.valueOf(prop.getProperty("redis.pool.maxWait")));
			config.setTestOnBorrow(Boolean.valueOf(prop.getProperty("redis.pool.testOnBorrow")));
			config.setTestOnReturn(Boolean.valueOf(prop.getProperty("redis.pool.testOnReturn")));
			Map<String, NetAddress> initNetAddress = initNetAddress(prop.getProperty("redis"));
			NetAddress netAddress = initNetAddress.get(Constant.SLAVE);//读取从节点数据
			pool = new JedisPool(config,netAddress.getIp(), netAddress.getPort());
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally{
			if(reader != null){
				reader.close();
			}
		}
	}
	
	public static Map<String,NetAddress> initNetAddress(String redis){
		Map<String, NetAddress> addressMap=new HashMap<>();
		if(redis == null || "".equals(redis)){
			logger.error("the config of redis is empty....");
			return null;
		}
		String[] split = redis.split(";");
		for(int i=0;i<split.length;i++){
			NetAddress net=new NetAddress();
			String[] address = split[i].split(":");
			String key = address[0];
			String ip=address[1];
			int port=Integer.parseInt(address[2]);
			net.setIp(ip);
			net.setPort(port);
			addressMap.put(key, net);
		}
		return addressMap;
	}
	
	public List<AdvisterFrequency> getUserPVFrequency(String cookie) {
		if(Constant.VISITOR_FREQUENCY_SWITCH == 0){
			logger.info("The userPVFrequency have closed //////////////");
			return null;
		}
		if ((cookie == null) ||"".equals(cookie)||(cookie.length() == 0)) {
			logger.warn("cookie is empty!");
			return null;
		}
		List<AdvisterFrequency> advList = null;
		Jedis jedis=null;
		try{
			jedis = getConnection();
			if(jedis == null){
				logger.warn("Unable to get redis instance from pool xxxxxxxxx");
				return null;
			}
			//jedis.auth("jzgg2016");//测试开启权限
			jedis.select(10);//设置redis查询的数据序号
			jedis.getClient().setSoTimeout(2);
			jedis.getClient().setConnectionTimeout(10);
			String context =jedis.get(Constant.FREQUENCY_KEY+cookie);
			if(context == null || "".equals(context)){
				logger.warn("The cookieId:"+cookie+" is empty===========");
			}else{
				advList = binder.getMapper().readValue(context, new TypeReference<List<AdvisterFrequency>>(){});
			}
		}catch(Exception e){
			logger.error(e.getMessage(),e);
		}finally {
			if(jedis != null){
				jedis.close();
			}
        }  
		return advList;
	}
	
	
	private Jedis getConnection() {
		Jedis jedis = null;
		try {
			if(pool == null){
				return null;
			}
			jedis = pool.getResource();
		} catch (JedisConnectionException e) {
			logger.error("redis_pool is:"+e.getMessage(), e);
		}
		return jedis;
	}
	
	
	public static RedisMasterSlaveManager getInstance() {
		if(instance == null) {
			synchronized (RedisMasterSlaveManager.class) {
				if(instance == null) {
					instance = new RedisMasterSlaveManager();
				}
			}
		}
		return instance;
	}
	
	public void close(){
		pool.destroy();
		pool.close();
	}
}
