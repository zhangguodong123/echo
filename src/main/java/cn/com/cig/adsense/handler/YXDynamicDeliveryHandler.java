package cn.com.cig.adsense.handler;

import static cn.com.cig.adsense.general.CommonBiz.getCookieId;
import static cn.com.cig.adsense.general.CommonBiz.getUserModelFrequency;
import static cn.com.cig.adsense.general.CommonBiz.getUserStruct;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Deque;
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
import cn.com.cig.adsense.vo.fix.UserStruct;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * @File: YXDynamicDeliveryHandler.java
 * @Package cn.com.cig.adsense.handler
 * @Description: TODO
 * @author zhangguodong
 * @date 2015年5月11日 下午2:42:21
 * @version V1.0
 */
public class YXDynamicDeliveryHandler implements HttpHandler {
	private static Logger logger = LoggerFactory.getLogger(HMCDynamicDeliveryHandler.class);
	private static final ModelMaterialService mmService = ModelMaterialServiceImpl.getInstance();
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

		try {
			Integer cityId = null;
			try {
				cityId = Integer.valueOf(cid);
			} catch (NumberFormatException e) {
				modelMaterialss = mmService.getTop9(Constant.YIXIN);
				handEndleRequest(exchange, modelMaterialss, positionId,deviceId);// 结束方法返回
				return;
			}
			
			// 自己计算地域，根据地域投.
			int[] locationArr = new int[] { 0, cityId, 0 };
			String tCookieId = getCookieId(exchange);
			UserStruct us = getUserStruct(tCookieId);
			Map<Integer, Integer> modelFrequency = getUserModelFrequency(us);
			modelMaterialss=getModelMaterialss(locationArr,us,modelFrequency,Constant.YIXIN);
			
			handEndleRequest(exchange, modelMaterialss, positionId,deviceId);// 结束方法返回
		} catch (Exception e) {
			// TODO 出问题了，弄个补漏的物料投，动态投放老莫不会补漏。。
			logger.error(e.getMessage(), e);
			modelMaterialss = mmService.getTop9(Constant.YIXIN);
			handEndleRequest(exchange, modelMaterialss, positionId,deviceId);// 结束方法返回
		}
	}
	
	/**
	 * @description:将结果转换为json 数据输出
	 * @param modelMaterialss
	 * @param positionId
	 * @return
	 */
	public static String convertToJson(Collection<ModelMaterial> modelMaterialss,String positionId) {
		Collection<ModelMaterial> generateYXCheMonitorCode = Utils.generateYXMonitorCode(Constant.BITAUTO, positionId,modelMaterialss, 1, true);
		JSONArray jarry = new JSONArray();
		int i = 0;
		for (ModelMaterial mo : generateYXCheMonitorCode) {
			if(i >= 8){
				break;
			} else {
				i++;
			}
			JSONObject ob = new JSONObject();
			Integer SerialID = mo.getModelId();
			String Url = mo.getClickMonitorCode();
			String Img = mo.getPic210X140();
			String SerialName = mo.getName();
			int applyCount = mo.getApplyCount();
			String monthPay = mo.getMonthPay();
			
			ob.put("SerialID", SerialID);
			ob.put("Url", Url);
			ob.put("Img", Img);
			ob.put("ApplyCount", applyCount);
			ob.put("MonthPay", monthPay);
			ob.put("SerialName", SerialName);
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
	public void handEndleRequest(HttpServerExchange exchange,Collection<ModelMaterial> modelMaterialss, String positionId, Integer deviceId) {
		if (modelMaterialss != null && modelMaterialss.size() == Constant.ONE_DELIVERY_NUM) {
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
				modelMaterialss = mmService.getTop9(Constant.YIXIN);
				String top9 = convertToJson(modelMaterialss, positionId);
				result = Utils.combineResponseJsonp(top9, callback);
			}
			result = Utils.combineResponseJsonp(data, callback);//加jsonp解决js,jquery跨域支持问题
			exchange.getResponseSender().send(result, Charset.forName("UTF-8"));
			mmService.add(Integer.parseInt(positionId),deviceId);
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
				modelMaterialss = mmService.getTop9(Constant.YIXIN);
			} else {
				modelMaterialss = mmService.getTopNByAllModels(modelFrequency.keySet(), Constant.YIXIN);
			}
		} else {
			int province = locationArr[0];
			int city = 0;
			if (locationArr.length >= 2) {
				city = locationArr[1];
			}
			if (us == null) {
				modelMaterialss = mmService.getTopNByProvinceAndcity(province, city, Constant.YIXIN);
			} else {
				modelMaterialss = mmService.getTopNByModelsProvinceAndcity(modelFrequency.keySet(), province, city,Constant.YIXIN);
			}
		}
		return modelMaterialss;
	}
}
