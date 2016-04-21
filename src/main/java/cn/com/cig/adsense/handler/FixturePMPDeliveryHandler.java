package cn.com.cig.adsense.handler;

import static cn.com.cig.adsense.general.CommonBiz.getClientIp;
import static cn.com.cig.adsense.general.CommonBiz.getCookieId;
import static cn.com.cig.adsense.general.CommonBiz.getUk;
import static cn.com.cig.adsense.general.CommonBiz.getUserStruct;
import static cn.com.cig.adsense.general.CommonBiz.limitedAdvisterFrequency;
import static cn.com.cig.adsense.general.FixtureBiz.getRefferer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.dao.redis.RedisMasterSlaveManager;
import cn.com.cig.adsense.delivery.impl.SortMaterialsByModelFrequencyForBitauto;
import cn.com.cig.adsense.general.FixtureBiz;
import cn.com.cig.adsense.service.BitautoMaterialLibraryService;
import cn.com.cig.adsense.service.DmpService;
import cn.com.cig.adsense.service.impl.BitautoMaterialLibraryServiceImpl;
import cn.com.cig.adsense.service.impl.DmpServiceImpl;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.MaterialUtils;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.utils.encode.EncodeUtil;
import cn.com.cig.adsense.vo.PlatefromEnum;
import cn.com.cig.adsense.vo.Region;
import cn.com.cig.adsense.vo.dyn.AdvisterFrequency;
import cn.com.cig.adsense.vo.fix.Advertiser;
import cn.com.cig.adsense.vo.fix.BitautoFixtureDeliveryAttribute;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.Campaign;
import cn.com.cig.adsense.vo.fix.Position;
import cn.com.cig.adsense.vo.fix.Status;
import cn.com.cig.adsense.vo.fix.UserStruct;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**   
 * @File: PmpDynamicDeliveryHandler.java 
 * @Package cn.com.cig.adsense.handler 
 * @Description: 私有广告主精准广告图片处理类
 * @author zhangguodong   
 * @date 2015年9月23日 上午10:21:24 
 * @version V1.0   
 */
public class FixturePMPDeliveryHandler implements HttpHandler{
	private static Logger logger = LoggerFactory.getLogger(FixturePMPDeliveryHandler.class);
	private static final BitautoMaterialLibraryService mls = BitautoMaterialLibraryServiceImpl.getInstance();
	private HttpHandler fixtureDelivery=new FixtureDeliveryHandlerForBitauto();
	private static final MaterialUtils materialUtils = MaterialUtils.getInstance();
	private static final DmpService ds = DmpServiceImpl.getInstance();
	private static final SortMaterialsByModelFrequencyForBitauto sortHandler = new SortMaterialsByModelFrequencyForBitauto();
	private static final Random random = new Random();
	private RedisMasterSlaveManager instance = RedisMasterSlaveManager.getInstance();
	
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		Deque<String> positionIdDeque = exchange.getQueryParameters().get(Constant.POSITION_ID);//request_pid
		if ((positionIdDeque == null) || (positionIdDeque.size() == 0)) {
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);//404
			return;
		}
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		try{
			String positionId = positionIdDeque.peek();
			Position position = mls.getPositionById(positionId);
			if(position == null){
				logger.error("The pid:"+positionId+" position is error [error] [error]");
				exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			String size = mls.getPositionSizeById(positionId);      //频道广告位尺寸;
			if((size==null) || ("".equals(size))){
				exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			Integer pid = Integer.valueOf(positionId); 
			Integer advisterId = mls.getPMPAdvisterID(pid);         //查找广告位关联pmp广告主ID;
			if(advisterId== null || advisterId ==0){
				logger.warn("The pid:"+pid+" relate advisterID is null");
				fixtureDelivery.handleRequest(exchange);
				return;
			}
			Advertiser advertiser = mls.getPMPAdvister(advisterId); //查找pmp广告主Bean;
			if(advertiser == null || advertiser.getId() == null){
				logger.warn("The PID:"+pid+" 's of advertiser is null");
				fixtureDelivery.handleRequest(exchange);
				return;
			}
			
			String cookieId = getCookieId(exchange);           //cookieId:测试和实际的都可以出;
			//获取访客对广告的频次监控数据
			List<AdvisterFrequency> userPVFrequency = instance.getUserPVFrequency(cookieId);
			//判断访客在该广告主是否已经达到→每天每访客限定曝光阀值，如果达到去投公投;
			if(limitedAdvisterFrequency(userPVFrequency,advertiser)){
				logger.warn("The cookieID: "+cookieId+" have  over every visitor limited exposure advisterID:"+advisterId);
				fixtureDelivery.handleRequest(exchange);
				return;
			}
			String uk = getUk(exchange,cookieId);//获取同屏去重的唯一标识码;
			String ip=getClientIp(exchange);              //获取IP;String ip = "114.251.131.6";
			Region region = Utils.getRegion(ip);//String ip = "114.251.131.6";
			UserStruct us = getUserStruct(cookieId);
			
			Campaign fixedPmp = mls.getDirectPMPCampain(advisterId,pid);// 定投PMP开启的广告计划;
			List<Campaign> unFixedPmp = advertiser.getUnDirectional();  //非定投PMP开启的广告计划;
			List<Campaign> unFixedUnPmp = advertiser.getGeneral();		//非定投PMP关闭的广告计划;
			
			Collection<BitautoMaterial> materials = null;
			//1) 定投
			if(fixedPmp !=null){
				if(region !=null){
					materials=mls.getDirectPMPMaterialsBySizeAndRegion(fixedPmp,size, region.getProvince(), region.getCity());//投定投符合尺寸的地域素材
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);//频次监测
				}
				if(materials != null && materials.size() >0){
					boolean flag=handlCookieDuplicateRegion(exchange,materials,uk,us,position,size);
					if(flag) {
						return;
					}else{
						materials=null;
					}
				}
				if(materials==null || materials.size() ==0){
					materials=mls.getDirectPMPDefaultMaterialsBySizeAndRegion(fixedPmp,size);     //投放默认素材
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);//频次监测
				}
				if(materials != null && materials.size() >0){
					Collection<BitautoMaterial> duplicateExclusion = materialUtils.removeDuplicate(uk,materials);
					if(duplicateExclusion != null && duplicateExclusion.size() >0){
						returnMaterial(fixtureDelivery,exchange, duplicateExclusion, position, size,Status.has_no_default.getIndex(),null);
						return;
					}else{
						materials=null;
					}
				}
			}
			
			//2) 非定投pmp
			if(materials == null || materials.size() == 0){
				if(unFixedPmp != null && unFixedPmp.size() >0){
					if(region !=null){
						materials=mls.getUnDirectPMPMaterialsBySizeAndRegion(unFixedPmp,size, region.getProvince(), region.getCity());
						materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);//频次监测
					}
				}
				if(materials != null && materials.size() >0){
					boolean flag=handlCookieDuplicateRegion(exchange,materials,uk,us,position,size);
					if(flag) {
						return;
					}else{
						materials=null;
					}
				}
				if(materials==null || materials.size() ==0){
					materials=mls.getUnDirectPMPDefaultMaterialsBySize(unFixedPmp,size);
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);//频次监测
				}
				if(materials != null && materials.size() >0){
					Collection<BitautoMaterial> duplicateExclusion = materialUtils.removeDuplicate(uk,materials);
					if(duplicateExclusion != null && duplicateExclusion.size() >0){
						returnMaterial(fixtureDelivery,exchange, duplicateExclusion, position, size,Status.has_no_default.getIndex(),null);
						return;
					}else{
						materials=null;
					}
				}
			}
			
			//3)非定投非pmp
			if(materials == null || materials.size()== 0){
				if(unFixedUnPmp != null && unFixedUnPmp.size() >0){
					if(region !=null){
						materials=mls.getGeneralPMPMaterialsBySizeAndRegion(unFixedUnPmp,size, region.getProvince(), region.getCity());
						materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);//频次监测
					}
				}
				if(materials != null && materials.size() >0){
					boolean flag=handlCookieDuplicateRegion(exchange,materials,uk,us,position,size);
					if(flag) {
						return;
					}else{
						materials=null;
					}
				}
				if(materials==null || materials.size() ==0){
					materials=mls.getGeneralPMPDefaultMaterialsBySize(unFixedUnPmp,size);
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);//频次监测
				}
			}
			returnMaterial(fixtureDelivery,exchange, materials, position, size,Status.has_no_default.getIndex(),uk);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}
	
	
	/**
	 * description:处理同屏去重cookie和符合[地域]素材
	 * @param exchange
	 * @param userPVFrequency
	 * @param materials
	 * @param uk
	 * @param us
	 * @param position
	 * @param size
	 * @return
	 */
	public boolean handlCookieDuplicateRegion(HttpServerExchange exchange,Collection<BitautoMaterial> materials,String uk,UserStruct us,Position position,String size){
		materials=FixtureBiz.processCookieMaterials(exchange,materials,uk,us);
		if (materials != null && materials.size() > 0) {
			Collection<BitautoMaterial> duplicatedMaterials = materialUtils.removeDuplicate(uk,materials);
			if((duplicatedMaterials != null) && (duplicatedMaterials.size() >0)){
				returnMaterial(fixtureDelivery,exchange, duplicatedMaterials, position, size,Status.has_no_default.getIndex(),null);
				return true;
			}else{
				materials = FixtureBiz.exclusionModelsFilter(materials);
				if(materials !=null && materials.size() >0){
					Collection<BitautoMaterial> duplicateExclusion = materialUtils.removeDuplicate(uk,materials);
					if((duplicateExclusion != null) && (duplicateExclusion.size() >0)){
						returnMaterial(fixtureDelivery,exchange, duplicateExclusion, position, size,Status.has_no_default.getIndex(),null);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private static void returnMaterial(HttpHandler fixtureDelivery, HttpServerExchange exchange,
			Collection<BitautoMaterial> materials, Position position, String size,int defalut_type, String uk) {
		
		if ((position == null) || ("".equals(position))) {
			logger.warn("Position is empty!");
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String positionId = position.getId();
		if ((positionId == null) || ("".equals(positionId))) {
			logger.warn("positionId is empty!");
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// 没有素材可用
		if ((materials == null) || (materials.size() == 0)) {
			logger.warn("Return materials is empty!");
			try {
				fixtureDelivery.handleRequest(exchange);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			return;
		}
		// 如果存在多个默认素材,根据之前浏览的素材,尽量投放不同的默认素材.
		// Material name 中包含&,说明是易车中间的八个广告位.
		
		Stream<BitautoMaterial> stream = materials.stream();
		Collection<BitautoMaterial> filtered = stream
				.filter((material) -> {
					return material.getName().contains(Constant.BITAUTO_SPECIAL_POSITION_SEPARATOR);
				}).collect(Collectors.toList());
		stream.close();
		
		String urlReturned = "";
		List<BitautoMaterial> result = new ArrayList<BitautoMaterial>(2);
		// 是否易车的中间八个广告位
		// 应该分不同的接口，判断太恶心了。
		if (filtered == null || filtered.size() == 0) {
			// 同屏去重(uk=null 表示已经去过重，不需要再去了。)
			if(uk != null){
				materials = materialUtils.removeDuplicate(uk, materials);
			}
			// 没有素材可用
			if ((materials == null) || (materials.size() == 0)) {
				logger.warn("All duplicate materials, Return empty!");
				exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
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
			result.add(material);
		} else {
			// 同屏去重(uk=null 表示已经去过重，不需要再去了。)
			if(uk != null){
				filtered = materialUtils.removeDuplicate(uk, filtered);
			}
			// 没有素材可用
			if ((materials == null) || (materials.size() == 0)) {
				logger.warn("All duplicate materials, Return empty!");
				exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			// 鼠标放到广告位上弹出的素材
			int randomIndex = random.nextInt(filtered.size());
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
			
			BitautoMaterial popupMaterial = Utils.bitautoMaterialCopy(sourceMaterial);
			// 易车的中间八个广告位需要两个素材
			String materialName = popupMaterial.getName().split(Constant.BITAUTO_SPECIAL_POSITION_SEPARATOR)[0];
			// 首页展示的素材
			BitautoMaterial material = Utils.bitautoMaterialCopy(mls.getMaterialByName(size, materialName));//TODO 此处有一些问题，待解决 
			result.add(material);
			result.add(popupMaterial);
		}
		// 判断landingPage是手机还是pc
		if (position.getMediaType() == PlatefromEnum.WAP) {
			for (BitautoMaterial material : result) {
				String wapLinkUrl = material.getWapLinkUrl();
				if (wapLinkUrl != null && !"".equals(wapLinkUrl)) {
					material.setLinkUrl(wapLinkUrl);
				} else {
					logger.warn("Material have not wapLinkUrl! positionId:"
							+ position.getId() + " materialId:"
							+ material.getId());
				}
			}
		}
		urlReturned = Utils.createBitautoCallbackStr(positionId, result, defalut_type);
		// 根据REQUEST_CHARSET，对汉字进行转码。
		String requestCharset = Constant.DEFAULT_REQUEST_CHARSET;
		Deque<String> requestCharsetDeque = exchange.getQueryParameters().get(Constant.REQUEST_CHARSET);
		if(requestCharsetDeque!=null && requestCharsetDeque.size()>0){
			String requestCharsetTemp = requestCharsetDeque.peek();
			if(requestCharsetTemp!=null && requestCharsetTemp.length()>0){
				requestCharsetTemp = requestCharsetTemp.trim();
				if(requestCharsetTemp.equalsIgnoreCase("GBK") || requestCharsetTemp.equalsIgnoreCase("GB2312")){
					urlReturned = EncodeUtil.Unicode2GBK(urlReturned);
					requestCharset = requestCharsetTemp;
				}
			}
		}
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=utf-8");
		exchange.getResponseSender().send(urlReturned.toString(),Charset.forName(requestCharset));
		return;
	}
	
	/**
	 * 过滤兴趣，过滤内容定向
	 * @param exchange
	 * @param materials
	 * @param us
	 * @return
	 */
	public static BitautoFixtureDeliveryAttribute cookieAndContentFilter(HttpServerExchange exchange,Collection<BitautoMaterial> materials,UserStruct us){
		// 责任链模式（Chain of Responsibility）
		// http://zz563143188.iteye.com/blog/1847029
		BitautoFixtureDeliveryAttribute da = new BitautoFixtureDeliveryAttribute();
		if(us != null){
			if(materials != null && materials.size() >0){
				da.setMaterials(materials);
				Map<Integer, Integer> tags = new HashMap<>();
				if(us.getMtag() != null){
					tags.putAll(us.getMtag());
				}
				if(us.getOtag() != null){
					tags.putAll(us.getOtag());
				}
				da.setTags(tags);
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
}
