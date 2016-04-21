package cn.com.cig.adsense.service;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import cn.com.cig.adsense.utils.job.ScheduledJob;

/**   
 * @File: JobFixThreadTest.java 
 * @Package cn.com.cig.adsense.service 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年7月16日 下午5:06:26 
 * @version V1.0   
 */
public class JobFixThreadTest {
	
	{
		ScheduledJob.getInstance().scheduleAtFixedRate(matchYipaiRegionsTask,0, 1, TimeUnit.DAYS);// 每天更新一次。
	}
	
	
	private static Runnable matchYipaiRegionsTask=new Runnable() {
		
		@Override
		public void run() {
			System.out.println("hellow");
		}
	};
	
	@Test
	public void test() throws Exception{
		Thread.sleep(10000000);
	}
}
