package cn.com.cig.adsense.step;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.dao.cassandra.CassandraFactoryDynamically;
import cn.com.cig.adsense.dao.impl.RpcDaoRedisCluster;
import cn.com.cig.adsense.dao.redis.RedisMasterSlaveManager;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.utils.consul.ConsulUtils;

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;

/**   
 * @File: Booter.java 
 * @Package cn.com.cig.adsense.biz 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月1日 下午5:47:58 
 * @version V1.0   
 */
public class Initialize {
	public static KeyValueClient kv = Consul.builder().withUrl(Constant.CONSUL_URL).build().keyValueClient();
	private static Logger logger = LoggerFactory.getLogger(Initialize.class);
	/**
	 * @description 初始化每台服务器的ID号。
	 * @param host
	 * @param port
	 */
	public static void startup(String host, int port){
		System.setProperty("java.net.preferIPv4Stack" , "true");
		String address = Utils.getAddress().toString();
		String[] addressArr = address.split("\\.");
		if(addressArr!=null && addressArr.length==4){
			String serverId = addressArr[2] + addressArr[3] + port;
			Constant.SERVER_ID = serverId;
			logger.info("Server id:" + Constant.SERVER_ID);
		} else {
			logger.error("Init server id failed! address:" + address);
			System.exit(1);
		}
		logger.info("listening " + host + ":" + port);
	}
	
	/**
	 * description:consul kv
	 * @param rootPath
	 * @param watchSeconds
	 */
	public static void consulKVListener(String rootPath,final int watchSeconds){
		try {
			KVCache nc = KVCache.newCache(kv, rootPath, watchSeconds);
			nc.addListener(new ConsulCache.Listener<String, Value>() {
				public void notify(Map<String, Value> newValues) {
					Map<Integer, String> kvs = ConsulUtils.cacheHashKeyMap(newValues);
					if(kvs == null || kvs.size() == 0){
						logger.error("newValues: is empty >>>>>【error】>>>>>>>");
						return;
					}
					if(kvs.containsKey(Constant.ECHO_CONFIG_KEY.hashCode())){
						String config = kvs.get(Constant.ECHO_CONFIG_KEY.hashCode());
						logger.info("config:" + config);
						init(config);
					}
					if(kvs.containsKey(Constant.REDISV3_CONFIG_KEY.hashCode())){
						String redis_v3 = kvs.get(Constant.REDISV3_CONFIG_KEY.hashCode());
						logger.info("redis_v3:" + redis_v3);
						RedisMasterSlaveManager.init(redis_v3);
					}
					
					if(kvs.containsKey(Constant.REDISV2_CONFIG_KEY.hashCode())){
						String redis_v2 = kvs.get(Constant.REDISV2_CONFIG_KEY.hashCode());
						logger.info("redis_v2:" + redis_v2);
						RpcDaoRedisCluster.getInstance();
						RpcDaoRedisCluster.init(redis_v2);
					}
					
					if(kvs.containsKey(Constant.CASSANDRA_CONFIG_KEY.hashCode())){
						String cassandra = kvs.get(Constant.CASSANDRA_CONFIG_KEY.hashCode());
						logger.info("cassandra:" + cassandra);
						CassandraFactoryDynamically.init(cassandra);
					}
				}
			});
			nc.start();
		} catch (Exception e) {
			logger.error("Error:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @description 初始化Constant
	 * @param configInfo
	 */
	private static void init(String configInfo){
		Properties prop = new Properties();
		StringReader reader = new StringReader(configInfo);
		try {
			prop.load(reader);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		Constant.MQ_KEY = prop.getProperty("mqKey");
		// for cig
		Constant.DB_CONNECTION = "jdbc:mysql://" + prop.getProperty("db.connection");
		Constant.DB_USER = prop.getProperty("db.user");
		Constant.DB_PASSWORD = prop.getProperty("db.password");
		// for bitauto
		Constant.BITA_DB_CONNECTION = "jdbc:mysql://" + prop.getProperty("bita.db.connection");//正式环境
		Constant.BITA_DB_USER = prop.getProperty("bita.db.user");
		Constant.BITA_DB_PASSWORD = prop.getProperty("bita.db.password");
		
		//for bitaiAuto interface of gouchePage
		Constant.BITA_DB_STATISTICS_CONNECTION = "jdbc:mysql://" + prop.getProperty("bita.db.statistics.connection");//正式环境
		Constant.BITA_DB_STATISTICS_USER = prop.getProperty("bita.db.statistics.user");
		Constant.BITA_DB_STATISTICS_PASSWORD = prop.getProperty("bita.db.statistics.password");
		
		// for model pic
		Constant.MODEL_PIC_DB_CONNECTION = "jdbc:mysql://" + prop.getProperty("model.pic.db.connection");
		Constant.MODEL_PIC_DB_USER = prop.getProperty("model.pic.db.user");
		Constant.MODEL_PIC_DB_PASSWORD = prop.getProperty("model.pic.db.password");
		String temp = prop.getProperty("request.log.path.prefix");
		if(!temp.endsWith("/")){
			temp = temp + "/";
		}
		Constant.REQUEST_LOG_PATH_PREFIX = temp;
		File requestLogs = new File(Constant.REQUEST_LOG_PATH_PREFIX);
		if(!requestLogs.isDirectory()){
			requestLogs.mkdirs();
		}
		temp = prop.getProperty("access.log.path.prefix");
		Constant.ACCESS_LOG_PATH_PREFIX = temp;
		File accessLogs = new File(Constant.ACCESS_LOG_PATH_PREFIX);
		if(!accessLogs.isDirectory()){
			accessLogs.mkdirs();
		}
		// topN models(热门车型)
		temp = prop.getProperty("topn.models.folder");
		Constant.TOPN_MODELS_FOLDER = temp;
		// 竞品车型
		temp = prop.getProperty("competitive.models.folder");
		Constant.COMPETITIVE_MODELS_FOLDER = temp;
		// 易湃 易集客 地域表
		temp = prop.getProperty("yipai.region.code");
		Constant.YIPAI_REGION_CODE = temp;
		
		// 新意动态投放广告位id
		String cigPids = prop.getProperty("cig.dyn.pids");
		if (cigPids != null ) {
			Constant.CIG_LIST= Arrays.asList(cigPids.split(","));
		}
		//易车动态投放广告位id
		String bitaiPids = prop.getProperty("bitauto.dyn.pids");
		if (bitaiPids != null) {
			Constant.BITAUTO_LIST=Arrays.asList(bitaiPids.split(","));
		}
		//团购投放列表
		String grouponPids = prop.getProperty("groupon.dyn.pids").toString();
		if(grouponPids!=null){
			Constant.GROUPON_LIST=Arrays.asList(grouponPids.split(","));
		}
		//活动投放列表
		String activiePids = prop.getProperty("activity.dyn.pids").toString();
		if(activiePids!=null){
			Constant.ACTIVITY_LIST=Arrays.asList(activiePids.split(","));
		}
		//惠买车接口投放列表
		String hmcIPids = prop.getProperty("hmc.interface.pids").toString();
		if(hmcIPids!=null){
			Constant.HUIMAICHE_ILIST=Arrays.asList(hmcIPids.split(","));
		}
		//惠买车动态投放列表
		String hmcDynPids = prop.getProperty("hmc.dyn.pids").toString();
		if(hmcDynPids!=null){
			Constant.HUIMAICHE_DLIST=Arrays.asList(hmcDynPids.split(","));
		}
		//二手车车动态投放列表
		String usedCarDynPids = prop.getProperty("usedCar.dyn.pids").toString();
		if(usedCarDynPids!=null ){
			Constant.USEDCAR_LIST=Arrays.asList(usedCarDynPids.split(","));
		}
		//易鑫接口投放列表
		String yixinIPids = prop.getProperty("yx.interface.pids").toString();
		if(yixinIPids!=null ){
			Constant.YX_ILIST=Arrays.asList(yixinIPids.split(","));
		}
		//易鑫动态投放列表
		String yixinDynPids = prop.getProperty("yx.dyn.pids").toString();
		if(yixinDynPids!=null){
			Constant.YX_DLIST=Arrays.asList(yixinDynPids.split(","));
		}
		//易车惠动态投放列表
		String ychDynPids = prop.getProperty("ych.dyn.pids").toString();
		if(ychDynPids!=null ){
			Constant.YCH_LIST=Arrays.asList(ychDynPids.split(","));
		}
		//初始化惠买车广告位列表商品源one
		String hmc_one_pids = prop.getProperty("hmc.dyn.pids.one").toString();
		if(hmc_one_pids!=null){
			Constant.HMC_ONE_LIST=Arrays.asList(hmc_one_pids.split(","));
		}
		//初始化惠买车广告位列表商品源two
		String hmc_two_pids = prop.getProperty("hmc.dyn.pids.two").toString();
		if(hmc_two_pids!=null){
			Constant.HMC_TWO_LIST=Arrays.asList(hmc_two_pids.split(","));
		}
		//初始化惠买车广告位列表商品源four
		String hmc_four_pids = prop.getProperty("hmc.dyn.pids.four").toString();
		if(hmc_four_pids!=null){
			Constant.HMC_FOUR_LIST=Arrays.asList(hmc_four_pids.split(","));
		}
		//初始化广告位地域列表
		String pidAreas = prop.getProperty("pid.areas").toString();
		if(pidAreas!=null ){
			if(!"".equals(pidAreas)){
				Constant.ADAID_AREAS.clear();
				StringTokenizer adsToken=new StringTokenizer(pidAreas, ">");
				while(adsToken.hasMoreElements()){
					String token = adsToken.nextToken();
					StringTokenizer pidToken=new StringTokenizer(token, ":");
					String pids = pidToken.nextToken();
					String areas = pidToken.nextToken();
					StringTokenizer areasToken=new StringTokenizer(areas, ",");
					List<String> list=new ArrayList<>();
					while(areasToken.hasMoreTokens()){
						list.add(areasToken.nextToken());
					}
					Constant.ADAID_AREAS.put(pids, list);
				}
			}else{
				Constant.ADAID_AREAS.clear();
			}
		}
		
		// 清理Headers_REFERER中监测代码的正则表达式(防止加了代码的url无法匹配，无法进行内容投放)
		String headersRefererCleanerRegex = prop.getProperty("headers.referer.cleaner");
		try {
			Pattern headersRefererCleaner = Pattern.compile(headersRefererCleanerRegex);
			Constant.HEADERS_REFERER_CLEANER = headersRefererCleaner;
			logger.info("Update headersRefererCleaner regex from "
					+ Constant.HEADERS_REFERER_CLEANER.pattern() + " to "
					+ headersRefererCleaner.pattern() + ".");
		} catch (PatternSyntaxException e) {
			logger.warn("Illegal headersRefererCleaner regex:"
					+ headersRefererCleanerRegex + ", use old cleaner regex:"
					+ Constant.HEADERS_REFERER_CLEANER, e);
		}
		String refererSwitch = prop.getProperty("headers.referer.switch");
		Constant.HEADERS_REFERER_SWITCH=Integer.parseInt(refererSwitch);
		String frequencySwitch = prop.getProperty("visitor.frequency.switch");
		Constant.VISITOR_FREQUENCY_SWITCH=Integer.parseInt(frequencySwitch);
	}
}
