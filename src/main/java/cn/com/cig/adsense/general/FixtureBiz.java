package cn.com.cig.adsense.general;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.delivery.impl.SortMaterialsByModelFrequencyForBitauto;
import cn.com.cig.adsense.service.DmpService;
import cn.com.cig.adsense.service.impl.DmpServiceImpl;
import cn.com.cig.adsense.service.impl.ModelMaterialServiceImpl;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.vo.Region;
import cn.com.cig.adsense.vo.dyn.AdvisterFrequency;
import cn.com.cig.adsense.vo.fix.BitautoFixtureDeliveryAttribute;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.Query;
import cn.com.cig.adsense.vo.fix.UserStruct;

/**   
 * @File: FixtureBiz.java 
 * @Package cn.com.cig.adsense.biz 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年9月10日 上午11:40:39 
 * @version V1.0   
 */
public class FixtureBiz extends CommonBiz{
	private static Logger logger = LoggerFactory.getLogger(CommonBiz.class);
	private static final DmpService ds = DmpServiceImpl.getInstance();
	private static final SortMaterialsByModelFrequencyForBitauto sortHandler = new SortMaterialsByModelFrequencyForBitauto();
	/* 1)  matchYipaiRegionsTask*/
	private static Map<Integer, Integer> matchAdsRegions = ModelMaterialServiceImpl.matchYipaiRegions();
	private static final Random random = new Random();
	
	/**
	 * 处理推荐系统过来的标签六位数的问题。
	 * @param query
	 * @return
	 */
	public static Query recodeQuery(Query query){
		Map<Integer, Integer> tags = query.getTags();
		if(query == null || query.getTags() == null || query.getTags().size() == 0){
			return query;
		}
		try{
			Map<Integer, Integer> newTags = new HashMap<>();
			Set<Integer> ks = tags.keySet();
			Iterator<Integer> it = ks.iterator();
			while(it.hasNext()){
				Integer next = it.next();
				String key = next.toString();
				char at = key.charAt(0);
				if(at == '3'){
					String newKey = key.substring(1,key.length());
					Integer parseInt = new Integer(newKey);
					newTags.put(parseInt, tags.get(next));
				}else{
					newTags.put(next, tags.get(next));
				}
			}
			query.setTags(newTags);
		}catch(Exception e){
			logger.error("parse number error:"+e.getMessage(),e);
		}
		return query;
	}
	
	/**
	 * description:ip,regionCode,
	 * @param exchange
	 * @return
	 */
	public static Region getRegionId(String ip,Integer cityId){
		Region region=null;
		if (ip != null && ip.length() >0){
			region = Utils.getRegion(ip);
		}
		if(region == null){
			if(cityId != null && cityId != 0){
				if(matchAdsRegions != null){
					boolean isProvince = matchAdsRegions.containsKey(cityId);
					if (isProvince) {
						region=new Region(matchAdsRegions.get(cityId));//涉及到一个转换为题
					}else{
						logger.warn("The matchAdsRegions cityId:"+cityId+"  is illegality #############");
					}
				}
			}
		}
		return region;
	}
	
	
	/**
	 * @description: APP一期生成输出最终结果
	 * @param exchange
	 * @param materials
	 * @param pid
	 * @param dvid
	 * @throws Exception 
	 */
	public static void returnMaterial(HttpHandler appDelivery,HttpServerExchange exchange,Collection<BitautoMaterial> materials,Integer pid,String dvid) throws Exception {
		if ((pid == null) || ("".equals(pid))) {
			logger.warn("pid is empty!");
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// 没有素材可用
		if ((materials == null) || (materials.size() == 0)) {
			logger.warn("Return materials is empty!");
			if(appDelivery != null){
				appDelivery.handleRequest(exchange);
				return;
			}
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String urlReturned = "";
		int randomIndex = random.nextInt(materials.size());
		// 随机出一个,避免老是看一个素材。
		BitautoMaterial sourceMaterial = null;
		int i = 0;
		Iterator<BitautoMaterial> iter = materials.iterator();
		while(iter.hasNext()){
			sourceMaterial = iter.next();
			if(i == randomIndex){
				break;
			}
			i++;
		}
		BitautoMaterial material = Utils.bitautoMaterialCopy(sourceMaterial);
		//TODO 输出接口问题
		urlReturned = Utils.generateDeviceCode(material, pid, dvid);
		if(urlReturned == null || "".equals(urlReturned)){
			logger.warn("generateDeviceCode is error!");
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json;charset=utf-8");
		exchange.getResponseSender().send(urlReturned.toString(),Charset.forName("UTF-8"));
		Utils.sendExposure(pid, material.getId(), dvid);
		return;
	}
	
	/**
	 * 内容定向获取 referer
	 * @param exchange
	 * @return
	 */
	public static String getRefferer(HttpServerExchange exchange){
		// debug
		String reffererUrl = null;
		Deque<String> testRefferer = exchange.getQueryParameters().get("trefferer");//request_trefferer
		if ((testRefferer != null) && (testRefferer.size() > 0)) {
			reffererUrl = testRefferer.getFirst();
			logger.debug("Test refferer:" + reffererUrl);
			reffererUrl = testRefferer.peek();
		} else {
			HeaderValues referer = exchange.getRequestHeaders().get(Headers.REFERER);//Headers_REFERER
			if(referer != null){
				reffererUrl = referer.getFirst();
			}
		}
		return reffererUrl;
	}
	
	/**
	 * 获取移动APPPost请求过来的数据 
	 * @param exchange
	 * @param limit
	 * @return
	 */
	public static String getPostString(HttpServerExchange exchange,int limit){
		if(limit <= 0){
			return null;
		}
	    ByteBuffer byteBuffer=ByteBuffer.allocate(limit);
	    try {
			exchange.getRequestChannel().read(byteBuffer);
		} catch (IOException e) {
			logger.error("e:"+e.getMessage(),e);
			return null;
		}
	    byteBuffer.rewind();
	    byte[] bytes = new byte[limit];
	    byteBuffer.get(bytes);
	    String post = new String(bytes, Charset.forName("UTF-8") );
	    byteBuffer.clear();
	    return post;
	}
	
	
	/**
	 * 频次限定
	 * @param userPVFrequency
	 * @param mats
	 * @return
	 */
	public static Collection<BitautoMaterial> selectedUserPVFrequency(List<AdvisterFrequency> userPVFrequency, Collection<BitautoMaterial> mats){
		if(mats == null || mats.size() == 0){
			logger.info("selectedUserPVFrequency materials is empty!");
			return null;
		}
		if(userPVFrequency == null || userPVFrequency.size() == 0){
			logger.info("selectedUserPVFrequency userPVFrequency is empty!");
			return mats;
		}
		Collection<BitautoMaterial> materials=null;
		//频次限定
		try{
			materials=limitedFrequency(userPVFrequency,mats);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
		}
		return materials;
	}
	
	/**
	 * 过滤兴趣，过滤内容定向
	 * @param exchange
	 * @param materials
	 * @param tags2
	 * @return
	 */
	public static BitautoFixtureDeliveryAttribute cookieAndContentFilter(HttpServerExchange exchange,Collection<BitautoMaterial> materials,Map<Integer, Integer> us){
		// 责任链模式（Chain of Responsibility）
		// http://zz563143188.iteye.com/blog/1847029
		BitautoFixtureDeliveryAttribute da = new BitautoFixtureDeliveryAttribute();
		if(us != null && us.size() >0){
			if(materials != null && materials.size() >0){
				da.setMaterials(materials);
				da.setTags(us);
				sortHandler.operator(da);
			}
		}
		
		// 精确匹配过滤掉所有物料后，走内容定向，再过滤一遍materials。
		if ((da.getMaterials()==null) || (da.getMaterials().size()==0)) {
			String reffererUrl = getRefferer(exchange);
			List<Integer> urlTags = ds.getUrlTags(reffererUrl);
			if((urlTags!=null) && (urlTags.size()>0)){
				if(materials != null && materials.size() >0){
					da = new BitautoFixtureDeliveryAttribute();
					da.setMaterials(materials);
					Map<Integer, Integer> tags = new HashMap<>(Utils.getHashMapSize(urlTags.size()));
					for(int tagId : urlTags){
						tags.put(tagId, 1);
					}
					da.setTags(tags);
					sortHandler.operator(da);
					Collection<BitautoMaterial> ms = da.getMaterials();
					if ((ms != null) && (ms.size() > 0)) {
						for(BitautoMaterial m : ms){
							m.setContentTarget(true);
						}
					}
				}
			}
		}
		return da;
	}
	
	
	public static Collection<BitautoMaterial> processCookieMaterials(HttpServerExchange exchange,Collection<BitautoMaterial> materials,String uk,UserStruct us){
		if(materials != null && materials.size() >0){
			if(us != null){
				Map<Integer, Integer> tags = new HashMap<>();
				if(us.getMtag() != null){
					tags.putAll(us.getMtag());
				}
				if(us.getOtag() != null){
					tags.putAll(us.getOtag());
				}
				BitautoFixtureDeliveryAttribute da = cookieAndContentFilter(exchange,materials,tags);
				if (da != null && (da.getMaterials() != null) && (da.getMaterials().size() > 0)) {
					materials =da.getMaterials();
				}
			}else{
				materials=exclusionModelsFilter(materials);
			}
		}
		return materials;
	}
	
	
	/**
	 * 排除投放物料中兴趣部分
	 * @param cookieMt
	 * @param allMt
	 * @return
	 */
	
	public static Collection<BitautoMaterial> exclusionModelsFilter(Collection<BitautoMaterial> materials){
		if(materials == null || materials.size() ==0) {
			return materials;
		}
		Stream<BitautoMaterial> modelStream = materials.stream();//如果兴趣不为空，返回兴趣素材，前提条件是地域符合
		List<BitautoMaterial> filter = modelStream.filter(
				(material) -> {
					Collection<Integer> regions = material.getRegions();
					Collection<Integer> models = material.getModels();
					return  (regions != null && regions.size()>0) && (models ==null || models.size() == 0);
				}).collect(Collectors.toList());
		modelStream.close();
		return filter;
	}
	
	/**
	 * 过滤cookie和内容定向，如果cookie内容定向没有，则返回没有兴趣的素材列表
	 * @param exchange
	 * @param materials
	 * @param userPVFrequency
	 * @param tags
	 * @return
	 */
	public static Collection<BitautoMaterial> cookieFilter(HttpServerExchange exchange,Collection<BitautoMaterial> materials, Map<Integer, Integer> tags){
		BitautoFixtureDeliveryAttribute da = cookieAndContentFilter(exchange,materials,tags);
		if ((da.getMaterials() != null) && (da.getMaterials().size() > 0)) {
			materials=da.getMaterials();
		}else{
			if(materials !=null && materials.size()>0){
				Stream<BitautoMaterial> modelStream = materials.stream();//如果兴趣不为空，返回兴趣素材，前提条件是地域符合
				List<BitautoMaterial> filter = modelStream.filter(
						(material) -> {
							Collection<Integer> models = material.getModels();
							Collection<Integer> regions = material.getRegions();
							return (regions != null && regions.size()>0) && (models ==null || models.size() == 0);
						}).collect(Collectors.toList());
				modelStream.close();
				materials=filter;
			}
		}
		return materials;
	}
}
