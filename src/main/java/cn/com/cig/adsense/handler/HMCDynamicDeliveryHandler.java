package cn.com.cig.adsense.handler;

import static cn.com.cig.adsense.general.CommonBiz.getCookieId;
import static cn.com.cig.adsense.general.CommonBiz.getUserModelFrequency;
import static cn.com.cig.adsense.general.CommonBiz.getUserStruct;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.service.ModelMaterialService;
import cn.com.cig.adsense.service.impl.ModelMaterialServiceImpl;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.vo.fix.ModelMaterial;
import cn.com.cig.adsense.vo.fix.OpenCity;
import cn.com.cig.adsense.vo.fix.UserStruct;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 * @File: HMCDynamicDeliveryHandler.java
 * @Package cn.com.cig.adsense.handler
 * @Description: TODO
 * @author zhangguodong
 * @date 2015年5月11日 下午2:42:21
 * @version V1.0
 */
public class HMCDynamicDeliveryHandler implements HttpHandler {
	private static Logger logger = LoggerFactory.getLogger(HMCDynamicDeliveryHandler.class);
	private static final ModelMaterialService mmService = ModelMaterialServiceImpl.getInstance();
	private static Map<Integer, OpenCity> openCitys = Utils.getoHuiMaiCheOpenCitylist();

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		Deque<String> positionIdDeque = exchange.getQueryParameters().get(Constant.POSITION_ID);// request_pid
		Deque<String> cityIdDeque = exchange.getQueryParameters().get(Constant.CITY_ID);// request_cityID
		Deque<String> device = exchange.getQueryParameters().get(Constant.DEVICE_ID);// request_cityID

		if ((positionIdDeque == null) || (positionIdDeque.size() == 0)) {
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
			return;
		}

		if ((cityIdDeque == null) || (cityIdDeque.size() == 0)) {
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
			return;
		}
		
		if ((device == null) || (device.size() == 0)) {
			logger.error("device is null..............");
		}

		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		Collection<ModelMaterial> modelMaterialss = null;
		String positionId = positionIdDeque.peek();
		String cid = cityIdDeque.peek();
		
		Integer deviceId = 0;
		try{
			deviceId = Integer.valueOf(device.peek());
		}catch(NumberFormatException e){
			logger.error("parse device is error:",deviceId);
		}

		Integer pid =null;
		Integer cityId = null;
		try {
			try {
				cityId = Integer.valueOf(cid);
				pid=Integer.parseInt(positionId);
			} catch (NumberFormatException e) {
				modelMaterialss = mmService.getTop9(Constant.HUIMAICHE);
				handEndleRequest(exchange, modelMaterialss,pid,deviceId);// 结束方法返回
				return;
			}
			
			if (!openCitys.containsKey(cityId)) {
				modelMaterialss = mmService.getTop9(Constant.HUIMAICHE);
				handEndleRequest(exchange, modelMaterialss, pid,deviceId);// 结束方法返回
				return;
			}

			// 自己计算地域，根据地域投.
			int[] locationArr = new int[] { 0, cityId, 0 };
			String tCookieId = getCookieId(exchange);
			UserStruct us = getUserStruct(tCookieId);
			Map<Integer, Integer> modelFrequency = getUserModelFrequency(us);
			modelMaterialss=getModelMaterialss(locationArr,us,modelFrequency,Constant.HUIMAICHE);
			modelMaterialss=sortByPromotion(modelMaterialss);
			handEndleRequest(exchange, modelMaterialss, pid,deviceId);// 结束方法返回
		} catch (Exception e) {
			// TODO 出问题了，弄个补漏的物料投，动态投放老莫不会补漏。。
			logger.error(e.getMessage(), e);
			modelMaterialss = mmService.getTop9(Constant.HUIMAICHE);
			handEndleRequest(exchange, modelMaterialss, pid,deviceId);// 结束方法返回
		}
	}
	
	private Collection<ModelMaterial> sortByPromotion(Collection<ModelMaterial> modelMaterialss) {
		List<ModelMaterial> promotion=new ArrayList<ModelMaterial>();
		List<ModelMaterial> unPromotion=new ArrayList<ModelMaterial>();
		List<ModelMaterial> result=new ArrayList<ModelMaterial>();
		if(modelMaterialss != null){
			Iterator<ModelMaterial> it = modelMaterialss.iterator();
			while(it.hasNext()){
				ModelMaterial next = it.next();
				boolean isPromotion = next.isPromotion();
				if(isPromotion){
					promotion.add(next);
				}else{
					unPromotion.add(next);
				}
			}
		}
		if(promotion != null && promotion.size() >0){
			result.addAll(promotion);
		}
		if(unPromotion != null && unPromotion.size() >0){
			result.addAll(unPromotion);
		}
		promotion=null;
		unPromotion=null;
		return result;
	}

	/**
	 * @description:将结果转换为json 数据输出
	 * @param modelMaterialss
	 * @param positionId
	 * @return
	 */
	public static String convertToJson(Collection<ModelMaterial> modelMaterialss,Integer positionId) {
		Collection<ModelMaterial> generateHuiMaiCheMonitorCode = Utils.generateHuiMaiCheMonitorCode(Constant.BITAUTO, positionId,modelMaterialss, 1,true);
		JSONArray jarry = new JSONArray();
		int i = 0;
		for (ModelMaterial mo : generateHuiMaiCheMonitorCode) {
			if(i >= 6){
				break;
			} else {
				i++;
			}
			JSONObject ob = new JSONObject();
			Integer SerialID = mo.getModelId();
			String Url = mo.getClickMonitorCode();
			String MUrl = mo.getImpressionMonitorCode();
			String Img = mo.getPic210X140();
			String SerialName = mo.getName();
			String Desc = mo.getPrefer_info();
			String ReferPrice = mo.getPrice();
			String MinPrice = mo.getMinPrice();
			
			if(positionId == 1111){
				try {
					double referPrice = Double.valueOf(ReferPrice).doubleValue();
					ob.put("ReferPrice", referPrice);
				} catch (NumberFormatException e) {
					logger.error("parse referPrice is error", e);
					ob.put("ReferPrice", 0.0);
				}
				try {
					double minPrice = Double.valueOf(MinPrice).doubleValue();
					ob.put("MinPrice", minPrice);
				} catch (NumberFormatException e) {
					logger.error("parse minPrice is error", e);
					ob.put("MinPrice", 0.0);
				}
				ob.put("SerialID", SerialID);
				ob.put("Url", Url);
				ob.put("MUrl", MUrl);
				ob.put("Img", Img);
				ob.put("SerialName", SerialName);
				ob.put("Desc", Desc);
			}
			if(positionId == 1272){
				try {
					double referPrice = Double.valueOf(ReferPrice).doubleValue();
					ob.put("Price", referPrice);
				} catch (NumberFormatException e) {
					logger.error("parse referPrice is error", e);
					ob.put("Price", 0.0);
				}
				ob.put("PCLink", Url);
				ob.put("MobileLink", MUrl);
				ob.put("Img", Img);
				ob.put("CSName", SerialName);
				if(Desc !=null && !"".equals(Desc)){
					String stence = Desc.substring(0, 2);
					if(stence.equals("平均")){
						Desc = Desc.substring(2, Desc.length());
					}
				}
				ob.put("Desc", Desc);
			}
			jarry.put(ob);
		}
		return jarry.toString();
	}
	
	/**
	 * @decription:处理结果集
	 * @param exchange
	 * @param modelMaterialss
	 * @param positionId
	 * @param deviceId 
	 */
	public void handEndleRequest(HttpServerExchange exchange,Collection<ModelMaterial> modelMaterialss, Integer positionId, Integer deviceId) {
		if (modelMaterialss != null && modelMaterialss.size() == 9) {
			Deque<String> callBackDeque = exchange.getQueryParameters().get(Constant.CALLBACK);// request_cityID
			String callback="";
			String result="";
			if ((callBackDeque == null) || (callBackDeque.size() == 0)) {
				callback="callback";
			}else{
				callback = callBackDeque.peek();
				if(callback == "" || callback.equals("")){
					callback="callback";
				}
			}
			String data = convertToJson(modelMaterialss, positionId);//转换为json数据并加监测代码
			if(data == null || "".equals(data)){
				modelMaterialss = mmService.getTop9(Constant.HUIMAICHE);
				String top9 = convertToJson(modelMaterialss, positionId);
				result = Utils.combineResponseJsonp(top9, callback);
			}
			result = Utils.combineResponseJsonp(data, callback);//加jsonp解决js,jquery跨域支持问题
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain;charset=utf-8");
			exchange.getResponseSender().send(result, Charset.forName("UTF-8"));
			//这块比较恶心，惠买车手机端接口和PC接口合并在一起。
			if(positionId == 1272){
				if(deviceId == 1){
					mmService.add(1274,deviceId);
				}else{
					mmService.add(positionId,deviceId);
				}
			}else{
				mmService.add(positionId,deviceId);
			}
		} else {
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
		}
		return;
	}
	
	/**
	 * @description:获取易车接口物料
	 * @param locationArr
	 * @param us
	 * @param modelFrequency
	 * @param positionIdTyp
	 * @return
	 */
	public Collection<ModelMaterial> getModelMaterialss(int[] locationArr,UserStruct us,Map<Integer, Integer> modelFrequency,int positionIdTyp){
		Collection<ModelMaterial> modelMaterialss = null;// 投放素材+匹配上的车型ids
		if ((locationArr == null)
				|| (locationArr.length == 0)
				|| (locationArr.length >= 2 && locationArr[0] == 0 && locationArr[1] == 0)) {
			if (us == null) {
				modelMaterialss = mmService.getTop9(Constant.HUIMAICHE);
			} else {
				modelMaterialss = mmService.getTopNByAllModels(modelFrequency.keySet(), Constant.HUIMAICHE);
			}
		} else {
			int province = locationArr[0];
			int city = 0;
			if (locationArr.length >= 2) {
				city = locationArr[1];
			}
			if (us == null) {
				modelMaterialss = mmService.getTopNByProvinceAndcity(province, city, Constant.HUIMAICHE);
			} else {
				modelMaterialss = mmService.getTopNByModelsProvinceAndcity(modelFrequency.keySet(), province, city,Constant.HUIMAICHE);
			}
		}
		return modelMaterialss;
	}
}
