package cn.com.cig.adsense.dao.cassandra;

import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.utils.Constant;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ConstantReconnectionPolicy;
import com.datastax.driver.core.policies.DowngradingConsistencyRetryPolicy;
import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;

public class CassandraFactoryDynamically{
	private static Logger logger = LoggerFactory.getLogger(CassandraFactoryDynamically.class);
	private static  Cluster cluster;
	private static  String key_space;
	public static KeyValueClient kv = Consul.builder().withUrl(Constant.CONSUL_URL).build().keyValueClient();
	static{
		String cassandra =kv.getValueAsString(Constant.CASSANDRA_CONFIG_KEY).get();
		logger.info("cassandra:"+cassandra);
		init(cassandra);
	}
	/**
	 * 第一次调用getInstance()时将加载内部类HolderClass，
	 * 在该内部类中定义了一个static类型的变量instance，
	 * 此时会首先初始化这个成员变量，由Java虚拟机来保证其线程安全性
	 * @author zgd
	 * */
	private CassandraFactoryDynamically(){
	}
	
	private static class HolderClass {
		private final static CassandraFactoryDynamically instance = new CassandraFactoryDynamically();
	}

	public static CassandraFactoryDynamically Instance() {
		return HolderClass.instance;
	}
	
	public static void init(String configInfo) {
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
		
		key_space = bundle.getProperty("keyspace");
		String user = bundle.getProperty("user");
		String password = bundle.getProperty("password");
		cluster = nodes==null ? Cluster.builder()
				.addContactPoint(host)
				.withCredentials(user,password)
				.withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
				.withReconnectionPolicy(new ConstantReconnectionPolicy(100L))
				.build()
				:Cluster.builder()
				.withCredentials(user,password)
				.withRetryPolicy(DowngradingConsistencyRetryPolicy.INSTANCE)
				.withReconnectionPolicy(new ConstantReconnectionPolicy(100L))
				.addContactPoints(nodes)
				.build();
	}

	public Session getSession() {
		if(cluster == null || key_space == null || "".equals(key_space)){
			logger.warn("The cluster:"+cluster);
			return null;
		}
		return cluster.connect(key_space);
	}
	
	public void setTimeoutMillis(int timeMillis) {
		if(timeMillis<0||timeMillis>10000){
			timeMillis=10000;
		}
		cluster.getConfiguration().getSocketOptions().setConnectTimeoutMillis(timeMillis);
	}
	
	public void closeCluster() {
		cluster.close();
	}
}
