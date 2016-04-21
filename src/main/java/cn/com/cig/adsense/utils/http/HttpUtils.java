package cn.com.cig.adsense.utils.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.vo.HttpMethodEnum;

/**   
 * @File: HttpUtils.java 
 * @Package cn.com.cig.adsense.utils 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年12月24日 下午2:38:58 
 * @version V1.0   
 */
public class HttpUtils {
	private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	/**
	 * GET  type:
     *    for example: httpClient("http://www.yiche.com/info?key=123",Method.GET,null)
     * POST type:
     *    for example: httpClient("http://www.yiche.com/info",Method.POST,"key=123456")
	 * @param urlPath
	 * @param method
	 * @param params
	 * @return
	 */
    public static String httpClient(String urlPath,HttpMethodEnum method,StringBuilder params){
    	if(urlPath ==null || "".equals(urlPath)){
    		logger.warn("The url is empty!");
    		return null;
    	}
    	StringBuilder builder = new StringBuilder();
    	HttpURLConnection conn=null;
		InputStream inputStream=null;
		InputStreamReader inputStreamReader=null;
		BufferedReader bufferedReader=null;
    	try{
    		URL url = new URL(urlPath);
    		conn = (HttpURLConnection) url.openConnection();
    		conn.setConnectTimeout(30000);//连接超时时间
    		conn.setReadTimeout(60000);   //读取超时时间,一分钟的延迟时间
    		conn.setUseCaches(false);
        	switch(method){
	    		case GET:{
	    			conn.setRequestMethod("GET");
	    			break;
	    		}
	    		case POST:{
	    			conn.setRequestMethod("POST");
	    			if(params !=null && params.length()>0){
	    				conn.setDoOutput(true);// 是否输入参数
	    				byte[] bytes = params.toString().getBytes();
	    				conn.getOutputStream().write(bytes);// 输入参数
	    				conn.getOutputStream().flush();
	    			}
	    			break;
	    		}
				default:
					break;
	        	}
    		//数据处理
    		int response_code = conn.getResponseCode();
			if(response_code == HttpURLConnection.HTTP_OK) {
				inputStream = conn.getInputStream();
				inputStreamReader = new InputStreamReader(inputStream, "utf-8");
				bufferedReader = new BufferedReader(inputStreamReader);
				while(true){
					final String line=bufferedReader.readLine();
					if(line == null) break;
					builder.append(line);
				}
			}else{
				logger.warn("request:"+urlPath+" is fault:"+response_code);
			}
    	}catch(Exception e){
    		logger.error(e.getMessage(),e);
    	}finally{
    		if(bufferedReader != null){
				try {
					bufferedReader.close();
				} catch (IOException e) {
					logger.error(e.getMessage(),e);
				}
			}
			
			if(inputStreamReader != null){
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					logger.error(e.getMessage(),e);
				}
			}
			
			if(inputStream != null){
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error(e.getMessage(),e);
				}
			}
			
			if(conn !=null){
	           conn.disconnect();
	        }
    	}
    	return builder.toString();
    }
}
