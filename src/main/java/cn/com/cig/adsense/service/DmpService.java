package cn.com.cig.adsense.service;

import java.util.List;

import cn.com.cig.adsense.vo.fix.UserStruct;

public interface DmpService {

	public UserStruct getUserStruct(String cookie);
	
	public List<Integer> getUrlTags(String url);
	
	public void close();
	
}
