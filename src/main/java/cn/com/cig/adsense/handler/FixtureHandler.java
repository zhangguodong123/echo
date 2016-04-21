package cn.com.cig.adsense.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.Deque;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.service.BitautoMaterialLibraryService;
import cn.com.cig.adsense.service.impl.BitautoMaterialLibraryServiceImpl;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.vo.fix.Position;
import cn.com.cig.adsense.vo.fix.PositionTypeEnum;

/**   
 * @File: FixtureHandler.java 
 * @Package cn.com.cig.adsense.handler 
 * @Description: Processing precision orientation request
 * @author zhangguodong   
 * @date 2015年9月14日 上午9:50:49 
 * @version V1.0   
 */
public class FixtureHandler  implements HttpHandler {
	private static Logger logger = LoggerFactory.getLogger(FixtureHandler.class);
	private static final BitautoMaterialLibraryService mls = BitautoMaterialLibraryServiceImpl.getInstance();
	
	private HttpHandler fixtureDeliveryHandler=new FixtureDeliveryHandlerForBitauto();
	private HttpHandler pmpFixtureDeliveryHandler = new FixturePMPDeliveryHandler();
	
	private HttpHandler textFixTureDeliveryHandler = new TextDeliveryHandler();
	private HttpHandler pmpTextFixTureDeliveryHandler = new FixturePMPTextDeliveryHandler();
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		Deque<String> positionIdDeque = exchange.getQueryParameters().get(Constant.POSITION_ID);//request_pid
		if ((positionIdDeque == null) || (positionIdDeque.size() == 0)) {
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);//404
			return;
		}
		try{
			String positionId = positionIdDeque.peek();                      //广告位ID
			Integer pid = Integer.valueOf(positionId); 
			Integer pmpId = mls.getPMPAdvisterID(pid);                       //是否位PMP广告位ID
			Position position = mls.getPositionById(positionId);             //频道信息
			if(pmpId == null || pmpId == 0){
				if(position!=null && position.getType()==PositionTypeEnum.TEXT){
					textFixTureDeliveryHandler.handleRequest(exchange);      //精准文本投放
				}else{
					fixtureDeliveryHandler.handleRequest(exchange);	         //精准图片投放
				}
			}else{
				if(position!=null && position.getType()==PositionTypeEnum.TEXT){
					pmpTextFixTureDeliveryHandler.handleRequest(exchange);   //pmp精准文本投放
				}else{
					pmpFixtureDeliveryHandler.handleRequest(exchange);	     //pmp精准图片投放
				}
			}
		}catch(Exception e){
			logger.error(e.getMessage(), e);
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
	}

}
