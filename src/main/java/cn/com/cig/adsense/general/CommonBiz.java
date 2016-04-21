package cn.com.cig.adsense.general;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.service.DmpService;
import cn.com.cig.adsense.service.impl.DmpServiceImpl;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.utils.date.SystemTimer;
import cn.com.cig.adsense.vo.dyn.AdvisterFrequency;
import cn.com.cig.adsense.vo.dyn.CampainFrequency;
import cn.com.cig.adsense.vo.fix.Advertiser;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.ModelMaterial;
import cn.com.cig.adsense.vo.fix.UserStruct;
/**
 * @File: Handler.java
 * @Package cn.com.cig.adsense.biz
 * @Description: TODO
 * @author zhangguodong
 * @date 2015年5月20日 下午1:52:06
 * @version V1.0
 */
public abstract class CommonBiz{
	private static Logger logger = LoggerFactory.getLogger(CommonBiz.class);
	private static final DmpService ds = DmpServiceImpl.getInstance();// 用户标签
	private static final Random random = new Random();
	
	/**
	 * @description:对list进行排序
	 * @param modelsKpi
	 */
	public static void sortModelsByWeigth(List<ModelMaterial> modelsKpi) {
		Collections.sort(modelsKpi, new Comparator<ModelMaterial>() {
		      @Override
		      public int compare(ModelMaterial o1, ModelMaterial o2) {
		    	double o1_weight = o1.getWeight();
		    	double o2_weight = o2.getWeight();
		    	if(o1_weight > o2_weight){
		    		return -1;
		    	}
		    	if(o1_weight == o2_weight){
		    		return 0;
		    	}
		    	return 1;
		    	
		      }
		    });
	}
	
	/**
	 * description:获取地区数组
	 * @param exchange
	 * @return
	 */
	public static int[] getLocationAreas(HttpServerExchange exchange){
		// 自己计算地域，根据地域投.
		String ip = Utils.getClientIpAddr(exchange);// get_IP
		Deque<String> testIpDeque = exchange.getQueryParameters().get("tip");// request_tip
		if ((testIpDeque != null) && (testIpDeque.size() > 0)) {
			logger.debug("Client ip:" + ip);
			ip = testIpDeque.peek();
			logger.debug("test ip:" + ip);
		}
		// String ip = "114.251.131.6";
		int[] locationArr = Utils.getAreas(ip);// get_Areas 格式:
		return locationArr;
	}
	
	/**
	 * @description:embed CookieID。
	 * @param exchange
	 */
	public static void embedCookie(HttpServerExchange exchange){
		Map<String, Cookie> cookies = exchange.getRequestCookies();
		Cookie idCookie = null;
		if (cookies.containsKey(Constant.COOKIE_ID)) {
			idCookie = exchange.getRequestCookies().get(Constant.COOKIE_ID);// cookie_CIGDCID
		}
		if ((idCookie == null) || (idCookie.getValue() == null)|| ("".equals(idCookie.getValue()))) {
			Utils.handleNewCookie(exchange);// create cookie
		}
	}
	/**
	 * @description:同屏去重标识符。
	 * @param exchange
	 * @param tCookieId
	 * @return
	 */
	public static String getUk(HttpServerExchange exchange,String tCookieId){
		Deque<String> ukDeque = exchange.getQueryParameters().get(Constant.UK);
		String uk = null;
		if ((ukDeque == null) || (ukDeque.size() == 0)) {
			if(tCookieId == null || "".equals(tCookieId)){
				uk = SystemTimer.currentTimeMillis() + ":" + random.nextInt(100);
				logger.warn("Gets uk is empty, use int. uk:" + uk);
			} else {
				uk = tCookieId;
			}
			logger.warn("Uk is empty! init uk:" + uk);
		} else {
			uk = ukDeque.peek();
			if((uk==null) || ("".equals(uk))){
				uk = SystemTimer.currentTimeMillis() + ":" + random.nextInt(1000);
				logger.warn("Gets uk is empty, use int. uk:" + uk);
			}
		}
		return uk;
	}
	
	/**
	 * 获取cookieID、没有测试cookieID，则取cookieID。
	 * @param exchange
	 * @return
	 */
	public static String getCookieId(HttpServerExchange exchange) {
		Map<String, Cookie> cookies = exchange.getRequestCookies();
		Cookie idCookie = cookies.get(Constant.COOKIE_ID);//cookie_CIGDCID
		Deque<String> testCookieDeque = exchange.getQueryParameters().get("tcookie");// request_tcookie
		String tcookieId = null;
		if ((testCookieDeque != null) && (testCookieDeque.size() > 0)) {
			String peek = testCookieDeque.peek();
			if(peek !=null && !"".equals(peek)){
				logger.debug("The test_cookie_id :" + peek);
				return peek;
			}
		}
		if(tcookieId == null){
			if(idCookie == null){
				logger.warn("The cookieId is null ##########");
				return tcookieId;
			}
			tcookieId=idCookie.getValue();
		}
		logger.debug("cookieId:" + tcookieId);
		return tcookieId;
	}
	
	/**
	 * @description:获取用户标签属性
	 * @param ds
	 * @param testCookieId
	 * @param idCookie
	 * @return
	 */
	public static UserStruct getUserStruct(String tCookieId) {
		UserStruct us = null;
		if (tCookieId == null || "".equals(tCookieId)) {
			logger.info("The cookie:"+tCookieId+" is no tagged ~~~~~~~~~~");
		} else {
			us = ds.getUserStruct(tCookieId);// request_tcookie
		}
		return us;
	}

	/**
	 * @description:获取用户标签频次排序值
	 * @param us
	 * @return
	 */
	public static Map<Integer, Integer> getUserModelFrequency(UserStruct us) {
		Map<Integer, Integer> modelFrequency = null;
		if (us != null) {
			modelFrequency = Utils.calculateModelFrequency(us.getMtag());
		}
		return modelFrequency;
	}

	/**
	 * @description 获取客户端IP
	 * @param exchange
	 * @return
	 */
	public static String getClientIp(HttpServerExchange exchange) {
		// 自己计算地域，根据地域投.
		String ip = Utils.getClientIpAddr(exchange);
		Deque<String> testIpDeque = exchange.getQueryParameters().get("tip");// request_tip
		if ((testIpDeque != null) && (testIpDeque.size() > 0)) {
			logger.debug("Client ip:" + ip);
			ip = testIpDeque.peek();
			logger.debug("test ip:" + ip);
		}
		return ip;
	}

	public static String getReffererTag(HttpServerExchange exchange) {
		String reffererUrl = null;
		// debug
		Deque<String> testRefferer = exchange.getQueryParameters().get("trefferer");// request_trefferer
		if ((testRefferer != null) && (testRefferer.size() > 0)) {
			reffererUrl = testRefferer.getFirst();
			logger.debug("Test refferer:" + reffererUrl);
			reffererUrl = testRefferer.peek();
		} else {
			HeaderValues referer = exchange.getRequestHeaders().get(Headers.REFERER);// Headers_REFERER
			if (referer != null) {
				reffererUrl = referer.getFirst();
			}
		}
		List<Integer> urlTags = ds.getUrlTags(reffererUrl);
		if (urlTags == null || urlTags.size() == 0) {
			logger.warn("Refferer is no tags");
			return null;
		}
		for (int i = 0; i < urlTags.size(); i++) {
			Integer value = urlTags.get(i);
			if (value > 10000) {
				return value.toString();
			}
		}
		return null;
	}
	
	/**
	 * description:频次监控
	 * @param userPVFrequency
	 * @param materials
	 * @return
	 */
	public static Collection<BitautoMaterial> limitedFrequency(List<AdvisterFrequency> userPVFrequency,Collection<BitautoMaterial> materials){
		if(materials == null){
			logger.error("The limitedFrequency userPVFrequency or materials is null....");
			return null;
		}
		if(userPVFrequency == null || userPVFrequency.size() ==0){
			return materials;
		}
		
		LinkedHashMap<Integer, BitautoMaterial> dataMap = new LinkedHashMap<Integer,BitautoMaterial>();
		Iterator<BitautoMaterial> fIt = materials.iterator();
		while(fIt.hasNext()){
			BitautoMaterial next = fIt.next();
			if(!dataMap.containsKey(next.getId())){
				dataMap.put(next.getId(), next);
			}
		}
		
		Map<Integer,BitautoMaterial> data=new LinkedHashMap<Integer,BitautoMaterial>();
		
		Iterator<BitautoMaterial> it = materials.iterator();
		while(it.hasNext()){
			BitautoMaterial bitautoMaterial = it.next();
			int advisterId = bitautoMaterial.getAdvisterId();
			int campaignId = bitautoMaterial.getCampaignId();
			Long advLimitVisitorDisplayDay = bitautoMaterial.getAdvLimitVisitorDisplayDay();
			Long campExposeVisitDayLimit = bitautoMaterial.getCampExposeVisitDayLimit();
			Long campExposeVisitHourLimit = bitautoMaterial.getCampExposeVisitHourLimit();
			for(int i=0;i<userPVFrequency.size();i++){
				AdvisterFrequency ads = userPVFrequency.get(i);
				int aId = ads.getId();
				Long visitorDisplayDay = ads.getLimitVisitorDisplayDay();
				List<CampainFrequency> campains = ads.getCampains();
				if(campains == null || campains.size() == 0){ //先匹配广告主ID
					if(aId == advisterId){
						if(advLimitVisitorDisplayDay != null && visitorDisplayDay!=null && advLimitVisitorDisplayDay >0 && visitorDisplayDay >= advLimitVisitorDisplayDay){
							data.put(bitautoMaterial.getId(), bitautoMaterial);
						}
					}
				}else{
					for(int j=0;j<campains.size();j++){
						CampainFrequency campainFrequency = campains.get(j);
						int cid = campainFrequency.getId();
						Long exposeVisitDay = campainFrequency.getExposeVisitDayLimit();
						Long exposeVisitHour = campainFrequency.getExposeVisitHourLimit();
						
						if(aId == advisterId){
							if(advLimitVisitorDisplayDay != null && visitorDisplayDay!=null && advLimitVisitorDisplayDay >0 && visitorDisplayDay >= advLimitVisitorDisplayDay){
								data.put(bitautoMaterial.getId(), bitautoMaterial);
							}
							if(cid==campaignId){
								if(campExposeVisitDayLimit != null && exposeVisitDay != null && campExposeVisitDayLimit > 0 && exposeVisitDay >= campExposeVisitDayLimit){
									data.put(bitautoMaterial.getId(), bitautoMaterial);
								}
								if(campExposeVisitHourLimit != null && exposeVisitHour != null && campExposeVisitHourLimit > 0 && exposeVisitHour>=campExposeVisitHourLimit){
									data.put(bitautoMaterial.getId(), bitautoMaterial);
								}
							}
						}
					}
				}
			}
		}
		
		Set<Integer> its = data.keySet();
		Iterator<Integer> iterator = its.iterator();
		while(iterator.hasNext()){
			Integer next = iterator.next();
			if(dataMap.containsKey(next)){
				dataMap.remove(next);
			}
		}
		return dataMap.values();
	}
	
	/**
	 * 描述：限定广告主频次
	 * @param userPVFrequency
	 * @param advertiser
	 * @return
	 */
	public static boolean limitedAdvisterFrequency(List<AdvisterFrequency> userPVFrequency, Advertiser advertiser) {
		// TODO 判断广告主是否超过设定限量
		if(advertiser == null || userPVFrequency ==null || userPVFrequency.size() == 0){
			return false;
		}
		Integer vId = advertiser.getId();
		long limitVisitorDisplayDay = advertiser.getLimitVisitorDisplayDay();
		if(limitVisitorDisplayDay == 0){
			return false;
		}
		AdvisterFrequency advisterFrequency = null;
		for(int i=0;i<userPVFrequency.size();i++){
			AdvisterFrequency advister = userPVFrequency.get(i);
			int id = advister.getId();
			if(id != 0 && id ==vId){
				advisterFrequency=advister;
				break;
			}
		}
		if(advisterFrequency == null || advisterFrequency.getLimitVisitorDisplayDay() ==0){
			return false;
		}
		if(limitVisitorDisplayDay > 0 && advisterFrequency.getLimitVisitorDisplayDay() >=limitVisitorDisplayDay){
			return true;
		}
		return false;
	}
}
