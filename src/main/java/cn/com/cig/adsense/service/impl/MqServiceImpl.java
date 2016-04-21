package cn.com.cig.adsense.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import cn.com.cig.adsense.service.MqService;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.date.SystemTimer;
import cn.com.cig.adsense.utils.job.ScheduledJob;

public class MqServiceImpl implements MqService {
	
	private static Logger logger = LoggerFactory.getLogger(MqServiceImpl.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH");
	
	// kestrel.echo.impression.queue=echoImpressionTuple
	//private WeightedRoundRobinKestrelClient mqClient = new WeightedRoundRobinKestrelClient(Constant.MQ_KEY);
	
	private BlockingQueue<String> queue = new ArrayBlockingQueue<>(10_000);
	//TODO 上线时加大这个配置。
	private int threadNum = 2;
	private ExecutorService es = Executors.newFixedThreadPool(threadNum);
	// request写入文件。
	private BlockingQueue<String> toFileQueue = new ArrayBlockingQueue<>(20_000);
	
	@Override
	public boolean messageProducer(String message) {
		boolean result = false;
		result = queue.offer(message);
		if(!result){
			logger.error("The message cannot be inserted into the queue! message:" + message);
		}
		return result;
	}
	
	private MqServiceImpl(){
		for(int i=0;i<threadNum;i++){
			es.execute(sendMessage);
		}
		ScheduledJob.getInstance().scheduleAtFixedRate(writeMessageToFile, 1,
				3, TimeUnit.SECONDS);
	}
	
	private Runnable sendMessage = new Runnable() {
		@Override
		public void run() {
			try{
				while(true){
					String message = null;
					try {
						message = queue.take();
						if((message==null) || (message.length()==0)){
							continue;
						}
						boolean result = toFileQueue.offer(message);
						if (!result) {
							logger.error("Message add to toFileQueue failed! message:"
									+ message);
						}
						//result = mqClient.messageProducer(message);
						if (!result) {
							logger.error("Message send failed! message:"
									+ message);
						}
					} catch (Exception e) {
						logger.error("Process message failed! message:" + message, e);
					}
				}
			} catch(Exception e) {
				logger.error("MqService scheduled job sendMessage error!", e);
			}
		}
	};
	
	private Runnable writeMessageToFile = new Runnable() {
		public void run() {
			try{
				if(toFileQueue.isEmpty()){
					return;
				}
				StringBuilder sb = new StringBuilder();
				// 一次最多取50000条消息，防止一直poll消息不写硬盘，内存溢出。
				int pollMessageCount = 0;
				while (pollMessageCount < 50_000) {
					pollMessageCount++;
					String message = null;
					try {
						message = toFileQueue.poll(50,
								TimeUnit.MILLISECONDS);
						if(message == null){
							break;
						} else {
							sb.append(message);
							sb.append("\n");
						}
					} catch (InterruptedException e1) {
						logger.error("Lost message:" + message, e1);
						continue;
					}
				}
				if(sb.length() > 0){
					try {
						File requestLog = new File(getRequestLogFileName());
						Files.write(Paths.get(requestLog.toURI()),
								sb.toString().getBytes("utf-8"),
								StandardOpenOption.CREATE,
								StandardOpenOption.APPEND);
					} catch (IOException e) {
						logger.error(
								"Write message to File failed! messages:"
										+ sb.toString(), e);
					}
				}
			} catch(Exception e) {
				logger.error("MqService scheduled job writeMessageToFile error!", e);
			}
		}
	};
	
	/*
	 * 只保留最近一个月的log。
	 */
	private String getRequestLogFileName() {
		// 使用日期作为文件名
		String logFileName = sdf.format(new Date(SystemTimer
				.currentTimeMillis()));
		return Constant.REQUEST_LOG_PATH_PREFIX + logFileName;
	}
	
	// 单例
	private static class MqClientHolder {
		private static MqService mqService = new MqServiceImpl();
	}
	public static MqService getInstance(){
		return MqClientHolder.mqService;
	}
	@Override
	public void close() {
		try{
			es.shutdown();
		} catch(Exception e){
			e.printStackTrace();
		}
		try{
			//mqClient.close();
		} catch(Exception e){
			e.printStackTrace();
		}
	}

}
