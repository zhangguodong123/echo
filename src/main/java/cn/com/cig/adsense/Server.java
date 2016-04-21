package cn.com.cig.adsense;

import static io.undertow.Handlers.path;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.accesslog.DefaultAccessLogReceiver;
import io.undertow.util.Headers;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.handler.BitautoDeliveryHandler;
import cn.com.cig.adsense.handler.CigDeliveryHandler;
import cn.com.cig.adsense.handler.DynamicUiInterfaceHandler;
import cn.com.cig.adsense.handler.MobDispatcherHandler;
import cn.com.cig.adsense.handler.UiInterfaceHandler;
import cn.com.cig.adsense.service.impl.DmpServiceImpl;
import cn.com.cig.adsense.step.Initialize;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.utils.date.DateUtil;
import cn.com.cig.adsense.utils.http.AsyncHttpClientUtils;
import cn.com.cig.adsense.utils.job.ScheduledJob;
import cn.com.cig.adsense.vo.DateEnum;
/**
 * @description: start Server
 * @author zhanggd
 *
 */
public class Server{
	private static Logger logger = LoggerFactory.getLogger(Server.class);
	public static void main(String[] args) {
		logger.info("Server start...");
		// 启动时指定host和port.
		if (args.length != 2) {
			logger.info("Missing parameter host or port! example:localhost 8080");
			System.exit(1);
		}
		String host = args[0];
		int port = Integer.parseInt(args[1]);
		Initialize.startup(host,port);						//初始化每台服务器ServerId
		Initialize.consulKVListener(Constant.ECHO_ROOT,10); //配置文件监听
		// 添加accessLogs
		PathHandler path = path();
		path.addPrefixPath("/i", new CigDeliveryHandler());
		path.addPrefixPath("/bitai", new BitautoDeliveryHandler());
		path.addPrefixPath("/ssp/v1.0/yicheappplace/post", new MobDispatcherHandler());
		path.addPrefixPath("/ui", new UiInterfaceHandler());
		path.addPrefixPath("/dyn", new DynamicUiInterfaceHandler());
		path.addPrefixPath("/test", new HttpHandler() {
					@Override
					public void handleRequest(HttpServerExchange exchange)throws Exception {
						exchange.getResponseHeaders().put(Headers.CONTENT_TYPE,"text/plain");
						exchange.getResponseSender().send("Welcome to " + Utils.getAddress() + ":"+ exchange.getHostPort());
					}
				});
		final ExecutorService accessLogExecutorService = Executors.newFixedThreadPool(12);
		File accessLogs = new File(Constant.ACCESS_LOG_PATH_PREFIX);
		DefaultAccessLogReceiver dal = new DefaultAccessLogReceiver(accessLogExecutorService, accessLogs, "echo-access-log-" + DateUtil.dateFormatText(DateEnum.FORMAT_DATE_1,new Date()), ".log");
		AccessLogHandler accessLogHandler = new AccessLogHandler(path,dal, "combined", Server.class.getClassLoader());
		
		try {
			Thread.sleep(180000);
			final Undertow server = Undertow
					.builder()
					.setHandler(accessLogHandler)
					.addHttpListener(port, host)
					.build();
			
			server.start();
			shutdownServer(server);
			logger.info("Server Start completed.......................................");
		} catch (InterruptedException e) {
			logger.error("Error:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @description 关闭服务器，关闭相关服务。
	 * @param server
	 */
	private static void shutdownServer(Undertow server) {
		/*退出清理*/
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            	logger.info("Server stop...");
            	try{
                 	AsyncHttpClientUtils.getInstance().closed();
                } catch(Exception e){
                 	logger.error(e.getMessage(), e);
                }
                try{
                	server.stop();
                } catch(Exception e){
                	logger.error(e.getMessage(), e);
                }
                try{
                	ScheduledJob.getInstance().close();
                } catch(Exception e3){
                	logger.error(e3.getMessage(), e3);
                }
                try{
                	DmpServiceImpl.getInstance().close();
                } catch(Exception e4){
                	logger.error(e4.getMessage(), e4);
                }
                logger.info("Server stop completed.");
            }
        });
	}
}
