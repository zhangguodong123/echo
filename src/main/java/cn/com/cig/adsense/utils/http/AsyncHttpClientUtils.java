package cn.com.cig.adsense.utils.http;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.vo.HttpMethodEnum;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Param;
import com.ning.http.client.Response;

/**   
 * @File: AsyncHttpClientUtils.java 
 * @Package cn.com.cig.adsense.utils 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年9月17日 下午4:13:44 
 * @version V1.0   
 */
public class AsyncHttpClientUtils {
	private static Logger logger = LoggerFactory.getLogger(AsyncHttpClientUtils.class);
	private AsyncHttpClient get = new AsyncHttpClient(
			new AsyncHttpClientConfig.Builder().setConnectTimeout(100)
					.setReadTimeout(200)
					.setMaxConnections(30000)
					.setPooledConnectionIdleTimeout(1)
					.setCompressionEnforced(true)
					.setRequestTimeout(100)
					.build());
	
	private AsyncHttpClient post = new AsyncHttpClient(
			new AsyncHttpClientConfig.Builder().setConnectTimeout(100)
					.setReadTimeout(10000)
					.setMaxConnections(30000)
					.setPooledConnectionIdleTimeout(50)
					.setCompressionEnforced(true)
					.setRequestTimeout(500)
					.build());

	/**
	 * description:asyn http client
	 * 
	 * @param url
	 * @return
	 */
	public String asyncHttpUrl(String url) {
		String result = null;
		if (url == null || "".equals(url)) {
			logger.error("url is null");
		}
		try {
			Future<Response> f = get.prepareGet(url).execute(
					new AsyncCompletionHandler<Response>() {
						@Override
						public Response onCompleted(Response response)
								throws Exception {
							return response;
						}
					});
			result = f.get(300, TimeUnit.MILLISECONDS).getResponseBody();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}
	
	public String asyncHttp(String url,HttpMethodEnum type,List<Param> params){
		String data = null;
		if (url == null || "".equals(url)) {
			logger.error("url is null");
		}
		switch(type){
			case GET:
				try {
					Future<Response> f = post.prepareGet(url).execute(
							new AsyncCompletionHandler<Response>() {
								@Override
								public Response onCompleted(Response response)
										throws Exception {
									return response;
								}
							});
					data = f.get(300, TimeUnit.MILLISECONDS).getResponseBody();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				break;
			case POST:
				try {
					Future<Response> f=post.preparePost(url).setFormParams(params).execute(new AsyncCompletionHandler<Response>(){
						@Override
						public Response onCompleted(Response response) throws Exception {
							return response;
						}
					});
					data = f.get(3000, TimeUnit.MILLISECONDS).getResponseBody();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				break;
			default:
				break;
				
 		}
		return data;
	}

	private AsyncHttpClientUtils() {
	}

	private static class AsyncHttpClientUtilsHolder {
		private static AsyncHttpClientUtils ahc = new AsyncHttpClientUtils();
	}

	public static AsyncHttpClientUtils getInstance() {
		return AsyncHttpClientUtilsHolder.ahc;
	}
	
	public  void closed(){
		if(get != null || post != null){
			get.closeAsynchronously();
			post.closeAsynchronously();
			get.close();
			post.close();
		}
	}
}
