package cn.com.cig.adsense.handler;

import static cn.com.cig.adsense.general.CommonBiz.getClientIp;
import static cn.com.cig.adsense.general.CommonBiz.getCookieId;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.delivery.impl.SortMaterialsByModelFrequency;
import cn.com.cig.adsense.service.DmpService;
import cn.com.cig.adsense.service.MaterialLibraryService;
import cn.com.cig.adsense.service.impl.CigMaterialLibraryServiceImpl;
import cn.com.cig.adsense.service.impl.DmpServiceImpl;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.utils.encode.EncodeUtil;
import cn.com.cig.adsense.vo.fix.FixtureDeliveryAttribute;
import cn.com.cig.adsense.vo.fix.Material;
import cn.com.cig.adsense.vo.fix.UserStruct;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/*
 * 处理固定位投放请求
 */
public class FixtureDeliveryHandler implements HttpHandler {

	private static Logger logger = LoggerFactory.getLogger(FixtureDeliveryHandler.class);
	private static final Random random = new Random();

	private DmpService ds = DmpServiceImpl.getInstance();
	private static final MaterialLibraryService mls = CigMaterialLibraryServiceImpl.getInstance();
	
	private static final SortMaterialsByModelFrequency sortHandler = new SortMaterialsByModelFrequency();
	
	
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		
		Deque<String> positionIdDeque = exchange.getQueryParameters().get(Constant.POSITION_ID);
		if ((positionIdDeque == null) || (positionIdDeque.size() == 0)) {
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		try{
			// 下列操作异步执行,在worker线程池中执行.
			String positionId = positionIdDeque.peek();
			// user id
			String tCookieId = getCookieId(exchange);
			
			// 自己计算地域，根据地域投.
			String ip =getClientIp(exchange);
			
			int[] locationArr = Utils.getAreas(ip);
			
			if ((locationArr == null) || (locationArr.length == 0)) {
				logger.warn("Unrecognized IP! ip:" + ip);
				handleDefaultMaterial(exchange, positionId);
				return;
			}
			
			int province = locationArr[0];
			int city = 0;
			if (locationArr.length >= 2) {
				city = locationArr[1];
			}
			
			List<Material> materials = null;
			// 是否关联过地域
			//TODO 每次都算地域有点浪费，先判断是否关联地域以后再算
			boolean relatedRegion = mls.relatedRegionByPositionId(positionId);
			if(relatedRegion){
				materials = mls.getMaterialByPositionIdRegion(
						positionId, province, city);
				// 关联过地域:根据地域未找到素材时，投默认素材。
				if ((materials == null) || (materials.size() == 0)) {
					handleDefaultMaterial(exchange, positionId);
					return;
				}
			} else {
				// 没有关联过地域:根据车型投所有素材（不包括默认）
				materials = mls.getMaterialByPositionId(positionId);
			}
			if ((materials == null) || (materials.size() == 0)) {
				// 找不到匹配物料,返回默认物料(包含多个-随机选一个)
				handleDefaultMaterial(exchange, positionId);
				return;
			}
			// 新cookie，根据地域选择素材后，直接投放，不过dmp了。
			// debug下，伪造cookieId方便测试，这个时候就不需要idCookie == null的判断了。
			if (tCookieId == null) {
				if (relatedRegion) {
					handleOnlyRegionMaterials(exchange, materials, positionId);
				} else {
					// 没关联地域,投默认素材。
					handleDefaultMaterial(exchange, positionId);
				}
				return;
			}
			// 老cookie
			// 未关联车型，不通过dmp,直接投放。
			boolean relatedModel = mls.relatedModel(materials);
			if (!relatedModel) {
				if(relatedRegion){
					handleOnlyRegionMaterials(exchange, materials, positionId);
				}else {
					// 没关联地域,投默认素材。
					handleDefaultMaterial(exchange, positionId);
				}
				return;
			}
			// cookie ub中保存以前的impression和click
			UserStruct us = ds.getUserStruct(tCookieId);
			// DMP中没这个cookie.和新cookie一样.
			if (us == null) {
				if(relatedRegion){
					handleOnlyRegionMaterials(exchange, materials, positionId);
				}else {
					// 没关联地域，dmp里也没有，投默认素材。
					handleDefaultMaterial(exchange, positionId);
				}
				return;
			}
			
			// 责任链模式（Chain of Responsibility）
			// http://zz563143188.iteye.com/blog/1847029
			FixtureDeliveryAttribute da = new FixtureDeliveryAttribute();
			da.setMaterials(materials);
			da.setUserStruct(us);
	//		da.setUserBehaviorMap(userBehaviorMap);
			sortHandler.operator(da);
			
			if ((da.getMaterials()==null) || (da.getMaterials().size()==0)) {
				// 找不到匹配物料,返回默认物料(包含多个-随机选一个)
				handleDefaultMaterial(exchange, positionId);
				return;
			}
			// 固定位素材调取失败,前台自动投放默认图片。
			// da中剩下的,就是符合投放策略的素材.
			returnMaterial(exchange, da.getMaterials(), positionId);
		} catch(Exception e){
			// 出问题了，由老莫投一个默认的物料。
			logger.error(e.getMessage(), e);
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
//			exchange.endExchange();
			return;
		}
	}

	private static void handleDefaultMaterial(
			final HttpServerExchange exchange, final String positionId) {

		// DMP中不存在的cookie返回默认素材
		List<Material> materials = mls.getDefaultMaterial(positionId);
		returnMaterial(exchange, materials, positionId);
	}
	
	// 地域[1],兴趣[0]：投放时选择地域符合兴趣为空的素材投。
	private static void handleOnlyRegionMaterials(final HttpServerExchange exchange, List<Material> materials, final String positionId){
		List<Material> onlyRegionMaterials = new ArrayList<>();
		Iterator<Material> iter = materials.iterator();
		while(iter.hasNext()){
			Material m = iter.next();
			if((m.getModels()==null) || (m.getModels().size()==0)){
				onlyRegionMaterials.add(m);
			}
		}
		if(onlyRegionMaterials.size() > 0){
			returnMaterial(exchange, onlyRegionMaterials, positionId);
		} else {
			handleDefaultMaterial(exchange, positionId);
		}
	}

	private static void returnMaterial(HttpServerExchange exchange,
			List<Material> materials, String positionId) {
		
		if ((positionId == null) || ("".equals(positionId))) {
			logger.warn("PositionId is empty!");
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// 没有素材可用
		if ((materials == null) || (materials.size() == 0)) {
			logger.warn("Return materials is empty!");
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// 如果存在多个默认素材,根据之前浏览的素材,尽量投放不同的默认素材.
		// Material name 中包含&,说明是易车中间的八个广告位.
		List<Material> filtered = Lists.newArrayList(Iterables.filter(
				materials, new Predicate<Material>() {
					public boolean apply(Material m) {
						boolean result = (m.getName()
								.contains(Constant.BITAUTO_SPECIAL_POSITION_SEPARATOR));
						return result;
					}
				}));
		String urlReturned = "";
		Material material = null;
//		List<String> materialIdList = new ArrayList<>(2);
		// 是否易车的中间八个广告位
		//TODO 应该分不同的接口，判断太恶心了。
		if (filtered == null || filtered.size() == 0) {
			int randomIndex = random.nextInt(materials.size());
			// 随机出一个,避免老是看一个素材。
			material = materials.get(randomIndex);
			urlReturned = Utils.createCigCallbackStr(Arrays.asList(material));
		} else {
			// 鼠标放到广告位上弹出的素材
			int randomIndex = random.nextInt(filtered.size());
			// 随机出一个,避免老是看一个素材。
			Material popupMaterial = filtered.get(randomIndex);
			// 易车的中间八个广告位需要两个素材
			String materialName = popupMaterial.getName().split(Constant.BITAUTO_SPECIAL_POSITION_SEPARATOR)[0];
			material = mls.getMaterialByName(positionId, materialName);
			urlReturned = Utils.createCigCallbackStr(Arrays.asList(material, popupMaterial));
		}
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
	}
}
