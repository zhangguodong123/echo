package cn.com.cig.adsense.handler;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import cn.com.cig.adsense.delivery.impl.SortMaterialsByModelFrequencyForBitauto;
import cn.com.cig.adsense.general.CommonBiz;
import cn.com.cig.adsense.service.BitautoMaterialLibraryService;
import cn.com.cig.adsense.service.DmpService;
import cn.com.cig.adsense.service.impl.BitautoMaterialLibraryServiceImpl;
import cn.com.cig.adsense.service.impl.DmpServiceImpl;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.vo.fix.BitautoFixtureDeliveryAttribute;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.Position;
import cn.com.cig.adsense.vo.fix.Status;
import cn.com.cig.adsense.vo.fix.UserStruct;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

public class UiInterfaceHandler implements HttpHandler {

	private static Logger logger = LoggerFactory.getLogger(UiInterfaceHandler.class);
	
	private static final BitautoMaterialLibraryService mls = BitautoMaterialLibraryServiceImpl.getInstance();
	private static final DmpService ds = DmpServiceImpl.getInstance();
	private static final SortMaterialsByModelFrequencyForBitauto sortHandler = new SortMaterialsByModelFrequencyForBitauto();
	
	private static final String dynamicInterface = "http://echo.adsense.cig.com.cn/bitai?pid=";
	
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		
		Deque<String> key = exchange.getQueryParameters().get(Constant.PARAM);
		if ((key == null) || (key.size() == 0)) {
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		
		String keyPeek = key.peek();
		if(keyPeek == null || !keyPeek.equals(Constant.KEY)){
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		// pid分两类：精准和动态
		List<Position> positions = mls.getAllPositions();
		List<Position> fixturePids = new ArrayList<Position>();
		List<Position> dynamicPids = new ArrayList<Position>();
		positions.stream().forEach(position -> {
			String positionId = position.getId();
			if(Constant.GROUPON_LIST.contains(positionId)){
				if(Constant.ADAID_AREAS.containsKey(positionId)){
					//加地域限制
					int[] locationArr = CommonBiz.getLocationAreas(exchange);
					if ((locationArr == null)|| (locationArr.length == 0)|| (locationArr.length >= 2 && locationArr[0] == 0 && locationArr[1] == 0)){
						//使用精准投放
						fixturePids.add(position);
					}else{
						int province = locationArr[0];
						int city = 0;
						if (locationArr.length >= 2) {
							city = locationArr[1];
						}
						List<String> areas = Constant.ADAID_AREAS.get(positionId);
						if(areas.contains(String.valueOf(province)) || areas.contains(String.valueOf(city))){
							//使用动态投放
							dynamicPids.add(position);
						}else{
							//使用精准投放
							fixturePids.add(position);
						}
					}
				}else{
					//使用动态投放，团购列表有，地域限制里没有该广告位ID
					dynamicPids.add(position);
				}
			} else if(Constant.BITAUTO_LIST.contains(positionId)|| Constant.ACTIVITY_LIST.contains(positionId)){
				//使用动态投放
				dynamicPids.add(position);
			} else {
				//使用精准投放
				fixturePids.add(position);
			}
		});
		
		// 根据cookie和地域，返回每个pid的可投放列表（最高权重，低权重的不管）
		Map<String, Cookie> cookies = exchange.getRequestCookies();
		Cookie idCookie = cookies.get(Constant.COOKIE_ID);//cookie_CIGDCID
		String cookieId = null;
		Deque<String> testCookieDeque = exchange.getQueryParameters().get("tcookie");//request_tcookie
		if ((testCookieDeque != null) && (testCookieDeque.size() > 0)) {
			if(idCookie != null){
				logger.debug("cookieId:" + idCookie.getValue());
			} else {
				logger.debug("cookieId:null");
			}
			cookieId = testCookieDeque.peek();
			logger.debug("test cookieId:" + cookieId);
		} else {
			if(idCookie != null){
				cookieId = idCookie.getValue();
			}
		}
		// 自己计算地域，根据地域投.
		String ip = Utils.getClientIpAddr(exchange);
		Deque<String> testIpDeque = exchange.getQueryParameters().get("tip");//request_tip
		if ((testIpDeque != null) && (testIpDeque.size() > 0)) {
			logger.debug("Client ip:" + ip);
			ip = testIpDeque.peek();
			logger.debug("test ip:" + ip);
		}
		
		//String ip = "114.251.131.6";
		int province = 0;
		int city = 0;
		int[] locationArr = Utils.getAreas(ip);
		if ((locationArr == null) || (locationArr.length == 0)) {
			logger.warn("Unrecognized IP! ip:" + ip);
		} else {
			province = locationArr[0];
			city = 0;
			if (locationArr.length >= 2) {
				city = locationArr[1];
			}
		}
		
		Map<Position, List<String>> result = new LinkedHashMap<Position, List<String>>();
		
		if(fixturePids.size() > 0){
			Map<String, Position> positionMap = new HashMap<String, Position>();
			for(Position position : fixturePids){
				positionMap.put(position.getId(), position);
			}
			
			String reffererUrl = null;
			// debug
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
			Map<String, Collection<BitautoMaterial>> fixtureResult = getFixtureResult(
					fixturePids, cookieId, province, city, reffererUrl);
			Iterator<Entry<String, Collection<BitautoMaterial>>> iter = fixtureResult.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, Collection<BitautoMaterial>> entry = iter.next();
				Collection<BitautoMaterial> materials = entry.getValue();
				if(materials != null){
					List<String> materialUrls = new ArrayList<String>(materials.size());
					for(BitautoMaterial material : materials){
						materialUrls.add(material.getUrl() + "|@|" + material.getLinkUrl());
					}
					result.put(positionMap.get(entry.getKey()), materialUrls);
				}
			}
		}
		
		if(dynamicPids.size() > 0){
			for(Position position : dynamicPids){
				// 挨个处理动态投放的广告位
				result.put(position, Arrays.asList(dynamicInterface + position.getId()));
			}
		}
		
		StringBuilder resultStr = new StringBuilder();
		Iterator<Entry<Position, List<String>>> iter = result.entrySet().iterator();
		while(iter.hasNext()){
			Entry<Position, List<String>> entry = iter.next();
			Position position = entry.getKey();
			List<String> materialUrls = entry.getValue();
			resultStr.append(position.getId());
			resultStr.append(",");
			resultStr.append(position.getMediaName() + "-" + position.getChannelName() + "-" + position.getName());
			resultStr.append(",");
			if(materialUrls.size() > 0){
				for(String materialUrl : materialUrls){
					resultStr.append(materialUrl);
					resultStr.append("|:|");
				}
			} else {
				logger.warn("Position " + position.getId() + " have not material!");
			}
			resultStr.append(";");
		}
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=utf-8");
		exchange.getResponseSender().send(resultStr.toString(),Charset.forName("UTF-8"));
	}

	private Map<String, Collection<BitautoMaterial>> getFixtureResult(List<Position> fixturePids, String cookieId,
			int province, int city, String reffererUrl) {

		Map<String, Collection<BitautoMaterial>> exchange = new LinkedHashMap<String, Collection<BitautoMaterial>>();
		if(cookieId==null || "".equals(cookieId)){
			logger.warn("cookieId is empty!");
		}
		if(fixturePids.size() > 0){
			for(Position position : fixturePids){
				String positionId = position.getId();
				// 挨个处理精准投放的广告位
				String size = mls.getPositionSizeById(positionId);
				if((size==null) || ("".equals(size))){
					logger.warn("Position " + position.getId() + " size is empty!");
					continue;
				}
				List<BitautoMaterial> materials = null;
				// 是否关联过地域
				boolean relatedRegion = mls.relatedRegionBySize(size);
				if(relatedRegion){
					materials = mls.getMaterialBySizeAndRegion(size, province, city);
					//关联过地域:根据地域未找到素材时，投默认素材。
					if ((materials == null) || (materials.size() == 0)) {
						handleDefaultMaterial(exchange, positionId, size,Status.no_region_materials.getIndex());
						continue;
					}
				} else {
					// 没有关联过地域:根据车型投所有素材（不包括默认）
					materials = mls.getMaterialBySize(size);
				}
				// 根据广告活动 定投设置 目标广告位 过滤不能在当前广告位投放的物料。
				if((materials!=null) && (materials.size()>0)){
					Stream<BitautoMaterial> stream = materials.stream();
					materials = stream.filter((material)->{
						Collection<Integer> places = material.getPlaces();
						if((places.size() == 1 && places.contains(0))||(places == null) || (places.size() == 0)){
							return true;
						} else {
							return places.contains(Integer.parseInt(positionId));
						}
					}).collect(Collectors.toList());
					stream.close();
				} 
				// 如果广告位关联活动(定投,1:1)，就只投这个活动下的物料。
				if ((materials != null) && (materials.size() > 0)) {
					Stream<BitautoMaterial> stream = materials.stream();
					List<BitautoMaterial> temp = stream
							.filter((material) -> {
								Collection<Integer> places = material.getPlaces();
								if ((places != null) && (places.size() >= 0)) {
									return places.contains(Integer.parseInt(positionId));
								}
								return false;
							}).collect(Collectors.toList());
					stream.close();
					if ((temp != null) && (temp.size() > 0)) {
						materials = temp;
					}
				}
				// 找不到匹配物料,返回默认物料(包含多个-随机选一个)
				if ((materials == null) || (materials.size() == 0)) {
					handleDefaultMaterial(exchange, positionId, size,Status.no_match_materials.getIndex());
					continue;
				}
				// cookie ub中保存以前的impression和click
				UserStruct us = ds.getUserStruct(cookieId);
				
				// 责任链模式（Chain of Responsibility）
				// http://zz563143188.iteye.com/blog/1847029
				BitautoFixtureDeliveryAttribute da = new BitautoFixtureDeliveryAttribute();
				if(us != null){
					da.setMaterials(materials);
					Map<Integer, Integer> tags = new HashMap<>();
					if(us.getMtag() != null){
						tags.putAll(us.getMtag());
					}
					if(us.getOtag() != null){
						tags.putAll(us.getOtag());
					}
					da.setTags(tags);
					//da.setUserBehaviorMap(userBehaviorMap);
					sortHandler.operator(da);
				}
				
				// 精确匹配过滤掉所有物料后，走内容定向，再过滤一遍materials。
				if ((da.getMaterials()==null) || (da.getMaterials().size()==0)) {
					List<Integer> urlTags = ds.getUrlTags(reffererUrl);
					if((urlTags!=null) && (urlTags.size()>0)){
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
				
				if ((da.getMaterials() != null) && (da.getMaterials().size() > 0)) {
					// da中剩下的,就是符合投放策略的素材.
					returnMaterial(exchange, da.getMaterials(), positionId,size,Status.has_no_default.getIndex());
					continue;
				} else {
					// 新cookie，根据地域选择素材后，直接投放，不过dmp了。
					// debug下，伪造cookieId方便测试，这个时候就不需要idCookie == null的判断了。
					if(cookieId == null || "".equals(cookieId)){
						if(relatedRegion){
							handleOnlyRegionMaterials(exchange, materials, positionId, size);
						}else {
							// 没关联地域,投默认素材。
							handleDefaultMaterial(exchange, positionId, size,Status.no_cookie_no_match_size_materials.getIndex());
						}
						continue;
					}
					// 老cookie
					// 未关联车型，不通过dmp,直接投放。
					boolean relatedModel = mls.relatedModel(materials);
					if (!relatedModel) {
						if(relatedRegion){
							handleOnlyRegionMaterials(exchange, materials, positionId, size);
						}else {
							// 没关联地域,投默认素材。
							handleDefaultMaterial(exchange, positionId, size,Status.no_relatedModel_no_relatedRegion_materials.getIndex());
						}
						continue;
					}
				}
				
			}
		} else {
			logger.warn("fixturePids is empty!");
		}
		return exchange;
	}
	
	private static void handleDefaultMaterial(final Map<String, Collection<BitautoMaterial>> exchange, final String positionId, final String size,int default_type) {
		// DMP中不存在的cookie返回默认素材
		List<BitautoMaterial> materials = mls.getDefaultMaterialBySize(size);
		if(exchange.containsKey(positionId)){
			logger.warn("Replace position materials.positionId:" + positionId);
		}
		exchange.put(positionId, materials);
//		returnMaterial(exchange, materials, positionId, size,default_type);
	}
	
	// 地域[1],兴趣[0]：投放时选择地域符合兴趣为空的素材投。
	private static void handleOnlyRegionMaterials(final Map<String, Collection<BitautoMaterial>> exchange, Collection<BitautoMaterial> materials, final String positionId, final String size){
		List<BitautoMaterial> onlyRegionMaterials = new ArrayList<>();
		Iterator<BitautoMaterial> iter = materials.iterator();
		while(iter.hasNext()){
			BitautoMaterial m = iter.next();
			if((m.getModels()==null) || (m.getModels().size()==0)){
				m.setRegionTarget(true);
				onlyRegionMaterials.add(m);
			}
		}
		if(onlyRegionMaterials.size() > 0){
			if(exchange.containsKey(positionId)){
				logger.warn("Replace position materials.positionId:" + positionId);
			}
			exchange.put(positionId, onlyRegionMaterials);
//			returnMaterial(exchange, onlyRegionMaterials, positionId, size,Status.has_no_default.getIndex());
		} else {
			handleDefaultMaterial(exchange, positionId, size,Status.no_cookie_materials.getIndex());
		}
	}
	
	private static void returnMaterial(Map<String, Collection<BitautoMaterial>> exchange,
			Collection<BitautoMaterial> materials, String positionId, String size,int defalut_type) {
		
		// 没有素材可用
		if ((materials == null) || (materials.size() == 0)|| (positionId == null) || ("".equals(positionId))) {
			logger.warn("Return materials is empty! positionId:" + positionId);
			return;
		}
		// 如果存在多个默认素材,根据之前浏览的素材,尽量投放不同的默认素材.
		// Material name 中包含&,说明是易车中间的八个广告位.
		List<BitautoMaterial> filtered = Lists.newArrayList(Iterables.filter(
				materials, new Predicate<BitautoMaterial>() {
					public boolean apply(BitautoMaterial m) {
						boolean result = (m.getName().contains(Constant.BITAUTO_SPECIAL_POSITION_SEPARATOR));//&
						return result;
					}
				}));
		if (filtered == null || filtered.size() == 0) {
			if(exchange.containsKey(positionId)){
				logger.warn("Replace position materials.positionId:" + positionId);
			}
			exchange.put(positionId, materials);
		} else {
			if(exchange.containsKey(positionId)){
				logger.warn("Replace position materials.positionId:" + positionId);
			}
			exchange.put(positionId, filtered);
		}
	}
	
}
