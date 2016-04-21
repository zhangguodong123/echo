package cn.com.cig.adsense.dao;

import java.util.List;

import cn.com.cig.adsense.vo.fix.UserStruct;

public interface RpcDao {

	public List<Integer> getUrlTags(String url);

	public UserStruct getUserStruct(String cookie);
	
	public void close();
	
}
