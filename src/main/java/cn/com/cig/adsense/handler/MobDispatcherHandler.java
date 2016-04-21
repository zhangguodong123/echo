package cn.com.cig.adsense.handler;

import static cn.com.cig.adsense.general.FixtureBiz.getPostString;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import cn.com.cig.adsense.general.FixtureBiz;
import cn.com.cig.adsense.service.BitautoMaterialLibraryService;
import cn.com.cig.adsense.service.impl.BitautoMaterialLibraryServiceImpl;
import cn.com.cig.adsense.vo.PlatefromEnum;
import cn.com.cig.adsense.vo.fix.Position;
import cn.com.cig.adsense.vo.fix.PositionTypeEnum;
import cn.com.cig.adsense.vo.fix.Query;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.MimeMappings;

/**
 * @File: MobDispatcherHandler.java
 * @Package cn.com.cig.adsense.handler
 * @Description: TODO
 * @author zhangguodong
 * @date 2016年3月7日 下午4:57:37
 * @version V1.0
 */
public class MobDispatcherHandler implements HttpHandler {
	private static Logger logger = LoggerFactory.getLogger(MobDispatcherHandler.class);
	private static final BitautoMaterialLibraryService mls = BitautoMaterialLibraryServiceImpl.getInstance();

	private AppDeliveryHandler appHandler = new AppDeliveryHandler();
	private AppPMPDeliveryHandler appPmpHandler = new AppPMPDeliveryHandler();
	private static Gson gson = new Gson();
	
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		switch (exchange.getRequestMethod().toString()) {
			case "POST":
				HeaderValues contentType=exchange.getRequestHeaders().get(Headers.CONTENT_TYPE);
				String mimeJson = MimeMappings.DEFAULT.getMimeType("json");
				if(!contentType.contains(mimeJson)){
					exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
					return;
				}
				HeaderValues contentLength=exchange.getRequestHeaders().get(Headers.CONTENT_LENGTH);
				if(contentLength == null || contentLength.size() == 0 || Integer.parseInt(contentLength.getFirst()) <= 0){
					exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404 
					return;
				}
				int limit = Integer.parseInt(contentLength.getFirst());
				String post = getPostString(exchange,limit);
				if(post == null || "".equals(post)){
					exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404 
					return;
				}
				try{
					Query query = gson.fromJson(post, Query.class);
					logger.info("post:"+post);//日志打印
					query=FixtureBiz.recodeQuery(query);//处理标签6位数的问题
					String pubid = query.getPubid();
					Integer pid = query.getPid();
					String dvid = query.getDvid();
					
					if ((pubid == null || "".equals(pubid)) || (pid == null || pid == 0) || (dvid == null || "".equals(dvid))) {
						logger.error("Illegal request[pubid=%s,dvid=%s,pid=%s,ip=%s,cityid=%s,tags=%s]",pubid,dvid,pid);
						exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
						return;
					}
					
					Position position = mls.getPositionById(pid.toString()); // 频道信息
					if(position== null){
						logger.error("Illegal pid:"+pid+" ******");
						exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
						return;
					}
					PlatefromEnum platType = position.getMediaType();
					PositionTypeEnum pidType = position.getType();
					Integer pmpId = mls.getPMPAdvisterID(pid); // 是否位PMP广告位ID
					switch (platType) {
						case APP:
							switch (pidType) {
								case ELETEXT:
									if (pmpId == null || pmpId == 0) {
										appHandler.setQuery(query);
										appHandler.handleRequest(exchange);
									} else {
										appPmpHandler.setQuery(query);
										appPmpHandler.handleRequest(exchange);
									}
									break;
								default:
									logger.error("Illegal pid:"+pid+" ******");
									exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
									break;
							}
							break;
						default:
							logger.error("Illegal pid:"+pid+" ******");
							exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
							break;
					}
					
				}catch(JsonSyntaxException e){
					logger.error("e:"+e.getMessage(),e);
					exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
				}
				break;
			default:
				exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
				break;
		}
	}
}
