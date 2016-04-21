package cn.com.cig.adsense.handler;

import java.util.Deque;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;

public class CigDeliveryHandler implements HttpHandler {

	private HttpHandler fixtureDeliveryHandler = new FixtureDeliveryHandler();
	private HttpHandler dynamicDeliveryHandler = new DynamicDeliveryHandler();
	
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		
		Deque<String> positionIdDeque = exchange.getQueryParameters().get(Constant.POSITION_ID);
		if ((positionIdDeque == null) || (positionIdDeque.size() == 0)) {
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		
		Map<String, Cookie> cookies = exchange.getRequestCookies();
		Cookie idCookie = null;
		if(cookies.containsKey(Constant.COOKIE_ID)){
			idCookie = exchange.getRequestCookies().get(Constant.COOKIE_ID);
		}
		if ((idCookie == null) || (idCookie.getValue() == null)
				|| ("".equals(idCookie.getValue()))) {
			Utils.handleNewCookie(exchange);
		}
		String positionId = positionIdDeque.peek();
		if(Constant.CIG_LIST.contains(positionId)){
			//使用动态投放
			dynamicDeliveryHandler.handleRequest(exchange);
		} else {
			//使用固定位投放
			fixtureDeliveryHandler.handleRequest(exchange);
		}
	}
	
}
