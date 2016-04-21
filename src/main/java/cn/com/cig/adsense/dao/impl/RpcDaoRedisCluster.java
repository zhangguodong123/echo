package cn.com.cig.adsense.dao.impl;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import cn.com.cig.adsense.dao.cassandra.UserData;
import cn.com.cig.adsense.dao.cassandra.UserDataDao;
import cn.com.cig.adsense.dao.redis.RedisStoreManager;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.date.DateUtil;
import cn.com.cig.adsense.utils.json.JsonBinder;
import cn.com.cig.adsense.vo.DateEnum;
import cn.com.cig.adsense.vo.fix.Result;
import cn.com.cig.adsense.vo.fix.TagParse;
import cn.com.cig.adsense.vo.fix.TagParseResult;
import cn.com.cig.adsense.vo.fix.TagResult;
import cn.com.cig.adsense.vo.fix.Tags;
import cn.com.cig.adsense.vo.fix.UserResult;
import cn.com.cig.adsense.vo.fix.UserStruct;
/**   
 * @File: RpcDaoRedisCluster.java 
 * @Package cn.com.cig.adsense.dao.impl 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年7月21日 上午10:53:35 
 * @version V1.0   
 */
public class RpcDaoRedisCluster implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger logger = LoggerFactory.getLogger(RpcDaoRedisCluster.class);
	private static UserDataDao dao;
	private static RedisStoreManager redisManger;
	private static final JsonBinder binder = JsonBinder.buildNonNullBinder();
	public static KeyValueClient kv = Consul.builder().withUrl(Constant.CONSUL_URL).build().keyValueClient();
	// 单例
	private RpcDaoRedisCluster() {
	}

	private static class RpcDaoRedisClusterHolder {
		private static RpcDaoRedisCluster RpcDaoRedisCluster = new RpcDaoRedisCluster();
	}

	public static RpcDaoRedisCluster getInstance() {
		return RpcDaoRedisClusterHolder.RpcDaoRedisCluster;
	}
	
	static{
		dao = new UserDataDao();
		String redis_v2 = kv.getValueAsString(Constant.REDISV2_CONFIG_KEY).get();
		logger.info("redis_v2:"+redis_v2);
		init(redis_v2);
	}
	
	public List<Integer> getUrlTags(String url) {
		if(Constant.HEADERS_REFERER_SWITCH == 0){
			logger.info("The referer_url have closed //////////////");
			return null;
		}
		if ((url == null) || (url.length() == 0)) {
			logger.warn("Url is empty! url:" + url);
			return null;
		}
		List<Integer> result = new ArrayList<>();
		Jedis jedis = null;
		String sresult = null;
		try {
			jedis = redisManger.getConnection();
			String md5Url = getMD5Str(url);
			String key = md5Url + Constant.TAG_VERSION;
			sresult = jedis.get(key);
			TagResult tagsResult = null;
			if (sresult != null) {
				tagsResult = binder.fromJson(sresult, TagResult.class);
				Tags.Builder builder = new Tags.Builder();
				builder.result(Result.SUCCESS);
				if (tagsResult.getModel() != null) {
					TagParse model = new TagParse.Builder()
							.tagId(tagsResult.getModel().getTagId() % 100000)
							.score(tagsResult.getModel().getScore()).build();
					builder.model(model);
				}
				List<TagParseResult> parseResults = tagsResult.getOtherResult();
				List<TagParse> otherResult = new ArrayList<>(2);
				if (parseResults != null && parseResults.size() != 0) {
					for (TagParseResult otag : parseResults) {
						TagParse tag = new TagParse.Builder()
								.tagId(otag.getTagId()).score(otag.getScore())
								.build();
						otherResult.add(tag);
					}
					builder.otherResult(otherResult);
				}
				Tags tags = builder.build();
				if (tags.isSetModel()) {
					result.add(tags.getModel().getTagId());
				}
				if (tags.isSetOtherResult()) {
					Iterator<TagParse> iter = tags.getOtherResult().iterator();
					while (iter.hasNext()) {
						result.add(iter.next().getTagId());
					}
				}
			} else {
				logger.info("no result key:" + key + " url:" + url);
			}
		} catch (Exception e) {
			logger.error("Query url failed! url:" + url, e);
		} finally {
			if (jedis != null) {
				redisManger.closeConnection(jedis);
			}
		}
		return result;
	}
	
	public static void init(String info){
		Properties bundle = new Properties();
		StringReader reader = new StringReader(info);
		try {
			bundle.load(reader);
			JedisPoolConfig config = new JedisPoolConfig();
			config.setMaxIdle(Integer.valueOf(bundle.getProperty("redis.pool.maxIdle")));
			config.setTestOnBorrow(Boolean.valueOf(bundle.getProperty("redis.pool.testOnBorrow")));
			config.setTestOnReturn(Boolean.valueOf(bundle.getProperty("redis.pool.testOnReturn")));
			redisManger = new RedisStoreManager(config,bundle.getProperty("redis"));
			reader.close();
		} catch (IOException e) {
			logger.error("cookie error:", e);
		}
	}
	
	/*public List<Integer> getUrlTags(String url) {
		if ((url == null) || (url.length() == 0)) {
			logger.warn("Url is empty! url:" + url);
			return null;
		}
		List<Integer> result = new ArrayList<>();
		String sresult = null;
		try{
			String md5Url = getMD5Str(url);
			String key = md5Url + Constant.TAG_VERSION;
			sresult = jc.get(key);
			TagResult tagsResult = null;
			if (sresult != null) {
				tagsResult = binder.fromJson(sresult, TagResult.class);
				Tags.Builder builder = new Tags.Builder();
				builder.result(Result.SUCCESS);
				if (tagsResult.getModel() != null) {
					TagParse model = new TagParse.Builder()
							.tagId(tagsResult.getModel().getTagId() % 100000)
							.score(tagsResult.getModel().getScore()).build();
					builder.model(model);
				}
				List<TagParseResult> parseResults = tagsResult.getOtherResult();
				List<TagParse> otherResult = new ArrayList<>(2);
				if (parseResults != null && parseResults.size() != 0) {
					for (TagParseResult otag : parseResults) {
						TagParse tag = new TagParse.Builder()
								.tagId(otag.getTagId()).score(otag.getScore())
								.build();
						otherResult.add(tag);
					}
					builder.otherResult(otherResult);
				}
				Tags tags = builder.build();
				if (tags.isSetModel()) {
					result.add(tags.getModel().getTagId());
				}
				if (tags.isSetOtherResult()) {
					Iterator<TagParse> iter = tags.getOtherResult().iterator();
					while (iter.hasNext()) {
						result.add(iter.next().getTagId());
					}
				}
			} else {
				logger.info("no result key:" + key + " url:" + url);
			}
			
		}catch(Exception e){
			logger.error("Query url failed! url:" + url, e);
		}
		return result;
	}*/

	private static String getMD5Str(String str) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			logger.error("NoSuchAlgorithmException caught!", e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(
						Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}
		return md5StrBuff.toString();
	}

	
	public UserStruct getUserStruct(String cookie) {
		if ((cookie == null) || (cookie.length() == 0)) {
			return null;
		}
		try {
			UserData data = dao.selectOne(cookie, 300);
			if (data == null) {
				return null;
			}
			UserStruct.Builder builder = new UserStruct.Builder()
					.userId(cookie)
					.city((data.getCity() == null) ? 0 : data.getCity().intValue())
					.province((data.getProvince() == null) ? 0 : data.getProvince().intValue());
			if (data.getMsc() != null)
				builder.mtag(data.getMsc());

			if (data.getOsc() != null)
				builder.otag(data.getOsc());

			if (data.getLast_model() != null) {
				builder.lastModel(data.getLast_model());
			} else {
				builder.lastModel(0);
			}
			if (data.getCreated_time() != null) {
				builder.createTime(DateUtil.dateFormatText(DateEnum.FORMAT_DATE_0,data.getCreated_time()));
			} else {
				builder.createTime(DateUtil.dateFormatText(DateEnum.FORMAT_DATE_0,new Date()));
			}
			if (data.getLast_visited() != null) {
				builder.lastVisit(DateUtil.dateFormatText(DateEnum.FORMAT_DATE_0,data.getLast_visited()));
			} else {
				builder.lastVisit(DateUtil.dateFormatText(DateEnum.FORMAT_DATE_0,new Date()));
			}

			UserStruct struct = builder.build();
			UserResult result = new UserResult.Builder().result(Result.SUCCESS).user(struct).build();

			if (result.isSetUser()) {
				return result.getUser();
			}

		} catch (Exception e) {
			logger.error("Query cookie failed! cookie:" + cookie, e);
		}
		return null;
	}
	
	public void closeConnection(){
		/*//TODO try catch
		jc.close();*/
		if (redisManger != null) {
			redisManger.close();
		}
	}
	
}
