package cn.com.cig.adsense.utils.job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ScheduledJob {

	private final ScheduledExecutorService ses = Executors.newScheduledThreadPool(15, Executors.defaultThreadFactory());
	// 单例
	private ScheduledJob() {
		
	}
	private static class ScheduledJobHolder {
		private static ScheduledJob scheduledJob = new ScheduledJob();
	}
	public static ScheduledJob getInstance() {
		return ScheduledJobHolder.scheduledJob;
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,long initialDelay, long period, TimeUnit unit) {
		return ses.scheduleAtFixedRate(command, initialDelay, period, unit);
	}
	public void close() {
		if (!ses.isShutdown()) {
			ses.shutdown();
		}
	}
}
