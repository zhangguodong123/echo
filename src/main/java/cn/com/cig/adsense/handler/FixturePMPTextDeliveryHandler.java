package cn.com.cig.adsense.handler;

import static cn.com.cig.adsense.general.CommonBiz.getClientIp;
import static cn.com.cig.adsense.general.CommonBiz.getCookieId;
import static cn.com.cig.adsense.general.CommonBiz.getUk;
import static cn.com.cig.adsense.general.CommonBiz.getUserStruct;
import static cn.com.cig.adsense.general.CommonBiz.limitedAdvisterFrequency;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.dao.redis.RedisMasterSlaveManager;
import cn.com.cig.adsense.general.FixtureBiz;
import cn.com.cig.adsense.service.BitautoMaterialLibraryService;
import cn.com.cig.adsense.service.impl.BitautoMaterialLibraryServiceImpl;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.MaterialUtils;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.utils.encode.EncodeUtil;
import cn.com.cig.adsense.vo.PlatefromEnum;
import cn.com.cig.adsense.vo.Region;
import cn.com.cig.adsense.vo.dyn.AdvisterFrequency;
import cn.com.cig.adsense.vo.fix.Advertiser;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.Campaign;
import cn.com.cig.adsense.vo.fix.Position;
import cn.com.cig.adsense.vo.fix.Status;
import cn.com.cig.adsense.vo.fix.UserStruct;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**   
 * @File: FixturePMPTextDeliveryHandler.java 
 * @Package cn.com.cig.adsense.handler 
 * @Description: 私有广告主精准广告文本处理类
 * @author zhangguodong   
 * @date 2015年9月23日 上午10:47:02 
 * @version V1.0   
 */
public class FixturePMPTextDeliveryHandler implements HttpHandler{
	private static Logger logger = LoggerFactory.getLogger(FixturePMPTextDeliveryHandler.class);
	private static final BitautoMaterialLibraryService mls = BitautoMaterialLibraryServiceImpl.getInstance();
	private HttpHandler textFixtureDelivery=new TextDeliveryHandler();
	private static final MaterialUtils materialUtils = MaterialUtils.getInstance();
	private static final Random random = new Random();
	private static final String chineseChar = "[\u4E00-\u9FA5]";
	private static final String fullWidthSymbols = "[\uFF00-\uFFFF\u3000-\u303F]";
	private static final String halfWidthSymbols = "[\u0000-\u00FF]";
	private static final Pattern chineseCharPattern = Pattern.compile(chineseChar);
	private static final Pattern fullWidthSymbolsPattern = Pattern.compile(fullWidthSymbols);
	private static final Pattern halfWidthSymbolsPattern = Pattern.compile(halfWidthSymbols);
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
			String positionId = positionIdDeque.peek();           //广告位ID
			Integer pid = Integer.valueOf(positionId);
			Position position = mls.getPositionById(positionId);  //获取媒体频道BEAN;
			if(position == null){
				logger.error("The pid:"+positionId+" position is error [error] [error]");
				exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			
			Integer advisterId = mls.getPMPAdvisterID(pid);//查找广告位关联pmp广告主ID;
			if(advisterId== null || advisterId ==0){
				logger.warn("The pid:"+positionId+" relate advisterID is null.....");
				textFixtureDelivery.handleRequest(exchange);
				return;
			}
			
			Advertiser advertiser = mls.getPMPAdvister(advisterId);//查找pmp广告主Bean;
			if(advertiser == null || advertiser.getId() == null){
				logger.warn("The pid:"+positionId+" of advertiser is null!");
				textFixtureDelivery.handleRequest(exchange);
				return;
			}
			
			String cookieId = getCookieId(exchange);
			List<AdvisterFrequency> userPVFrequency = instance.getUserPVFrequency(cookieId);//获取访客对广告的频次监控数据
			if(limitedAdvisterFrequency(userPVFrequency,advertiser)){//判断访客在该广告主是否已经达到→每天每访客限定曝光阀值，如果达到去投公投;
				logger.warn("The advister "+advertiser.getId()+" have over.");
				textFixtureDelivery.handleRequest(exchange);
				return;
			}
			
			String uk = getUk(exchange,cookieId);//获取同屏去重的唯一标识码
			String ip=getClientIp(exchange);
			Region region = Utils.getRegion(ip);
			UserStruct us = getUserStruct(cookieId);
			
			
			Campaign fixedPmp = mls.getDirectPMPCampain(advisterId,pid); // 定投PMP开启的广告计划;
			List<Campaign> unFixedPmp = advertiser.getUnDirectional();   //非定投PMP开启的广告计划;
			List<Campaign> unFixedUnPmp = advertiser.getGeneral();		 //非定投PMP关闭的广告计划;
			
			Collection<BitautoMaterial> materials = null;
			//1) 定投pmp开
			if(fixedPmp !=null){
				if(region !=null){
					materials=mls.getTextDirectPMPMaterialsBySizeAndRegion(fixedPmp,region.getProvince(), region.getCity());//投定投符合尺寸的地域素材
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);//频次监测
				}
				if(materials !=null && materials.size()>0){
					boolean flag=handlCookieDuplicateRegion(textFixtureDelivery,exchange,materials,uk,us,position);
					if(flag) {
						return;
					}else{
						materials=null;
					}
				}
				if(materials == null || materials.size() ==0){
					materials=mls.getTextDirectPMPDefaultMaterialsBySizeAndRegion(fixedPmp);
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);//频次监测
				}
				if(materials != null && materials.size() >0){
					materials = generateCreative(materials, position.getTitleMaxNum());
					Collection<BitautoMaterial> duplicateExclusion = materialUtils.removeDuplicate(uk,materials);
					if((duplicateExclusion != null) && (duplicateExclusion.size() >0)){
						returnMaterial(textFixtureDelivery,exchange, duplicateExclusion, position,Status.has_no_default.getIndex(),uk);
						return;
					}else{
						materials=null;
					}
				}
			}
			
			//2) 非定投pmp开
			if(materials == null || materials.size() == 0){
				if(unFixedPmp !=null && unFixedPmp.size()>0){
					if(region !=null){
						materials=mls.getTextUnDirectPMPMaterialsBySizeAndRegion(unFixedPmp, region.getProvince(), region.getCity());
					}
				}
				if(materials !=null && materials.size() >0){
					boolean flag=handlCookieDuplicateRegion(textFixtureDelivery,exchange,materials,uk,us,position);
					if(flag) {
						return;
					}else{
						materials=null;
					}
				}
				if(materials == null || materials.size() ==0){
					materials=mls.getTextUnDirectPMPDefaultMaterialsBySize(unFixedPmp);
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);//频次监测
				}
				if(materials != null && materials.size() >0){
					materials = generateCreative(materials, position.getTitleMaxNum());
					Collection<BitautoMaterial> duplicateExclusion = materialUtils.removeDuplicate(uk,materials);
					if((duplicateExclusion != null) && (duplicateExclusion.size() >0)){
						returnMaterial(textFixtureDelivery,exchange, duplicateExclusion, position,Status.has_no_default.getIndex(),uk);
						return;
					}else{
						materials=null;
					}
				}
			}
			
			//3) 非定投pmp关
			if(materials == null || materials.size() == 0){
				if(unFixedUnPmp !=null && unFixedUnPmp.size()>0){
					if(region !=null){
						materials=mls.getTextGeneralPMPMaterialsBySizeAndRegion(unFixedUnPmp,region.getProvince(), region.getCity());
						materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);//频次监测
					}
				}
				if(materials !=null && materials.size() >0){
					boolean flag=handlCookieDuplicateRegion(textFixtureDelivery,exchange,materials,uk,us,position);
					if(flag) {
						return;
					}else{
						materials=null;
					}
				}
				if(materials == null || materials.size() ==0){
					materials=mls.getTextGeneralPMPDefaultMaterialsBySize(unFixedUnPmp);
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);//频次监测
				}
			}
			returnMaterial(textFixtureDelivery,exchange, materials, position,Status.has_no_default.getIndex(),uk);
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}
	
	/**
	 * description:处理同屏去重cookie和符合[地域]素材
	 * @param delivery
	 * @param exchange
	 * @param userPVFrequency
	 * @param materials
	 * @param uk
	 * @param us
	 * @param position
	 * @return
	 */
	public boolean handlCookieDuplicateRegion(HttpHandler delivery,HttpServerExchange exchange,Collection<BitautoMaterial> materials,String uk,UserStruct us,Position position){
		materials=FixtureBiz.processCookieMaterials(exchange,materials,uk,us);
		if (materials != null && materials.size() > 0) {
			materials = generateCreative(materials, position.getTitleMaxNum());
			Collection<BitautoMaterial> duplicatedMaterials = materialUtils.removeDuplicate(uk,materials);
			if((duplicatedMaterials != null) && (duplicatedMaterials.size() >0)){
				returnMaterial(delivery,exchange, duplicatedMaterials, position,Status.has_no_default.getIndex(),null);
				return true;
			}else{
				materials = FixtureBiz.exclusionModelsFilter(materials);
				if(materials !=null && materials.size() >0){
					materials = generateCreative(materials, position.getTitleMaxNum());
					Collection<BitautoMaterial> duplicateExclusion = materialUtils.removeDuplicate(uk,materials);
					if((duplicateExclusion != null) && (duplicateExclusion.size() >0)){
						returnMaterial(delivery,exchange, duplicateExclusion, position,Status.has_no_default.getIndex(),null);
						return true;
					}
				}
			}
		}
		return false;
	}
	
	
	private static void returnMaterial(HttpHandler fixtureDelivery,HttpServerExchange exchange,
			Collection<BitautoMaterial> materials, Position position, int defalut_type, String uk) {
		
		if ((position == null) || (position.getId() == null) || ("".equals(position.getId()))) {
			logger.warn("Position is empty!");
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
		// 同屏去重(uk=null 表示已经去过重，不需要再去了。)
		if(uk != null){
			materials = materialUtils.removeDuplicate(uk, materials);
		}
		// 没有素材可用
		if ((materials == null) || (materials.size() == 0)) {
			logger.warn("All duplicate text materials, Return empty!");
			try {
				fixtureDelivery.handleRequest(exchange);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			return;
		}
		materials = generateCreative(materials, position.getTitleMaxNum());
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
		exchange.getResponseSender().send(urlReturned.toString(),Charset.forName(requestCharset));
		return;
	}
	
	/*
	 * 去重的时候，如果title需要截断，去重的时候按照截断后的内容去重；
	 * 防止截断前不一样，截断后一样的title投出来；看起来物料重复了，
	 * 实际上是两个物料，只是截断后的titile相同。
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
}
