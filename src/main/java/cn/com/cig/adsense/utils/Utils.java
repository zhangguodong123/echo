package cn.com.cig.adsense.utils;

import static cn.com.cig.adsense.general.CommonBiz.getUk;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HeaderMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import net.sf.cglib.beans.BeanCopier;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cn.com.cig.adsense.utils.date.SystemTimer;
import cn.com.cig.adsense.utils.http.HttpUtils;
import cn.com.cig.adsense.vo.HttpMethodEnum;
import cn.com.cig.adsense.vo.Region;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.CityMapping;
import cn.com.cig.adsense.vo.fix.Feed;
import cn.com.cig.adsense.vo.fix.Material;
import cn.com.cig.adsense.vo.fix.ModelMaterial;
import cn.com.cig.adsense.vo.fix.OpenCity;
import cn.com.cig.adsense.vo.fix.Poster;
import cn.com.cig.ip.IPLocation;

public class Utils {

	private static Logger logger = LoggerFactory.getLogger(Utils.class);
	private static final Random random = new Random();
	private static final IPLocation ipLocation = IPLocation.getInstance();
	
	private static final BeanCopier bitautoMaterialCopier = BeanCopier.create(BitautoMaterial.class, BitautoMaterial.class, false);
	private static final BeanCopier modelMaterialCopier = BeanCopier.create(ModelMaterial.class, ModelMaterial.class, false);
	
	private static final String X_FORWARDED_FOR = "X-Forwarded-For";
	private static final String UNKNOWN = "unknown";
	// 监测代码模板
	private static final String impressionMonitorCodeTemplate = "http://i.ctags.cn/sy%s/pc%s/cr%s/tg%s/cptg%s/md%s?ord=%s&sid="+Constant.SERVER_ID;
	private static final String clickMonitorCodeTemplate = 		"http://c.ctags.cn/sy%s/pc%s/cr%s/cp%s/tg%s/cptg%s/md%s/sid"+Constant.SERVER_ID+"?%s";
	private static final String clickHuiMaiCheMonitorCodeTemplatePc ="http://c.ctags.cn/sy%s/pc%s/md%s?%s";
	private static final String clickHuiMaiCheMonitorCodeTemplateMobile ="http://c.ctags.cn/sy%s/pc%s/md%s?%s";
	private static final String dynamicDeliveryCallbackTemplate = "_cigcallback('|,||,||,||,|%s|,|%s|,|0|;||;|pid=%s&tg=&cptg=&isuc=&isar=&codetype=2&sid="+Constant.SERVER_ID+"')";
	private static final String clickAppCodeTemplate = "http://cl.ctags.cn/sy1/pc%s/mt%s/dv%s?%s";
	private static final String pvAppCodeTemplate = "http://i.ctags.cn/sy1/pc%s/mt%s/dv%s?ord=%s";
	
	private static final MaterialUtils materialUtils = MaterialUtils.getInstance();
	private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	public static String getClientIpAddr(HttpServerExchange exchange) {
		HeaderMap headMap = exchange.getRequestHeaders();
		String ip = headMap.getLast(X_FORWARDED_FOR);
//		if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
//			ip = headMap.getFirst(PROXY_CLIENT_IP);
//		}
//		if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
//			ip = headMap.getFirst(WL_PROXY_CLIENT_IP);
//		}
//		if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
//			ip = headMap.getFirst(HTTP_CLIENT_IP);
//		}
//		if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
//			ip = headMap.getFirst(HTTP_X_FORWARDED_FOR);
//		}
		if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
			InetSocketAddress peerAddress = (InetSocketAddress) exchange
					.getConnection().getPeerAddress();
			ip = peerAddress.getAddress().getHostAddress();
		}
		return ip;
	}

	/*
	 * cookie ID生成器
	 */
	public static String cookieIdGenerator() {
		// md5(服务器ip+随机数+时间)
		StringBuilder cookieIdStr = new StringBuilder("");
		InetAddress inetAddress = getAddress();
		String adress = null;
		if(inetAddress == null){
			adress = "127.0.0.1";
		} else {
			adress = inetAddress.getHostAddress();
		}
		cookieIdStr.append(adress);
		int randomInt = random.nextInt(10000);
		cookieIdStr.append(randomInt);
		cookieIdStr.append(SystemTimer.currentTimeMillis());
		String cookieId = DigestUtils.md5Hex(cookieIdStr.toString());
		return cookieId;
	}
	
	public static Map<Integer, Integer> calculateModelFrequency(Map<Integer, Integer> models) {
		if (models == null) {
			logger.warn("Models is null!");
			return new HashMap<>();
		}
//		Map<Integer, Integer> models = ud.getMsc();
		// 车型频次排序,高->低
		TreeMap<Integer, Integer> frequency = new ValueComparableMap<Integer, Integer>(Ordering.natural().reverse().nullsLast());
		if (models != null && models.size() > 0) {
			Iterator<Entry<Integer, Integer>> iter = models.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Integer, Integer> entry = iter.next();
				frequency.put(entry.getKey(), entry.getValue());
			}
		}
		return frequency;
	}
	
	/**
	 * Get host IP address
	 * 
	 * @return IP Address
	 */
	public static InetAddress getAddress() {
		try {
			for (Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces(); interfaces.hasMoreElements();) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (networkInterface.isLoopback()
						|| networkInterface.isVirtual()
						|| !networkInterface.isUp()) {
					continue;
				}
				Enumeration<InetAddress> addresses = networkInterface
						.getInetAddresses();
				if (addresses.hasMoreElements()) {
					return addresses.nextElement();
				}
			}
		} catch (SocketException e) {
			logger.warn("Error when getting host ip address: <{}>.",
					e);
		}
		return null;
	}
	
	public static int getHashMapSize(int size){
		//loadFactor的默认值为0.75，默认情况下，数组大小为16，可以放16*0.75=12个元素。
		if(size <= 0){
			throw new RuntimeException("Map size <= 0. size:" + size);
		}
		return (int) Math.ceil(size / 0.75);
	}
	
	public static int daysBetween(long now, long returnDate) {
		// 19700102
		if(returnDate <= 57600000){
			logger.warn("returnDate illegal! returnDate:" + returnDate);
			returnDate = SystemTimer.currentTimeMillis();
		}
		Calendar cNow = Calendar.getInstance();
		Calendar cReturnDate = Calendar.getInstance();
		cNow.setTimeInMillis(now);
		cReturnDate.setTimeInMillis(returnDate);
		setTimeToMidnight(cNow);
		setTimeToMidnight(cReturnDate);
		long todayMs = cNow.getTimeInMillis();
		long returnMs = cReturnDate.getTimeInMillis();
		long intervalMs = todayMs - returnMs;
		return millisecondsToDays(intervalMs);
	}

	private static int millisecondsToDays(long intervalMs) {
		return (int) (intervalMs / (1000 * 86400));
	}

	private static void setTimeToMidnight(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
	}
	
	public static int[] getAreas(String ip){
		// 省 市 区
		int[] locationArr = null;
		if(ip == null || "".equals(ip)) return null;
		try {
			locationArr = ipLocation.getAreas(ip);
		} catch (Exception e) {
			logger.error("Parse ip error! ip:" + ip, e);
		}
		return locationArr;
	}
	
	public static Region getRegion(String ip){
		// 省 市 区
		try {
			int[] locationArr = ipLocation.getAreas(ip);
			if(locationArr.length == 3 && (locationArr[0] != 0 || locationArr[1] != 0)){
				Region region=new Region();
				region.setProvince(locationArr[0]);
				region.setCity(locationArr[1]);
				region.setDistrict(locationArr[2]);
				return region;
			}
		} catch (Exception e) {
			logger.error("Parse ip error! ip:" + ip, e);
		}
		return null;
	}
	
	public static String createCigCallbackStr(List<Material> materials){
//		小图;弹出的大图;文字
//		id,url,landingPage;id,url,landingPage;text
		if((materials==null) || (materials.size()==0)){
			logger.warn("materials is empty! materials:" + materials);
			return "";
		}
		StringBuilder urlReturned = new StringBuilder("_ascallback('");
		String text = "";
		List<Integer> matchedModels = new ArrayList<>();
		Integer audienceId=null;
		for (int i = 0; i < materials.size(); i++){
			Material material = materials.get(i);
			if(i > 0){
				urlReturned.append("|;|");
			}
			urlReturned.append(material.getId() == null ? "" : material.getId());
			urlReturned.append("|,|");
			urlReturned.append(material.getUrl() == null ? "" : material.getUrl());
			urlReturned.append("|,|");
			urlReturned.append(material.getLandingPage() == null ? "" : material.getLandingPage());
			urlReturned.append("|,|");
			urlReturned.append(material.getMet_type());
			urlReturned.append("|,|");
			urlReturned.append(material.getWidth());
			urlReturned.append("|,|");
			urlReturned.append(material.getHeight());
			if ((text == null) || ("".equals(text))) {
				text = material.getText();
			}
			if(material.getMatchedTags() != null){
				matchedModels.addAll(material.getMatchedTags());
			}
			if(material.getAudienceId() != null){
				audienceId=material.getAudienceId();
			}
		}
		urlReturned.append("|;|");
		urlReturned.append(text == null ? "" : text);
		urlReturned.append("|;|pid=");
		urlReturned.append(materials.get(0).getPositionId());
		// &tg=,,,,,,
		String matchedModelsStr = "";
		if ((matchedModels != null) && (matchedModels.size() > 0)) {
			Stream<Integer> stream = matchedModels.stream();
			matchedModelsStr = stream
					.limit(Constant.MATCHED_TAGS_NUM_LIMIT)
					.filter(mm -> mm != null)
					.map((mm) -> mm.toString())
					.collect(Collectors.joining(","));
			stream.close();
		}
		urlReturned.append("&tg=");
		urlReturned.append(matchedModelsStr);
		urlReturned.append("&codetype=1");
		urlReturned.append("&au="+audienceId);
		urlReturned.append("&sid="+Constant.SERVER_ID+"')");
		return urlReturned.toString();
	}
	
	public static String createBitautoCallbackStr(String positionId, List<BitautoMaterial> materials, int defalut_type){
//		小图;弹出的大图;文字
//		id,url,landingPage;id,url,landingPage;text
		if((materials==null) || (materials.size()==0)){
			logger.warn("materials is empty! materials:" + materials);
			return "";
		}
		StringBuilder urlReturned = new StringBuilder("_cigcallback('");
		String title = "";
		List<Integer> matchedModels = new ArrayList<>();
		List<Integer> matchedCompetitiveModels = new ArrayList<>();
		boolean isContentTarget = false;
		boolean isRegionTarget = false;
		Integer audienceId=null;
		for (int i = 0; i < materials.size(); i++){
			BitautoMaterial material = materials.get(i);
			if(i > 0){
				urlReturned.append("|;|");
			}
			urlReturned.append(material.getId());
			urlReturned.append("|,|");
			urlReturned.append(material.getUrl() == null ? "" : material.getUrl());
			urlReturned.append("|,|");
			urlReturned.append(material.getLinkUrl() == null || "".equals(material.getLinkUrl()) ? (material.getWapLinkUrl() == null || "".equals(material.getWapLinkUrl()) ? "":material.getWapLinkUrl()): material.getLinkUrl());
			urlReturned.append("|,|");
			urlReturned.append(material.getMatType().getIndex());
			urlReturned.append("|,|");
			urlReturned.append(material.getWidth());
			urlReturned.append("|,|");
			urlReturned.append(material.getHeight());
			urlReturned.append("|,|");
			urlReturned.append(material.getCampaignId());
			if ((title == null) || ("".equals(title))) {
				title = material.getTitle();
			}
			if(material.getMatchedTags() != null){
				matchedModels.addAll(material.getMatchedTags());
			}
			if(material.getMatchedCompetitiveModels() != null){
				matchedCompetitiveModels.addAll(material.getMatchedCompetitiveModels());
			}
			if(material.isContentTarget()){
				isContentTarget = true;
			}
			if(material.isRegionTarget()){
				isRegionTarget = true;
			}
			if(material.getAudienceId() != null){
				audienceId=material.getAudienceId();
			}
		}
		urlReturned.append("|;|");
		urlReturned.append(title == null ? "" : title);
		urlReturned.append("|;|pid=");
		urlReturned.append(positionId);
		// &tg=,,,,,,
		String matchedModelsStr = "";
		if((matchedModels!=null) && (matchedModels.size()>0)){
			Stream<Integer> stream = matchedModels.stream();
			matchedModelsStr = stream.distinct()
					.limit(Constant.MATCHED_TAGS_NUM_LIMIT)
					.filter(mm -> mm != null)
					.map((mm) -> mm.toString())
					.collect(Collectors.joining(","));
			stream.close();
		}
		urlReturned.append("&tg=");
		urlReturned.append(matchedModelsStr);
		// 竞品是否匹配
		String matchedCompetitiveModelsStr = "";
		if((matchedCompetitiveModels!=null) && (matchedCompetitiveModels.size()>0)){
			Stream<Integer> stream = matchedCompetitiveModels.stream();
			matchedCompetitiveModelsStr = stream.distinct()
					.limit(Constant.MATCHED_TAGS_NUM_LIMIT)
					.filter(mm -> mm != null)
					.map((mm) -> mm.toString())
					.collect(Collectors.joining(","));
			stream.close();
		}
		urlReturned.append("&cptg=");
		urlReturned.append(matchedCompetitiveModelsStr);
		// 是否按内容定向
		urlReturned.append("&isuc=");
		if(isContentTarget){
			urlReturned.append("1");
		} else {
			urlReturned.append("0");
		}
		// 是否按地域定向
		urlReturned.append("&isar=");
		if (isRegionTarget) {
			urlReturned.append("1");
		} else {
			urlReturned.append("0");
		}
		urlReturned.append("&codetype=1");
		urlReturned.append("&default_type="+defalut_type);
		urlReturned.append("&au="+audienceId);
		urlReturned.append("&sid="+Constant.SERVER_ID+"')");
		return urlReturned.toString();
	}
	
	public static String createDynamicDeliveryCallbackStr(String positionId, int width, int height) {
		return String.format(dynamicDeliveryCallbackTemplate, width, height, positionId);
	}
	
//	以下维度，可以在代码中任意组合。
//	维度	英文	曝光标识	点击标识	备注（ID仅支持数字）
//	系统	system	sy	sy	0：adsense，1易车精准，3：smartAD
//	客户、广告商	customer、advertiser	cu	cu	客户ID
//	项目	project	pj	pj	项目ID
//	排期（活动）	plan、campaign	pn	pn	排期ID
//	广告位	place	pc	pc	广告位ID
//	素材	meterial	mt	mt	素材ID
//	车系、车型	model	md	md	车系ID
//	车款	style	st	st	车款ID
	// pageSize:3.每个广告位显示三个车型，那么每三个车型使用一个曝光监测代码。点击代码都是不同的。
	public static List<ModelMaterial> generateMonitorCode(String system,String positionId, Collection<ModelMaterial> sourceModelMaterials, int pageSize, boolean... isMall) {
		// 系统 system sy sy 0：adsense，1易车精准，3：smartAD
		List<ModelMaterial> result=new ArrayList<>();
		int sy = -1;
		switch (system) {
		case Constant.CIG:
			sy = 0;
			break;
		case Constant.BITAUTO:
			sy = 1;
			break;
		default:
			logger.warn("Not support system! system:" + system);
			break;
		}
		if ((positionId == null) || ("".equals(positionId))) {
			logger.warn("PositionId is empty, use -1!");
			positionId = "-1";
		}
		if ((sourceModelMaterials == null) || (sourceModelMaterials.size() == 0)) {
			logger.warn("modelMaterials is empty!");
			return null;
		}
		if(pageSize <= 0){
			logger.warn("pageSize <= 0, set pageSize = 1 !");
			pageSize = 1;
		}
		List<ModelMaterial> modelMaterials = ImmutableList.copyOf(sourceModelMaterials);
		int j = -1;
		String tempImpressionCode = "";
		for (int i = 0; i < modelMaterials.size(); i++) {
			String matchedModelsStr = "";
			ModelMaterial modelMaterial = modelMaterialCopy(modelMaterials.get(i));
			if ((modelMaterial != null)
					&& (modelMaterial.getMatchedTags() != null)) {
				Stream<Integer> stream = modelMaterial.getMatchedTags()
						.stream();
				matchedModelsStr = stream.filter((tag) -> tag != null)
						.distinct().limit(Constant.MATCHED_TAGS_NUM_LIMIT)
						.map((tag) -> Integer.toString(tag))
						.collect(Collectors.joining(","));
				stream.close();
			}
			String matchedCompetitiveModelsStr = "";
			if ((modelMaterial != null)
					&& (modelMaterial.getMatchedCompetitiveModels() != null)) {
				Stream<Integer> stream = modelMaterial
						.getMatchedCompetitiveModels().stream();
				matchedCompetitiveModelsStr = stream.filter((tag) -> tag != null)
						.distinct().limit(Constant.MATCHED_TAGS_NUM_LIMIT)
						.filter(tag -> tag != null)
						.map((tag) -> Integer.toString(tag))
						.collect(Collectors.joining(","));
				stream.close();
			}
			// 按照pageSize分组生成曝光代码。
			if(i / pageSize > j){
				j = i / pageSize;
				int maxIndex = i + pageSize;
				if(maxIndex > modelMaterials.size()){
					maxIndex = modelMaterials.size();
				}
				List<ModelMaterial> subList = modelMaterials.subList(i, maxIndex);
				// 投放物料id使用车型id.
				StringBuilder modelIds = new StringBuilder("");
				for(ModelMaterial subMm : subList){
					modelIds.append(subMm.getModelId());
					modelIds.append(",");
				}
				String modelIdsStr = modelIds.toString();
				if (modelIdsStr.endsWith(",")) {
					modelIdsStr = modelIdsStr.substring(0, modelIdsStr.length() - 1);
				}
				tempImpressionCode = String.format(impressionMonitorCodeTemplate, sy,
						positionId, j+1, matchedModelsStr, matchedCompetitiveModelsStr, modelIdsStr, SystemTimer.currentTimeMillis());
			}
			modelMaterial.setImpressionMonitorCode(tempImpressionCode);
			// 第1个位置、第2个位置、第3个位置（用于动态模板，每轮播有3个车型，用于标示车型在第几个位置上）
			int m = i % pageSize + 1;
			
			//TODO 重构，先实现。
			//易车商城 投放线索收集 回传 参数
//			广告位ID：123
//			#素材ID：0 （没有素材就写0）
//			#广告ID：789
//			车系ID：1905
//			轮播ID：2
//			轮播位置ID：1
//			rfpa_source=123_0_789_1905_2_1
			String landingPage = modelMaterial.getLandingPage();
			Integer pid=Integer.parseInt(positionId);
			//惠买车接口投放加tracker            
			if(Constant.HUIMAICHE_DLIST.contains(positionId)){
				landingPage=dealUrlTracker(landingPage,"tracker_u=247_gcjdt");
			}
			if(Constant.HMC_ONE_LIST.contains(positionId)){
				if(pid == 1027){
					landingPage=dealUrlTracker(landingPage,"tracker_u=361_dtgg");
				}
				if(pid == 1011){
					landingPage=dealUrlTracker(landingPage,"tracker_u=362_dtgg");
				}
				if(pid == 1070){
					landingPage=dealUrlTracker(landingPage,"tracker_u=363_dtgg");
				}
			}
			if(Constant.YX_DLIST.contains(positionId)){
				landingPage = dealUrlTracker(landingPage,"from=gg07");
			}
			
			if(isMall!=null && isMall.length==1 && isMall[0]){
				StringBuilder temp = new StringBuilder("");
				//TODO 判断不严谨，如果有有多个问号可能就不对了。
//				eg:http://shijiazhuang.bitauto.com/?reffer=http://shijiazhuang.bitauto.com/i?aaa=bbb
				if(landingPage.contains("?")){
					temp.append("&");
				} else {
					temp.append("?");
				}
				temp.append("rfpa_source=");
				temp.append(positionId);
				temp.append("_");
				temp.append(0);
				temp.append("_");
				temp.append(0);
				temp.append("_");
				temp.append(modelMaterial.getModelId());
				temp.append("_");
				temp.append(j+1);
				temp.append("_");
				temp.append(m);
				landingPage = landingPage + temp;
			}
			String clickMonitorCode = String.format(clickMonitorCodeTemplate, sy,
					positionId, j+1, m, matchedModelsStr, matchedCompetitiveModelsStr, modelMaterial.getModelId(), landingPage);
			modelMaterial.setClickMonitorCode(clickMonitorCode);
			result.add(modelMaterial);
		}
		return result;
	}
	
	
	public static String generateDeviceCode(BitautoMaterial material,Integer pid,String dvid){
		if(material != null && pid != null  && dvid != null){
			Feed feed=new Feed();
			feed.setDvid(dvid);
			feed.setStatus(0);
			Poster poster=new Poster();
			poster.setNewSid(material.getId().toString());
			poster.setPicUrl(material.getUrl());
			poster.setTitle(material.getTitle());
			poster.setType("ListAd");
			
			String wapLinkUrl = material.getWapLinkUrl();
			String landingPage = material.getLinkUrl();
			
			if(wapLinkUrl == null || "".equals(wapLinkUrl)){
				if(landingPage == null || "".equals(landingPage)){
					logger.error("material ID:"+material.getId()+" link is error xxxxxxxxxxxxx");
					return null;
				}else{
					wapLinkUrl=landingPage;
				}
			}
			StringBuilder temp = new StringBuilder("");
			Integer modelId = material.getId();
			if(wapLinkUrl.contains("?")){
				temp.append("&");
			} else {
				temp.append("?");
			}
			temp.append("WT.FROM=jzgg__");
			temp.append(pid);
			temp.append("t");
			temp.append(modelId);
			temp.append("&rfpa_source=");
			temp.append(pid);
			temp.append("_");
			temp.append(modelId);
			wapLinkUrl = wapLinkUrl + temp;
			wapLinkUrl = String.format(clickAppCodeTemplate, pid,modelId,dvid, wapLinkUrl);
			poster.setUrl(wapLinkUrl);
			List<Poster> listPosters=new ArrayList<>(1);
			listPosters.add(poster);
			feed.setResult(listPosters);
			return gson.toJson(feed);
		}
		return null;
	}
	
	public static void sendExposure(Integer pid,Integer modelId,String dvid){
		int nextInt = random.nextInt(1000000);
		String url = String.format(pvAppCodeTemplate, pid,modelId,dvid,nextInt);
		HttpUtils.httpClient(url, HttpMethodEnum.GET, null);
		logger.info("Send the exposure success:"+url+"=>=>=>=>=>=>=>=>=>=>=>=>=>=>=>=>=>=>=>=>=>=>=>=>");
	}
	
	public static Collection<ModelMaterial> generateHuiMaiCheMonitorCode(String system,Integer positionId, Collection<ModelMaterial> modelMaterials, int pageSize, boolean... isMall) {
		// 系统 system sy sy 0：adsense，1易车精准，3：smartAD
		int sy = -1;
		switch (system) {
		case Constant.CIG:
			sy = 0;
			break;
		case Constant.BITAUTO:
			sy = 1;
			break;
		default:
			logger.warn("Not support system! system:" + system);
			break;
		}
		if ((positionId == null) ) {
			logger.warn("PositionId is empty, use -1!");
			positionId = -1;
		}
		if ((modelMaterials == null) || (modelMaterials.size() == 0)) {
			logger.warn("modelMaterials is empty!");
			return null;
		}
		
		for(ModelMaterial mm : modelMaterials){
			String landingPage = mm.getLandingPage();
			String getmUrl = mm.getmUrl();
			
			if(positionId == 1111){
				landingPage = addHttpHeader(landingPage,"tracker_u=216_dtjz");
				getmUrl = addHttpHeader(getmUrl,"tracker_u=217_dtjz");
			}
			if(positionId == 1272){
				landingPage = addHttpHeader(landingPage,"tracker_u=331_hdy");
				getmUrl = addHttpHeader(getmUrl,"tracker_u=332_hdy");
			}
			
			//1111_PC端：tracker_u=216_dtjz
			//1111_移动端：tracker_u=217_dtjz
			//1272_PC端：tracker_u=216_dtjz
			//1272_移动端：tracker_u=217_dtjz

			String pc_url=null;
			String mo_url=null;
			if(isMall!=null && isMall.length==1 && isMall[0]){
				if(positionId == 1111){
					landingPage = signboardConvert(landingPage,"1111",mm.getModelId());
					getmUrl = signboardConvert(getmUrl,"2222",mm.getModelId());
					pc_url = String.format(clickHuiMaiCheMonitorCodeTemplatePc, sy,"1111",mm.getModelId(), landingPage);
					mo_url = String.format(clickHuiMaiCheMonitorCodeTemplateMobile, sy,"2222",mm.getModelId(), getmUrl);
				}
				if(positionId == 1272){
					landingPage = signboardConvert(landingPage,"1272",mm.getModelId());
					getmUrl = signboardConvert(getmUrl,"1274",mm.getModelId());
					pc_url = String.format(clickHuiMaiCheMonitorCodeTemplatePc, sy,"1272",mm.getModelId(), landingPage);
					mo_url = String.format(clickHuiMaiCheMonitorCodeTemplateMobile, sy,"1274",mm.getModelId(), getmUrl);
				}
			}
			mm.setClickMonitorCode(pc_url);
			mm.setImpressionMonitorCode(mo_url);
		}
		return modelMaterials;
	}
	
	public static Collection<ModelMaterial> generateYXMonitorCode(String system,String positionId, Collection<ModelMaterial> modelMaterials, int pageSize, boolean... isMall) {
		// 系统 system sy sy 0：adsense，1易车精准，3：smartAD
		int sy = -1;
		switch (system) {
		case Constant.CIG:
			sy = 0;
			break;
		case Constant.BITAUTO:
			sy = 1;
			break;
		default:
			logger.warn("Not support system! system:" + system);
			break;
		}
		if ((positionId == null) || ("".equals(positionId))) {
			logger.warn("PositionId is empty, use -1!");
			positionId = "-1";
		}
		if ((modelMaterials == null) || (modelMaterials.size() == 0)) {
			logger.warn("modelMaterials is empty!");
			return null;
		}
		
		for(ModelMaterial mm : modelMaterials){
			
			String landingPage = dealUrlTracker(mm.getLandingPage(),"from=gg07");
			
			if(isMall!=null && isMall.length==1 && isMall[0]){
				landingPage = signboardConvert(landingPage,positionId,mm.getModelId());
			}
			
			String pc_url = String.format(clickHuiMaiCheMonitorCodeTemplatePc, sy,positionId,mm.getModelId(), landingPage);
			
			mm.setClickMonitorCode(pc_url);
		}
		return modelMaterials;
	}
	
	private static String dealUrlTracker(String url, String param) {
		StringBuilder temp = new StringBuilder("");
		if(url.contains("?")){
			temp.append("&");
		} else {
			temp.append("?");
		}
		temp.append(param);
		return (url + temp.toString());
	}

	public static String signboardConvert(String url,String positionId,Integer modelId){
		StringBuilder temp = new StringBuilder("");
		if(url.contains("?")){
			temp.append("&");
		} else {
			temp.append("?");
		}
		temp.append("rfpa_source=");
		temp.append(positionId);
		temp.append("_");
		temp.append(0);
		temp.append("_");
		temp.append(0);
		temp.append("_");
		temp.append(modelId);
		return (url + temp.toString());
	}
	
	public static String addHttpHeader(String url,String arg){
		StringBuffer sb=new StringBuffer();
		sb.append(url).insert(0,"http://");
		
		if(url.contains("?")){
			sb.append("&");
		} else {
			sb.append("?");
		}
		
		sb.insert(sb.length(), arg);
		return sb.toString();
	}
	
	
	public static Map<Integer, Integer> matchAdsRegions(){
		logger.info("Match yipai ads regions start...");
		Map<Integer, Integer> result = new HashMap<>(600);
		// 全国
		result.put(0, 0);
		try {
			Class.forName(Constant.DB_DRIVER);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
		Connection dbConnection=null;
		try{
			dbConnection = DriverManager.getConnection(Constant.DB_CONNECTION, Constant.DB_USER, Constant.DB_PASSWORD);
		}catch(SQLException e){
			logger.error("matchAdsRegions is error:",e);
		}
		List<String> haveProvince = new ArrayList<>();
		List<String> haveCity = new ArrayList<>();
		// 匹配上的地域ids
		List<Integer> inDb = new ArrayList<>();
		File regionCodeFile = new File(Constant.YIPAI_REGION_CODE);
		if(!regionCodeFile.exists()){
			logger.error("Yipai region file not found! region file:" + Constant.YIPAI_REGION_CODE);
		}
		Map<Integer, String> provinceMap = new HashMap<Integer, String>(50);
		Map<Integer, String> cityMap = new HashMap<Integer, String>(600);
		try {
			// 一次查出来放到map里重复使用。
			String provinceSql = "select * from province";
			PreparedStatement provincePs = dbConnection.prepareStatement(provinceSql);
			ResultSet rs = provincePs.executeQuery();
			while (rs.next()) {
				int proid = rs.getInt("proid");
				String proName = rs.getString("proName");
				provinceMap.put(proid, proName);
			}
			String citySql = "select * from city";
			PreparedStatement cityPs = dbConnection.prepareStatement(citySql);
			rs = cityPs.executeQuery();
			while (rs.next()) {
				int cityid = rs.getInt("cityid");
				String cityName = rs.getString("cityName");
				cityMap.put(cityid, cityName);
			}
			try (Stream<String> lines = Files.lines(regionCodeFile.toPath(), Charset.forName("utf-8"))) {
				lines.forEach(
						line -> {
							String[] regionIdAndName = line.split(",");
							if (regionIdAndName.length != 2) {
								logger.error("非法 line:" + line);
							} else {
								String regionIdStr = regionIdAndName[0];
								String name = regionIdAndName[1];
								if (name.endsWith("省")
										|| name.endsWith("自治区")
										|| name.equals("北京")
										|| name.equals("上海")
										|| name.equals("天津")
										|| name.equals("重庆")) {

									Stream<Entry<Integer, String>> stream = provinceMap
											.entrySet()
											.stream();
									stream
											.forEach(
													entry -> {
														int regionId = entry
																.getKey();
														String regionName = entry
																.getValue();

														if (regionName
																.startsWith(name)) {
															result.put(regionId,
																	Integer.parseInt(regionIdStr)
																	);
															haveProvince
																	.add(regionIdStr
																			+ ","
																			+ regionId
																			+ ","
																			+ name);
															inDb.add(regionId);
														}

													});
									stream.close();
								} else {
									Stream<Entry<Integer, String>> stream = cityMap
											.entrySet()
											.stream();
									stream
											.forEach(
													entry -> {
														int regionId = entry
																.getKey();
														String regionName = entry
																.getValue();

														if (regionName
																.startsWith(name)) {
															result.put(regionId,
																	Integer.parseInt(regionIdStr)
																	);
															haveCity.add(regionIdStr
																	+ ","
																	+ regionId
																	+ ","
																	+ name);
															inDb.add(regionId);
														}

													});
									stream.close();
								}
							}
						});
			} catch (Exception e) {
				logger.error("Processing region code file failed!", e);
			}

			/*logger.info("haveNotProvince:" + haveNotProvince);
			logger.info("haveNotProvince size:" + haveNotProvince.size());
			logger.info("haveProvince:" + haveProvince);
			logger.info("haveProvince size:" + haveProvince.size());
			logger.info("haveNotCity:" + haveNotCity);
			logger.info("haveNotCity size:" + haveNotCity.size());
			logger.info("haveCity:" + haveCity);
			logger.info("haveCity size:" + haveCity.size());

			logger.info("inDb:" + inDb);*/
			String inDbIds = inDb.toString().replace("[", "").replace("]", "");
			String checkProvinceSql = "select * from province where proid not in ("
					+ inDbIds + ")";
			provincePs = dbConnection
					.prepareStatement(checkProvinceSql);
			rs = provincePs.executeQuery();
			while (rs.next()) {
				int proid = rs.getInt("proid");
				String proName = rs.getString("proName");
				logger.info("不匹配的省份:" + proName + "," + proid);
			}

			String checkCitySql = "select * from city where cityid not in ("
					+ inDbIds + ")";
			cityPs = dbConnection.prepareStatement(checkCitySql);
			rs = cityPs.executeQuery();
			while (rs.next()) {
				int cityid = rs.getInt("cityid");
				String cityName = rs.getString("cityName");
				logger.info("不匹配的城市:" + cityName + "," + cityid);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException e) {
					e.printStackTrace();
					logger.error("close dbconnection is error",e);
				}
			}
		}
		logger.info("Match yipai regions end.");
		return result;
	}
	
	
	/*
	 * city数目(包含省份)
		455
		匹配完成的个数: 
		463
		去重
		449(+2包含两个省直辖县级行政区划)
		
		不存在 
		cityid
		不匹配的城市:省直辖县级行政区划,429000
		不匹配的城市:三沙市,460300
		不匹配的城市:省直辖县级行政区划,469000
		不匹配的城市:綦江区,500110
		不匹配的城市:大足区,500111
		不匹配的城市:海东市,630200
	 */
	public static Map<Integer, Integer> matchYipaiRegions() {
		logger.info("Match yipai regions start...");
		Map<Integer, Integer> result = new HashMap<>(600);
		// 全国
		result.put(0, 0);
		try {
			Class.forName(Constant.DB_DRIVER);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}
		Connection dbConnection=null;
		try {
			dbConnection = DriverManager.getConnection(Constant.DB_CONNECTION, Constant.DB_USER, Constant.DB_PASSWORD);
			List<String> haveNotProvince = new ArrayList<>();
			List<String> haveNotCity = new ArrayList<>();
			List<String> haveProvince = new ArrayList<>();
			List<String> haveCity = new ArrayList<>();
			// 匹配上的地域ids
			List<Integer> inDb = new ArrayList<>();
			File regionCodeFile = new File(Constant.YIPAI_REGION_CODE);
			if(!regionCodeFile.exists()){
				logger.error("Yipai region file not found! region file:" + Constant.YIPAI_REGION_CODE);
			}
			Map<Integer, String> provinceMap = new HashMap<Integer, String>(50);
			Map<Integer, String> cityMap = new HashMap<Integer, String>(600);
			// 一次查出来放到map里重复使用。
			String provinceSql = "select * from province";
			PreparedStatement provincePs = dbConnection
					.prepareStatement(provinceSql);
			ResultSet rs = provincePs.executeQuery();
			while (rs.next()) {
				int proid = rs.getInt("proid");
				String proName = rs.getString("proName");
				provinceMap.put(proid, proName);
			}
			String citySql = "select * from city";
			PreparedStatement cityPs = dbConnection.prepareStatement(citySql);
			rs = cityPs.executeQuery();
			while (rs.next()) {
				int cityid = rs.getInt("cityid");
				String cityName = rs.getString("cityName");
				cityMap.put(cityid, cityName);
			}
			try (Stream<String> lines = Files.lines(regionCodeFile.toPath(), Charset.forName("utf-8"))) {
				lines.forEach(
						line -> {
							String[] regionIdAndName = line.split(",");
							if (regionIdAndName.length != 2) {
								logger.error("非法 line:" + line);
							} else {
								String regionIdStr = regionIdAndName[0];
								String name = regionIdAndName[1];
								if (name.endsWith("省")
										|| name.endsWith("自治区")
										|| name.equals("北京")
										|| name.equals("上海")
										|| name.equals("天津")
										|| name.equals("重庆")) {

									Stream<Entry<Integer, String>> stream = provinceMap
											.entrySet()
											.stream();
									stream
											.forEach(
													entry -> {
														int regionId = entry
																.getKey();
														String regionName = entry
																.getValue();

														if (regionName
																.startsWith(name)) {
															result.put(
																	Integer.parseInt(regionIdStr),
																	regionId);
															haveProvince
																	.add(regionIdStr
																			+ ","
																			+ regionId
																			+ ","
																			+ name);
															inDb.add(regionId);
														}

													});
									stream.close();
								} else {
									Stream<Entry<Integer, String>> stream = cityMap
											.entrySet()
											.stream();
									stream
											.forEach(
													entry -> {
														int regionId = entry
																.getKey();
														String regionName = entry
																.getValue();

														if (regionName
																.startsWith(name)) {
															result.put(
																	Integer.parseInt(regionIdStr),
																	regionId);
															haveCity.add(regionIdStr
																	+ ","
																	+ regionId
																	+ ","
																	+ name);
															inDb.add(regionId);
														}

													});
									stream.close();
								}
							}
						});
			} catch (Exception e) {
				logger.error("Processing region code file failed!", e);
			}

			logger.info("haveNotProvince:" + haveNotProvince);
			logger.info("haveNotProvince size:" + haveNotProvince.size());
			logger.info("haveProvince:" + haveProvince);
			logger.info("haveProvince size:" + haveProvince.size());
			logger.info("haveNotCity:" + haveNotCity);
			logger.info("haveNotCity size:" + haveNotCity.size());
			logger.info("haveCity:" + haveCity);
			logger.info("haveCity size:" + haveCity.size());

			logger.info("inDb:" + inDb);
			String inDbIds = inDb.toString().replace("[", "").replace("]", "");
			String checkProvinceSql = "select * from province where proid not in ("
					+ inDbIds + ")";
			provincePs = dbConnection
					.prepareStatement(checkProvinceSql);
			rs = provincePs.executeQuery();
			while (rs.next()) {
				int proid = rs.getInt("proid");
				String proName = rs.getString("proName");
				logger.info("不匹配的省份:" + proName + "," + proid);
			}

			String checkCitySql = "select * from city where cityid not in ("
					+ inDbIds + ")";
			cityPs = dbConnection.prepareStatement(checkCitySql);
			rs = cityPs.executeQuery();
			while (rs.next()) {
				int cityid = rs.getInt("cityid");
				String cityName = rs.getString("cityName");
				logger.info("不匹配的城市:" + cityName + "," + cityid);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		logger.info("Match yipai regions end.");
		return result;
	}
	
	

	/*
	 * city数目(包含省份)
		455
		匹配完成的个数: 
		463
		去重
		449(+2包含两个省直辖县级行政区划)
		
		不存在 
		cityid
		不匹配的城市:省直辖县级行政区划,429000
		不匹配的城市:三沙市,460300
		不匹配的城市:省直辖县级行政区划,469000
		不匹配的城市:綦江区,500110
		不匹配的城市:大足区,500111
		不匹配的城市:海东市,630200
	 */
	public static Map<Integer, String> matchCityNameRegions() throws SQLException {
		logger.info("Match yipai regions start...");
		Map<Integer, Integer> result = new HashMap<>(600);
		// 全国
		result.put(0, 0);
		try {
			Class.forName(Constant.DB_DRIVER);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

		final Connection dbConnection = DriverManager.getConnection(Constant.DB_CONNECTION, Constant.DB_USER, Constant.DB_PASSWORD);
		List<String> haveNotProvince = new ArrayList<>();
		List<String> haveNotCity = new ArrayList<>();
		List<String> haveProvince = new ArrayList<>();
		List<String> haveCity = new ArrayList<>();
		// 匹配上的地域ids
		List<Integer> inDb = new ArrayList<>();
		File regionCodeFile = new File(Constant.YIPAI_REGION_CODE);
		if(!regionCodeFile.exists()){
			logger.error("Yipai region file not found! region file:" + Constant.YIPAI_REGION_CODE);
		}
		Map<Integer, String> provinceMap = new HashMap<Integer, String>(50);
		Map<Integer, String> cityMap = new HashMap<Integer, String>(600);
		try {
			// 一次查出来放到map里重复使用。
			String provinceSql = "select * from province";
			PreparedStatement provincePs = dbConnection
					.prepareStatement(provinceSql);
			ResultSet rs = provincePs.executeQuery();
			while (rs.next()) {
				int proid = rs.getInt("proid");
				String proName = rs.getString("proName");
				provinceMap.put(proid, proName);
			}
			String citySql = "select * from city";
			PreparedStatement cityPs = dbConnection.prepareStatement(citySql);
			rs = cityPs.executeQuery();
			while (rs.next()) {
				int cityid = rs.getInt("cityid");
				String cityName = rs.getString("cityName");
				cityMap.put(cityid, cityName);
			}
			try (Stream<String> lines = Files.lines(regionCodeFile.toPath(), Charset.forName("utf-8"))) {
				lines.forEach(
						line -> {
							String[] regionIdAndName = line.split(",");
							if (regionIdAndName.length != 2) {
								logger.error("非法 line:" + line);
							} else {
								String regionIdStr = regionIdAndName[0];
								String name = regionIdAndName[1];
								if (name.endsWith("省")
										|| name.endsWith("自治区")
										|| name.equals("北京")
										|| name.equals("上海")
										|| name.equals("天津")
										|| name.equals("重庆")) {

									Stream<Entry<Integer, String>> stream = provinceMap
									.entrySet()
									.stream();
									stream
											.forEach(
													entry -> {
														int regionId = entry
																.getKey();
														String regionName = entry
																.getValue();

														if (regionName
																.startsWith(name)) {
															result.put(
																	Integer.parseInt(regionIdStr),
																	regionId);
															haveProvince
																	.add(regionIdStr
																			+ ","
																			+ regionId
																			+ ","
																			+ name);
															inDb.add(regionId);
														}

													});
									stream.close();
								} else {
									Stream<Entry<Integer, String>> stream = cityMap.entrySet()
											.stream();
									stream
											.forEach(
													entry -> {
														int regionId = entry
																.getKey();
														String regionName = entry
																.getValue();

														if (regionName
																.startsWith(name)) {
															result.put(
																	Integer.parseInt(regionIdStr),
																	regionId);
															haveCity.add(regionIdStr
																	+ ","
																	+ regionId
																	+ ","
																	+ name);
															inDb.add(regionId);
														}

													});
									stream.close();
								}
							}
						});
			} catch (Exception e) {
				logger.error("Processing region code file failed!", e);
			}

			logger.info("haveNotProvince:" + haveNotProvince);
			logger.info("haveNotProvince size:" + haveNotProvince.size());
			logger.info("haveProvince:" + haveProvince);
			logger.info("haveProvince size:" + haveProvince.size());
			logger.info("haveNotCity:" + haveNotCity);
			logger.info("haveNotCity size:" + haveNotCity.size());
			logger.info("haveCity:" + haveCity);
			logger.info("haveCity size:" + haveCity.size());

			logger.info("inDb:" + inDb);
			String inDbIds = inDb.toString().replace("[", "").replace("]", "");
			String checkProvinceSql = "select * from province where proid not in ("
					+ inDbIds + ")";
			provincePs = dbConnection
					.prepareStatement(checkProvinceSql);
			rs = provincePs.executeQuery();
			while (rs.next()) {
				int proid = rs.getInt("proid");
				String proName = rs.getString("proName");
				logger.info("不匹配的省份:" + proName + "," + proid);
			}

			String checkCitySql = "select * from city where cityid not in ("
					+ inDbIds + ")";
			cityPs = dbConnection.prepareStatement(checkCitySql);
			rs = cityPs.executeQuery();
			while (rs.next()) {
				int cityid = rs.getInt("cityid");
				String cityName = rs.getString("cityName");
				logger.info("不匹配的城市:" + cityName + "," + cityid);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (dbConnection != null) {
				dbConnection.close();
			}
		}
		logger.info("Match yipai regions end.");
		cityMap.put(0, "全国");
		return cityMap;
	}
	
	
	public static String handleNewCookie(HttpServerExchange exchange) {
		// 使用不同域名(echo.adsense.cig.com.cn, echo.jy.ctags.cn)访问时，cookie写在相应域名下。
		String hostName = exchange.getHostName();
		String setDomainStr = ".cig.com.cn";
		if((hostName!=null) && (hostName.length()>0)){
			if(hostName.contains("ctags.cn")){
				setDomainStr = ".ctags.cn";
			}
		}
		// 种cookie id
		String cookieId = Utils.cookieIdGenerator();
		String uk = getUk(exchange,cookieId);//获取同屏去重的唯一标识码
		String maybeNewCookieId = materialUtils.generateNotDuplicatedCookieId(uk, cookieId);
		if(cookieId.equals(maybeNewCookieId)){
			// p3p cookie
			Cookie p3pCookie = new CookieImpl(Constant.P3P_COOKIE_KEY,Constant.P3P_COOKIE_VALUE);
			p3pCookie.setDomain(setDomainStr);
			p3pCookie.setPath("/");
			p3pCookie.setMaxAge(Constant.COOKIE_MAX_AGE);
			exchange.setResponseCookie(p3pCookie);
			// header("Set-Cookie: CIGDCID=".$Uid."; expires=Sun, 23-Dec-2018 08:13:02 GMT; domain=.cig.com.cn; path=/");
			Cookie idCookie = new CookieImpl(Constant.COOKIE_ID,cookieId);//cookie[name]_CIGDCID
			idCookie.setDomain(setDomainStr);
			idCookie.setPath("/");
			idCookie.setMaxAge(Constant.COOKIE_MAX_AGE);
			exchange.setResponseCookie(idCookie);
		}
		return cookieId;
	}
	/**
	 * @descirption:http connection request
	 * @param requestUrl
	 * @param method
	 * @return
	 */
	public static String httpURLConnRequest(String requestUrl,String method) {
		StringBuffer buffer = new StringBuffer();
		HttpURLConnection conn=null;
		InputStream inputStream=null;
		InputStreamReader inputStreamReader=null;
		BufferedReader bufferedReader=null;
		try {
			URL url = new URL(requestUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(120000);//2分钟超时时间
			conn.setRequestMethod(method);
			conn.setUseCaches(false);  
			conn.connect();
			// 将返回的输入流转换成字符串
			int response_code = conn.getResponseCode();
			if(response_code == HttpURLConnection.HTTP_OK) {
				inputStream = conn.getInputStream();
				inputStreamReader = new InputStreamReader(inputStream, "utf-8");
				bufferedReader = new BufferedReader(inputStreamReader);
				while(true){
					final String line=bufferedReader.readLine();
					if(line == null) break;
					buffer.append(line);
				}
			}else{
				logger.warn("request:"+requestUrl+" is fault:"+response_code);
			}
		} catch (Exception e) {
			logger.error("error:%s /n",requestUrl,e);
		} finally{
			if(bufferedReader != null){
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					bufferedReader=null;
				}
			}
			
			if(inputStreamReader != null){
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					inputStreamReader=null;
				}
			}
			
			if(inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}finally{
					inputStream=null;
				}
			}
			
			if(conn !=null){
	           conn.disconnect();
	        }
		}
		return buffer.toString();
	}
	
	/**
	 * @decription:获取惠买车开放投放城市
	 */
	public static Map<Integer,OpenCity> getoHuiMaiCheOpenCitylist(){
		Map<Integer,OpenCity> openCitys=new HashMap<>();
		String data = httpURLConnRequest(Constant.HUIMAICHE_OPENCITY_URL,"GET");
		if(data == null || "".equals(data)){
			logger.error("read Constant.HUIMAICHE_OPENCITY_URL is null....");
			return	openCitys;
		}
		JSONArray jo=new JSONArray(data);
		for(int i=0;i<jo.length();i++){
			OpenCity openCity=new OpenCity();
			JSONObject obj=jo.getJSONObject(i);
			int cityId = obj.getInt("CityID");
			int provinceId = obj.getInt("ProvinceID");
			String cityName = obj.getString("CityName");
			String cityNamePy = obj.getString("CityNamePY");
			int isCharge = obj.getInt("IsCharge");
			String cityURL = obj.getString("CityURL");
			openCity.setCityId(cityId);
			openCity.setProvinceId(provinceId);
			openCity.setCityName(cityName);
			openCity.setCityNamePy(cityNamePy);
			openCity.setIsCharge(isCharge);
			openCity.setCityUrl(cityURL);
			openCitys.put(cityId, openCity);
		}
		return openCitys;
	}
	
	/**
	 * @description 获取对应城市所属地区。
	 * @return
	 */
	public static Map<Integer,CityMapping> getCityMapping(){
		Map<Integer,CityMapping> cityMappings=new HashMap<>();
		String data = httpURLConnRequest(Constant.HUIMAICHE_TIPCITY_URL,"GET");
		if(data == null ||"".equals(data)) {
			logger.error("read "+Constant.HUIMAICHE_TIPCITY_URL+" is error....");
			return cityMappings;
		}
		JSONArray jo=new JSONArray(data);
		for(int i=0;i<jo.length();i++){
			CityMapping cityMaping=new CityMapping();
			JSONObject obj=jo.getJSONObject(i);
			int cityId = obj.getInt("CityID");
			int provinceId = obj.getInt("ProvinceID");
			int rewriteCityID = obj.getInt("RewriteCityID");
			cityMaping.setCityId(cityId);
			cityMaping.setPrivinceId(provinceId);
			cityMaping.setRewriteCityId(rewriteCityID);
			cityMappings.put(cityId, cityMaping);
		}
		return cityMappings;
	}	
	
	public static Path getNewlyCreatedFileException(String directoryURI,String fileNameStartsWith){
		if ((fileNameStartsWith == null) || ("".equals(fileNameStartsWith))) {
			return null;
		}
		File directory = new File(directoryURI);
		if (!directory.exists()) {
			return null;
		}
		
		Path lastFile = null;
		try (Stream<Path> paths = Files.list(directory.toPath())) {
			lastFile = paths.filter(file -> {
				return file.getFileName().toString().startsWith(fileNameStartsWith);
			})
			.max((file1, file2) -> {
				BasicFileAttributes attr1 = null;
				try {
					attr1 = Files.readAttributes(file1,
							BasicFileAttributes.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
				FileTime file1CreateTime = attr1.creationTime();
				BasicFileAttributes attr2 = null;
				try {
					attr2 = Files.readAttributes(file2,
							BasicFileAttributes.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
				FileTime file2CreateTime = attr2.creationTime();
				return file1CreateTime.compareTo(file2CreateTime);
			}).orElse(null);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		if (lastFile == null) {
			return null;
		}
		return lastFile;
	}
	
	/**
	 * 读取txt文件
	 * @param filename
	 * @return
	 */
	public static String readJsonFile(String filename){
	    try (InputStream is = new FileInputStream(filename)) {
	        return IOUtils.toString(is, StandardCharsets.UTF_8);
	    }catch (IOException e) {
	    	logger.error("error:"+e.getMessage(),e);
		}
	    return null;
	}
	/**
	 * 支撑jsonp跨域操作请求
	 * @param data
	 * @param callback
	 * @return
	 */
	public static String combineResponseJsonp(String data,String callback){
		if(callback.length() > 100000 
				||data == null 
				|| callback == null 
				|| "".equals(data) 
				|| "".equals(callback)){
			return "";
		}
		StringBuffer result=new StringBuffer();
		result.append(data);
		result.insert(0, callback+"(");
		result.insert(result.length(), ");");
		return result.toString();
	}
	
	public static BitautoMaterial bitautoMaterialCopy(BitautoMaterial material){
		if(material == null){
			return null;
		}
		if(material!=null && material.isCopied()){
			return material;
		}
		BitautoMaterial result = new BitautoMaterial();
		bitautoMaterialCopier.copy(material, result, null);
		//BeanUtils.copyProperties(result, material);
		result.setCopied(true);
		return result;
	}
	
	public static ModelMaterial modelMaterialCopy(ModelMaterial material){
		if(material == null){
			return null;
		}
		if(material!=null && material.isCopied()){
			return material;
		}
		ModelMaterial result = new ModelMaterial();
		modelMaterialCopier.copy(material, result, null);
		//BeanUtils.copyProperties(result, material);
		result.setCopied(true);
		return result;
	}
	
}
