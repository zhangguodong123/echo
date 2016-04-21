package cn.com.cig.adsense.utils.date;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
 * 时间缓存
 */
public class SystemTimer {
	
	private static Logger logger = LoggerFactory.getLogger(SystemTimer.class);
	private final static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private static final long tickUnit = Long.parseLong(System.getProperty("notify.systimer.tick", "50"));
	private static volatile long time = System.currentTimeMillis();
	
	static {
		executor.scheduleAtFixedRate(new TimerTicker(), tickUnit, tickUnit,
				TimeUnit.MILLISECONDS);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				executor.shutdown();
			}
		});
	}
	
	private static class TimerTicker implements Runnable {
		public void run() {
			try{
				time = System.currentTimeMillis();
			} catch(Exception e){
				logger.error("SystemTimer update time error!" + e);
			}
		}
	}

	public static long currentTimeMillis() {
		return time;
	}
	
	public static void main(String[] args){
		System.out.println(System.getProperty("notify.systimer.tick"));
		System.out.println(System.getProperty("notify.systimer.tick", "50"));
		System.out.println(System.getProperty("notify.systimer.tick"));
	}
	
}
