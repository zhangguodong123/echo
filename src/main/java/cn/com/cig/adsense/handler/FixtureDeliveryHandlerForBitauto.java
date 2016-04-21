package cn.com.cig.adsense.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

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
import cn.com.cig.adsense.vo.fix.BitautoFixtureDeliveryAttribute;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.Position;
import cn.com.cig.adsense.vo.fix.Status;
import cn.com.cig.adsense.vo.fix.UserStruct;
import static cn.com.cig.adsense.general.CommonBiz.getClientIp;
import static cn.com.cig.adsense.general.CommonBiz.getCookieId;
import static cn.com.cig.adsense.general.CommonBiz.getUk;
import static cn.com.cig.adsense.general.CommonBiz.getUserStruct;
import static cn.com.cig.adsense.general.CommonBiz.limitedFrequency;
import static cn.com.cig.adsense.general.FixtureBiz.getRefferer;
/*
 * 处理固定位投放请求
 */
public class FixtureDeliveryHandlerForBitauto implements HttpHandler {

	private static Logger logger = LoggerFactory.getLogger(FixtureDeliveryHandlerForBitauto.class);
	private static final Random random = new Random();
	private static final DmpService ds = DmpServiceImpl.getInstance();
	private static final BitautoMaterialLibraryService mls = BitautoMaterialLibraryServiceImpl.getInstance();
	private static final SortMaterialsByModelFrequencyForBitauto sortHandler = new SortMaterialsByModelFrequencyForBitauto();
	private static final MaterialUtils materialUtils = MaterialUtils.getInstance();
	private RedisMasterSlaveManager instance = RedisMasterSlaveManager.getInstance();//频次redis
	
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
			Position position = mls.getPositionById(positionId);// 文本广告用textDeliveryHandler处理。
			if(position == null){
				logger.error("The pid:"+positionId+" position is error [error] [error]");
				exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			String size = mls.getPositionSizeById(positionId);
			if((size==null) || ("".equals(size))){
				exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			//user id
			String cookieId = getCookieId(exchange);//获取Test cookie
			String uk = getUk(exchange,cookieId);//获取同屏去重的唯一标识码
			String ip=getClientIp(exchange);
			//region_id
			Region region = Utils.getRegion(ip);//String ip = "114.251.131.6";
			
			Collection<BitautoMaterial> materials = null;
			//1) 取符合[尺寸]和[地域]和[兴趣]的素材
			if(region != null ){
				materials = mls.getMaterialBySizeAndRegion(size, region.getProvince(), region.getCity());
			}
			//2) 取默认素材[地域][兴趣]都不符合的
			if(materials == null || materials.size() ==0){
				materials = mls.getDefaultMaterialBySize(size);//去默认中去取
			}
			
			//3) 频次限定
			List<AdvisterFrequency> userPVFrequency = instance.getUserPVFrequency(cookieId);
			if(materials != null && materials.size() >0){
				materials=selectedUserPVFrequency(userPVFrequency,materials);
			}
			
			//4) [cookie][内容定向]过滤
			UserStruct us = getUserStruct(cookieId);
			BitautoFixtureDeliveryAttribute da = cookieAndContentFilter(exchange,materials,us);//只投兴趣
			if (da != null && (da.getMaterials() != null) && (da.getMaterials().size() > 0)) {
				Collection<BitautoMaterial> duplicatedMaterials = materialUtils.removeDuplicate(uk, da.getMaterials());
				if((duplicatedMaterials != null) && (duplicatedMaterials.size() >0)){
					returnMaterial(exchange, duplicatedMaterials, position,size,Status.has_no_default.getIndex(), null,userPVFrequency);
					return;
				}
			}
			//5) [地域] 为null 或者[地域]不为null，没有[兴趣]的素材
			handleOnlyRegionMaterials(exchange, materials, position, size, uk,userPVFrequency);
		} catch(Exception e){
			logger.error(e.getMessage(), e);
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}
	
	private void handleDefaultMaterial(final HttpServerExchange exchange, final Position position, final String size,int default_type, final String uk, List<AdvisterFrequency> userPVFrequency) {
		// DMP中不存在的cookie返回默认素材
		Collection<BitautoMaterial> materials = mls.getDefaultMaterialBySize(size);
		//频次限定
		if(materials != null && materials.size() >0){
			materials=selectedUserPVFrequency(userPVFrequency,materials);
		}
		returnMaterial(exchange, materials, position, size,default_type, uk,userPVFrequency);
	}
	
	// 地域[1],兴趣[0]：投放时选择地域符合兴趣为空的素材投。
	private void handleOnlyRegionMaterials(final HttpServerExchange exchange, Collection<BitautoMaterial> materials, final Position position, final String size, final String uk, List<AdvisterFrequency> userPVFrequency){
		if(materials != null && materials.size() >0){
			Stream<BitautoMaterial> modelStream = materials.stream();//如果兴趣不为空，返回兴趣素材，前提条件是地域符合
			List<BitautoMaterial> filter = modelStream.filter(
					(material) -> {
						Collection<Integer> regions = material.getRegions();
						Collection<Integer> models = material.getModels();
						return  (regions != null && regions.size()>0) && (models ==null || models.size() == 0);
					}).collect(Collectors.toList());
			modelStream.close();
			materials=filter;
		}
		Collection<BitautoMaterial> removeDuplicatedMaterials = materialUtils.removeDuplicate(uk, materials);
		if((removeDuplicatedMaterials==null) || (removeDuplicatedMaterials.size()==0)){
			// 都是重复的，投非精准
			handleDefaultMaterial(exchange, position, size,Status.no_cookie_no_match_size_materials.getIndex(), uk,userPVFrequency);
		}else{
			returnMaterial(exchange, removeDuplicatedMaterials, position, size,Status.has_no_default.getIndex(), null,userPVFrequency);
		}
	}
	
	private static void returnMaterial(HttpServerExchange exchange,
			Collection<BitautoMaterial> materials, Position position, String size,int defalut_type, String uk, List<AdvisterFrequency> userPVFrequency) {
		
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
		//频次限定
		if(materials != null && materials.size() >0){
			materials=selectedUserPVFrequency(userPVFrequency,materials);
		}
		// 没有素材可用
		if ((materials == null) || (materials.size() == 0)) {
			logger.warn("Return materials is empty!");
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
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
			BitautoMaterial material = Utils.bitautoMaterialCopy(mls.getMaterialByName(size, materialName));
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
	 * @param us
	 * @return
	 */
	public BitautoFixtureDeliveryAttribute cookieAndContentFilter(HttpServerExchange exchange,Collection<BitautoMaterial> materials,UserStruct us){
		// 责任链模式（Chain of Responsibility）
		// http://zz563143188.iteye.com/blog/1847029
		BitautoFixtureDeliveryAttribute da = new BitautoFixtureDeliveryAttribute();
		if(us != null){
			if(materials != null && materials.size() >0){
				da.setMaterials(materials);
				Map<Integer, Integer> tags = new HashMap<>();
				if(us !=null && us.getMtag() != null){
					tags.putAll(us.getMtag());
				}
				if(us !=null && us.getOtag() != null){
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
