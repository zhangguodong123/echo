package cn.com.cig.adsense.handler;

import java.util.Deque;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.general.CommonBiz;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import static cn.com.cig.adsense.general.CommonBiz.embedCookie;

public class BitautoDeliveryHandler implements HttpHandler {

	private HttpHandler fixtureDeliveryHandler = new FixtureHandler();
	private HttpHandler dynamicDeliveryHandler = new DynamicDeliveryHandler();
	private HttpHandler hmcdynamicDeliveryHandler = new HMCDynamicDeliveryHandler();
	private HttpHandler yxdynamicDeliveryHandler = new YXDynamicDeliveryHandler();

	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		Deque<String> positionIdDeque = exchange.getQueryParameters().get(Constant.POSITION_ID);
		if ((positionIdDeque == null) || (positionIdDeque.size() == 0)) {
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		embedCookie(exchange);//seed cookie
		String positionId = positionIdDeque.peek();// request_pid

		if (Constant.GROUPON_LIST.contains(positionId)) {
			if (Constant.ADAID_AREAS.containsKey(positionId)) {
				// 加地域限制
				int[] locationArr = CommonBiz.getLocationAreas(exchange);
				if ((locationArr == null)
						|| (locationArr.length == 0)
						|| (locationArr.length >= 2 && locationArr[0] == 0 && locationArr[1] == 0)) {
					// 使用精准投放
					fixtureDeliveryHandler.handleRequest(exchange);
				} else {
					int province = locationArr[0];
					int city = 0;
					if (locationArr.length >= 2) {
						city = locationArr[1];
					}
					List<String> areas = Constant.ADAID_AREAS.get(positionId);
					if (areas.contains(String.valueOf(province))
							|| areas.contains(String.valueOf(city))) {
						// 使用动态投放
						dynamicDeliveryHandler.handleRequest(exchange);
					} else {
						// 使用精准投放
						fixtureDeliveryHandler.handleRequest(exchange);
					}
				}
			} else {
				// 使用动态投放，团购列表有，地域限制里没有该广告位ID
				dynamicDeliveryHandler.handleRequest(exchange);
			}
		} else if (Constant.BITAUTO_LIST.contains(positionId)
				|| Constant.ACTIVITY_LIST.contains(positionId)
				|| Constant.HUIMAICHE_DLIST.contains(positionId)		
				|| Constant.USEDCAR_LIST.contains(positionId)
				|| Constant.YX_DLIST.contains(positionId)
				|| Constant.YCH_LIST.contains(positionId)
				|| Constant.HMC_ONE_LIST.contains(positionId)
				|| Constant.HMC_TWO_LIST.contains(positionId)
				|| Constant.HMC_FOUR_LIST.contains(positionId)
				){
			// 使用动态投放
			dynamicDeliveryHandler.handleRequest(exchange);
		} else if (Constant.HUIMAICHE_ILIST.contains(positionId)) {
			hmcdynamicDeliveryHandler.handleRequest(exchange);// 使用动态投放
		} else if(Constant.YX_ILIST.contains(positionId)){
			yxdynamicDeliveryHandler.handleRequest(exchange);
		} else{
			// 使用精准投放
			fixtureDeliveryHandler.handleRequest(exchange);
		}
	}
}
