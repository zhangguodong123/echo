package cn.com.cig.adsense.service.impl;

import java.util.List;
import java.util.regex.Matcher;

import cn.com.cig.adsense.dao.impl.RpcDaoRedisCluster;
import cn.com.cig.adsense.service.DmpService;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.vo.fix.UserStruct;

public class DmpServiceImpl implements DmpService {
	private static final RpcDaoRedisCluster rpcDao = RpcDaoRedisCluster.getInstance();

	@Override
	public UserStruct getUserStruct(String cookie) {
		return rpcDao.getUserStruct(cookie);
	}
	
	@Override
	public List<Integer> getUrlTags(String url) {
		// 清理Headers_REFERER中监测代码(防止加了代码的url无法匹配，无法进行内容投放)
		// 只处理易车的url
		if((url != null) && (url.contains("bitauto.com")||url.contains("yiche.com"))){
			Matcher matcher = Constant.HEADERS_REFERER_CLEANER.matcher(url);
			if(matcher.find()){
				String group = matcher.group();
				if(".html?".equals(group)){
					url = url.substring(0, matcher.start() + 5);
				} else {
					url = url.substring(0, matcher.start());
				}
			}
		}
		return rpcDao.getUrlTags(url);
	}
	
	// 单例
	private DmpServiceImpl() {
	}
	
	private static class DmpServiceHolder {
		private static DmpService dmpService = new DmpServiceImpl();
	}

	public static DmpService getInstance() {
		return DmpServiceHolder.dmpService;
	}

	@Override
	public void close() {
		rpcDao.closeConnection();
	}
}
