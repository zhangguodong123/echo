package cn.com.cig.adsense.handler;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import cn.com.cig.adsense.utils.Utils;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.general.CommonBiz;
import cn.com.cig.adsense.service.BitautoMaterialLibraryService;
import cn.com.cig.adsense.service.impl.BitautoMaterialLibraryServiceImpl;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.vo.Region;
import cn.com.cig.adsense.vo.fix.ModelMaterial;
import cn.com.cig.adsense.vo.fix.Position;
import cn.com.cig.adsense.vo.fix.Size;
import cn.com.cig.adsense.vo.fix.UserStruct;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import static cn.com.cig.adsense.general.CommonBiz.getClientIp;
import static cn.com.cig.adsense.general.DynamicBiz.getModelMaterialss;
import static cn.com.cig.adsense.general.DynamicBiz.getPositionFlag;
import static cn.com.cig.adsense.general.DynamicBiz.getResponseString;
import static cn.com.cig.adsense.general.CommonBiz.getUserStruct;
import static cn.com.cig.adsense.general.CommonBiz.getCookieId;
import static cn.com.cig.adsense.general.CommonBiz.getUserModelFrequency;

public class DynamicUiInterfaceHandler implements HttpHandler {
	private static Logger logger = LoggerFactory.getLogger(DynamicUiInterfaceHandler.class);
	private static final BitautoMaterialLibraryService mls = BitautoMaterialLibraryServiceImpl.getInstance();
	
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
		if(keyPeek != null && keyPeek.equals(Constant.KEY)){
			try{
				StringWriter writer=getDynamicPositionOutString(exchange,getDynamicPosition(exchange,mls.getAllPositions()));
				exchange.setResponseCode(HttpServletResponse.SC_OK);
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain;charset=utf-8");
				exchange.getResponseSender().send(writer.toString(),Charset.forName("UTF-8"));
			}catch(Exception e){
				logger.error("error:"+e.getMessage(),e);
				exchange.setResponseCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}else{
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}
	
	/**
	 * @description 获取动态广告输出结果
	 * @param exchange
	 * @param dynamicPids
	 * @return
	 */
	public StringWriter getDynamicPositionOutString(HttpServerExchange exchange, List<Position> dynamicPids) {
		StringWriter writer=new StringWriter();
		if(dynamicPids ==null || dynamicPids.size() ==0 ){
			logger.error("dynamicPids is null");
			return writer;
		}
		for(int i=0;i<dynamicPids.size();i++){
			Position position = dynamicPids.get(i);
			String pid = position.getId();
			String ip = getClientIp(exchange);
			// user id
			String tCookieId = getCookieId(exchange);
			Size size = mls.getPosIdSizeById(pid);
			
			if ((size == null) || ("".equals(size))) {
				logger.warn("Position size is empty! positionId:" + pid);
				continue;
			}
			
			//int[] locationArr = Utils.getAreas(ip);// get_Areas 格式:
			Region region = Utils.getRegion(ip);// get_Areas 格式:
			int positionIdTyp=getPositionFlag(pid);//需要修改的地方
			
			UserStruct us = getUserStruct(tCookieId);
			Map<Integer, Integer> modelFrequency = getUserModelFrequency(us);
			
			Collection<ModelMaterial> modelMaterialss = getModelMaterialss(ip,us,region,modelFrequency,positionIdTyp);// 投放素材+匹配上的车型ids
			String responseString = null;
			try {
				responseString = getResponseString(modelMaterialss,size,pid,positionIdTyp);
			} catch (Exception e) {
				logger.error("responseString is error",e);
				continue;
			}
			writer.append(responseString);
		}
		return writer;
	}
	
	/**
	 * @description:获取动态广告位ID集
	 * @param exchange
	 * @param positions
	 * @return
	 */
	public static List<Position> getDynamicPosition(HttpServerExchange exchange,List<Position> positions){
		List<Position> dynamicPids = new ArrayList<Position>();
		positions
		.stream()
		.forEach(
				position -> {
					String positionId = position.getId();
					if (Constant.GROUPON_LIST.contains(positionId)) {
						if (Constant.ADAID_AREAS.containsKey(positionId)) {
							// 加地域限制
							int[] locationArr = CommonBiz.getLocationAreas(exchange);
							if (((locationArr != null) && (locationArr.length != 0))||(locationArr.length >= 2 && locationArr[0] == 0 && locationArr[1] == 0)) {
								int province = locationArr[0];
								int city = 0;
								if (locationArr.length >= 2) {
									city = locationArr[1];
								}
								List<String> areas = Constant.ADAID_AREAS.get(positionId);
								if (areas.contains(String.valueOf(province))|| areas.contains(String.valueOf(city))) {
									// 使用动态投放
									dynamicPids.add(position);
								} 
							}
						} else {
							// 使用动态投放，团购列表有，地域限制里没有该广告位ID
							dynamicPids.add(position);
						}
					} 
					if (Constant.BITAUTO_LIST.contains(positionId)
							|| Constant.ACTIVITY_LIST.contains(positionId)
							|| Constant.HUIMAICHE_DLIST.contains(positionId)) {
						// 使用动态投放
						dynamicPids.add(position);
					}
				});
		return dynamicPids;
	}
}
