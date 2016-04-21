package cn.com.cig.adsense.handler;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.service.BitautoMaterialLibraryService;
import cn.com.cig.adsense.service.impl.BitautoMaterialLibraryServiceImpl;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.vo.Region;
import cn.com.cig.adsense.vo.fix.ModelMaterial;
import cn.com.cig.adsense.vo.fix.Size;
import cn.com.cig.adsense.vo.fix.UserStruct;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import static cn.com.cig.adsense.general.DynamicBiz.getFirstRequestMaterialOfDDV;
import static cn.com.cig.adsense.general.CommonBiz.getUserStruct;
import static cn.com.cig.adsense.general.CommonBiz.getUserModelFrequency;
import static cn.com.cig.adsense.general.DynamicBiz.getModelMaterialss;
import static cn.com.cig.adsense.general.DynamicBiz.getResponseString;
import static cn.com.cig.adsense.general.DynamicBiz.getPositionFlag;
import static cn.com.cig.adsense.general.CommonBiz.getClientIp;
import static cn.com.cig.adsense.general.CommonBiz.getCookieId;


public class DynamicDeliveryHandler implements HttpHandler {
	private static Logger logger = LoggerFactory.getLogger(DynamicDeliveryHandler.class);
	private static final BitautoMaterialLibraryService mls = BitautoMaterialLibraryServiceImpl.getInstance();
	
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		int positionIdTyp = 0;
		Deque<String> positionIdDeque = exchange.getQueryParameters().get(Constant.POSITION_ID);// request_pid
		if ((positionIdDeque == null) || (positionIdDeque.size() == 0)) {
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
			return;
		}
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		try {
			// 下列操作异步执行,在worker线程池中执行.
			String positionId = positionIdDeque.peek();// pid_value
			Size size = mls.getPosIdSizeById(positionId);
			
			if ((size == null) || ("".equals(size))) {
				logger.warn("Position size is empty! positionId:" + positionId);
				exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
					
			boolean inIframe = getFirstRequestMaterialOfDDV(exchange);//处理容灾素材点击问题
			positionIdTyp=getPositionFlag(positionId);//获取业务线的标识状态码
			
			// user id
			String tCookieId = getCookieId(exchange);
			// 自己计算地域，根据地域投.
			String ip=getClientIp(exchange);
			
			//int[] locationArr = Utils.getAreas(ip);// get_Areas 格式:
			Region region = Utils.getRegion(ip);// get_Areas 格式:
			UserStruct us = getUserStruct(tCookieId);
			Map<Integer, Integer> modelFrequency = getUserModelFrequency(us);
			
			Collection<ModelMaterial> modelMaterialss = getModelMaterialss(ip,us,region,modelFrequency,positionIdTyp);// 投放素材+匹配上的车型ids
			if(positionIdTyp==Constant.HUIMAICHE_FIX){
				modelMaterialss=sortByPromotion(modelMaterialss);
			}
			if (modelMaterialss == null || modelMaterialss.size() == 0) {
				logger.debug(("data为空!" + modelMaterialss + " positionId:" + positionId));
				exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// TODO 弄个补漏的物料投
				return;
			} 
			
			try{
				String responseString = getResponseString(modelMaterialss,size,positionId,positionIdTyp);
				// 第一次请求只返回广告位id和尺寸，第二次请求(inIframe)返回内容。
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/html; charset=utf-8");
				if (inIframe) {
					exchange.getResponseSender().send(responseString,Charset.forName("UTF-8"));
				} else {
					String urlReturned = Utils.createDynamicDeliveryCallbackStr(positionId, size.getWidth(), size.getHeight());
					exchange.getResponseSender().send(urlReturned,Charset.forName("UTF-8"));
					return;
				}
			}catch(Exception e){
				logger.error(e.getMessage(), e);
				exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		} catch (Exception e) {
			// TODO 出问题了，弄个补漏的物料投，动态投放老莫不会补漏。。
			logger.error(e.getMessage(), e);
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
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
}
