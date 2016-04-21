package cn.com.cig.adsense.handler;

import static cn.com.cig.adsense.general.CommonBiz.getClientIp;
import static cn.com.cig.adsense.general.CommonBiz.getCookieId;
import static cn.com.cig.adsense.general.CommonBiz.getUk;
import static cn.com.cig.adsense.general.CommonBiz.getUserStruct;
import static cn.com.cig.adsense.general.CommonBiz.limitedFrequency;
import static cn.com.cig.adsense.general.FixtureBiz.getRefferer;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class TextDeliveryHandler implements HttpHandler {

	private static Logger logger = LoggerFactory.getLogger(TextDeliveryHandler.class);
	
	private static final Random random = new Random();
	private static final DmpService ds = DmpServiceImpl.getInstance();
	private static final BitautoMaterialLibraryService mls = BitautoMaterialLibraryServiceImpl.getInstance();
	
	private static final SortMaterialsByModelFrequencyForBitauto sortHandler = new SortMaterialsByModelFrequencyForBitauto();
	
	private static final MaterialUtils materialUtils = MaterialUtils.getInstance();
	
	private static final String chineseChar = "[\u4E00-\u9FA5]";
	private static final String fullWidthSymbols = "[\uFF00-\uFFFF\u3000-\u303F]";
	private static final String halfWidthSymbols = "[\u0000-\u00FF]";
	private static final Pattern chineseCharPattern = Pattern.compile(chineseChar);
	private static final Pattern fullWidthSymbolsPattern = Pattern.compile(fullWidthSymbols);
	private static final Pattern halfWidthSymbolsPattern = Pattern.compile(halfWidthSymbols);
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
		// 下列操作异步执行,在worker线程池中执行.
		String positionId = positionIdDeque.peek();//广告位ID;
		Position position = mls.getPositionById(positionId);
		
		if(position == null){
			logger.error("The pid:"+positionId+" position is error [error] [error]");
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		String cookieId = getCookieId(exchange);         //获取Test cookie;
		
		List<AdvisterFrequency> userPVFrequency = instance.getUserPVFrequency(cookieId);//获取访客对广告的频次数据;
		String uk = getUk(exchange,cookieId);//获取同屏去重的唯一标识码;
		String ip=getClientIp(exchange);
		
		Region region = Utils.getRegion(ip);//String ip = "114.251.131.6";
		
		Collection<BitautoMaterial> materials =null;//查找精准素材;
		//1) 取符合[地域]和[兴趣]的素材
		if(region !=null){
			materials = mls.getMaterialByRegion(mls.getAllTextMaterial(), region.getProvince(), region.getCity());
		}
		//2) 如果地域不符合，则取默认素材
		if(materials == null || materials.size() ==0){
			materials = mls.getAllDefaultTextMaterial();
		}
		//3) 频次限定
		if(materials != null && materials.size() >0){
			materials=selectedUserPVFrequency(userPVFrequency,materials);
		}
		//4) [cookie][内容定向]
		UserStruct us = getUserStruct(cookieId);
		BitautoFixtureDeliveryAttribute da = cookieAndContentFilter(exchange,materials,us);
		if (da != null && (da.getMaterials() != null) && (da.getMaterials().size() > 0)) {
			materials = generateCreative(da.getMaterials(), position.getTitleMaxNum());
			Collection<BitautoMaterial> removeDuplicatedMaterials = materialUtils.removeDuplicate(uk,materials);
			if((removeDuplicatedMaterials != null) && (removeDuplicatedMaterials.size() >0)){
				returnMaterial(exchange, removeDuplicatedMaterials, position, Status.has_no_default.getIndex(),null,userPVFrequency);
				return;
			}
		}
		//5) [地域] 为null 或者[地域]不为null，没有[兴趣]的素材
		handleOnlyRegionMaterials(exchange, materials, position, uk,userPVFrequency);
	}
	
	// 地域[1],兴趣[0]：投放时选择地域符合兴趣为空的素材投。
	private void handleOnlyRegionMaterials(
			final HttpServerExchange exchange, Collection<BitautoMaterial> materials,
			final Position position, final String uk, List<AdvisterFrequency> userPVFrequency) {
			// 同屏去重(uk=null 表示已经去过重，不需要再去了。)
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
				materials = generateCreative(materials, position.getTitleMaxNum());
			}
			Collection<BitautoMaterial> removeDuplicatedMaterials = materialUtils.removeDuplicate(uk, materials);
			if((removeDuplicatedMaterials==null) || (removeDuplicatedMaterials.size()==0)){
				// 都是重复的，投非精准
				handleDefaultMaterial(exchange, position, Status.no_cookie_no_match_size_materials.getIndex(), uk,userPVFrequency);
			}else{
				returnMaterial(exchange, removeDuplicatedMaterials, position, Status.has_no_default.getIndex(), null,userPVFrequency);
			}
	}
	
	private void handleDefaultMaterial(final HttpServerExchange exchange, final Position position, int default_type, final String uk, List<AdvisterFrequency> userPVFrequency) {
		// DMP中不存在的cookie返回默认素材
		Collection<BitautoMaterial> materials = mls.getAllDefaultTextMaterial();
		materials = generateCreative(materials, position.getTitleMaxNum());
		//频次限定
		if(materials != null && materials.size() >0){
			materials=selectedUserPVFrequency(userPVFrequency,materials);
		}
		returnMaterial(exchange, materials, position, default_type, uk,userPVFrequency);
	}
	
	private static void returnMaterial(HttpServerExchange exchange,
			Collection<BitautoMaterial> materials, Position position, int defalut_type, String uk, List<AdvisterFrequency> userPVFrequency) {
		
		if ((position == null) || (position.getId() == null) || ("".equals(position.getId()))) {
			logger.warn("Position is empty!");
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// 没有素材可用
		if ((materials == null) || (materials.size() == 0)) {
			logger.warn("Return materials is empty!");
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// 同屏去重(uk=null 表示已经去过重，不需要再去了。)
		if(uk != null){
			materials = materialUtils.removeDuplicate(uk, materials);
		}
		
		//频次限定
		if(materials != null && materials.size() >0){
			materials=selectedUserPVFrequency(userPVFrequency,materials);
		}
		
		// 没有素材可用
		if ((materials == null) || (materials.size() == 0)) {
			logger.warn("All duplicate text materials, Return empty!");
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		String urlReturned = "";
		int randomIndex = random.nextInt(materials.size());
		// 随机出一个,避免老是看一个素材。
		// 不修改原始物料，复制一个新的。
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
		if (material == null) {
			logger.warn("Copy materials is empty! sourceMaterial id:" + sourceMaterial.getId());
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// 判断landingPage是手机还是pc
		if (position.getMediaType() == PlatefromEnum.WAP) {
			String wapLinkUrl = material.getWapLinkUrl();
			if (wapLinkUrl != null && !"".equals(wapLinkUrl)) {
				material.setLinkUrl(wapLinkUrl);
			} else {
				logger.warn("Material have not wapLinkUrl! positionId:"
						+ position.getId() + " materialId:"
						+ material.getId());
			}
		}
		// 文本广告需要截掉多余的字。全角标点符号算一个字，半角标点符号算半个字。
//		String title = limitTextlength(material.getTitle(), position.getTitleMaxNum());
//		material.setTitle(title);
		urlReturned = Utils.createBitautoCallbackStr(position.getId(), Arrays.asList(material),defalut_type);
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
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
		exchange.getResponseSender().send(urlReturned.toString(),
				Charset.forName(requestCharset));
		return;
		// 打印请求时间
//		String responsTime = rta.readAttribute(exchange);
//		System.out.println(responsTime);
		
	}
	
	private static String limitTextlength(String text, int length){
		// length在前台存的时候，汉字算一个字，字母算半个字。在这里处理的时候，汉字算两个字，字母算一个，所以length * 2。
		if(text==null || text.length()==0){
			logger.warn("LimitTextlength text is empty!");
			return "";
		}
		char[] textArr = text.toCharArray();
		int num = 0;
		StringBuilder result = new StringBuilder("");
		for(char c : textArr){
			String charStr = Character.toString(c);
			if(charStr==null || charStr.length()==0){
				continue;
			}
			Matcher chineseCharMatcher = chineseCharPattern.matcher(charStr);
			Matcher fullWidthSymbolsMatcher = fullWidthSymbolsPattern.matcher(charStr);
			Matcher halfWidthSymbolsMatcher = halfWidthSymbolsPattern.matcher(charStr);
			if((chineseCharMatcher.matches()) || (fullWidthSymbolsMatcher.matches())){
				num += 2;
			} else if(halfWidthSymbolsMatcher.matches()){
				num += 1;
			} else {
				// 无法识别的字符按照全角处理
				num += 2;
				logger.warn("Not recognized char:(" + c + ") " + (int)c);
			}
			if(num <= length * 2){
				result.append(c);
			} else {
				break;
			}
		}
		return result.toString();
	}
	
	/*
	 * 去重的时候，如果title需要截断，去重的时候按照截断后的内容去重；防止截断前不一样，截断后一样的title投出来；看起来物料重复了，实际上是两个物料，只是截断后的titile相同。
	 */
	private static Collection<BitautoMaterial> generateCreative(Collection<BitautoMaterial> materials, int titleMaxNum){
		if(materials==null || materials.size()==0){
			return null;
		}
		Collection<BitautoMaterial> newMaterials = new ArrayList<BitautoMaterial>(materials.size());
		for(BitautoMaterial sourceMaterial : materials){
			BitautoMaterial material = Utils.bitautoMaterialCopy(sourceMaterial);
			if (material == null) {
				logger.warn("Copy materials is empty! sourceMaterial id:" + sourceMaterial.getId());
				continue;
			}
			// 文本广告需要截掉多余的字。全角标点符号算一个字，半角标点符号算半个字。
			String title = limitTextlength(material.getTitle(), titleMaxNum);
			if(title.length() < material.getTitle().length()){
				material.setCreative(title);
				material.setTitle(title);
			}
			newMaterials.add(material);
		}
		return newMaterials;
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
			if(materials != null && materials.size() > 0){
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
