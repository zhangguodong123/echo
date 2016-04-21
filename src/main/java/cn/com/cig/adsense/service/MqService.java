package cn.com.cig.adsense.service;

public interface MqService {
	
	public boolean messageProducer(String message);
	
	public void close();
}
