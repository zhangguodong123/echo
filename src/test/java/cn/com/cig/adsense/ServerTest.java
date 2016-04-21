package cn.com.cig.adsense;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.consul.ConsulUtils;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import com.orbitz.consul.Consul;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;

/**   
 * @File: AppTest.java 
 * @Package cn.com.cig.adsense 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年2月26日 下午2:34:12 
 * @version V1.0   
 */
public class ServerTest {
	private static Logger logger = LoggerFactory.getLogger(ServerTest.class);
	
	private static Cluster cluster;
	private static String key_space;

	public static void main(String[] args) {
		consulKVListener(Constant.ECHO_ROOT,5);
	}
	
	public static void consulKVListener(String rootPath,final int watchSeconds){
		try {
			Consul consul = Consul.builder().withUrl(Constant.CONSUL_URL).build();
			KVCache nc = KVCache.newCache(consul.keyValueClient(), rootPath, watchSeconds);
			nc.addListener(new ConsulCache.Listener<String, Value>() {
				public void notify(Map<String, Value> newValues) {
					Map<Integer, String> kvs = ConsulUtils.cacheHashKeyMap(newValues);
					if(kvs == null || kvs.size() == 0 || !kvs.containsKey(Constant.CASSANDRA_CONFIG_KEY.hashCode())){
						logger.error("The key:"+Constant.CASSANDRA_CONFIG_KEY+" is empty >>>>>>>>>>>>");
						return;
					}
					String config = kvs.get(Constant.CASSANDRA_CONFIG_KEY.hashCode());
					logger.info("CASSANDRA_CONFIG_INFO:" + config);
					setupCassandra(config);
				}
			});
			nc.start();
		} catch (Exception e) {
			logger.error("Error:"+e.getMessage(),e);
		}
	}
	
	
	private static void setupCassandra(String configInfo) {
		Properties bundle = new Properties();
		StringReader reader = new StringReader(configInfo);
		try {
			bundle.load(reader);
		} catch (IOException e1) {
			logger.error(e1.getMessage(), e1);
		}
		String host = bundle.getProperty("host", "localhost");
		String hosts = bundle.getProperty("hosts");
		List<InetAddress> nodes=new ArrayList<>();
		
		String[] otherHosts = null;
		if (hosts != null) {
			otherHosts = hosts.split(",");
			for(int i=0;i<otherHosts.length;i++){
				try {
					InetAddress ihost = InetAddress.getByName(otherHosts[i]);
					nodes.add(ihost);
				} catch (UnknownHostException e) {
					logger.error("error initInetAddress");
				}
			}
		}
		
		System.out.println(nodes);
		
		key_space = bundle.getProperty("keyspace");
		//String user = bundle.getProperty("user");
		//String password = bundle.getProperty("password");
		cluster = nodes==null ? Cluster.builder()
				.addContactPoint(host)
				//.withCredentials(user,password)
				.withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
				.withReconnectionPolicy(new ConstantReconnectionPolicy(100L))
				.build()
				:Cluster.builder()
				//.withCredentials(user,password)
				.withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
				.withReconnectionPolicy(new ConstantReconnectionPolicy(100L))
				.addContactPoints(nodes)
				.build();
				
				cluster.connect(key_space);
	}

}
