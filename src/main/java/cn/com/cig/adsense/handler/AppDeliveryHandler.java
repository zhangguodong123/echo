package cn.com.cig.adsense.handler;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.dao.redis.RedisMasterSlaveManager;
import cn.com.cig.adsense.general.FixtureBiz;
import cn.com.cig.adsense.service.BitautoMaterialLibraryService;
import cn.com.cig.adsense.service.impl.BitautoMaterialLibraryServiceImpl;
import cn.com.cig.adsense.utils.date.DateUtil;
import cn.com.cig.adsense.vo.DateEnum;
import cn.com.cig.adsense.vo.Region;
import cn.com.cig.adsense.vo.dyn.AdvisterFrequency;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.Query;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
/**   
 * @File: AppDeliveryHandler.java 
 * @Package cn.com.cig.adsense.handler 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月7日 上午11:46:11 
 * @version V1.0   
 */
public class AppDeliveryHandler implements HttpHandler {
	private static Logger logger = LoggerFactory.getLogger(AppDeliveryHandler.class);
	private RedisMasterSlaveManager instance = RedisMasterSlaveManager.getInstance();//频次redis
	private static final BitautoMaterialLibraryService mls = BitautoMaterialLibraryServiceImpl.getInstance();
	private Query query;
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if(query == null){
			logger.error("The query beaan is null......"+DateUtil.getFormatDateMillis(DateEnum.FORMAT_DATE_2, new Date()));
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
			return;
		}
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		Region region = FixtureBiz.getRegionId(query.getIp(),query.getCityid());
		List<AdvisterFrequency> userPVFrequency = instance.getUserPVFrequency(query.getDvid());
		Map<Integer, Integer> tags = query.getTags();
		//1) 取[地域]符合
		Collection<BitautoMaterial> materials = null;
		if(region != null){//关联过地域:根据地域未找到素材时，投默认素材。
			materials = mls.getMaterialByRegion(mls.getAllEletextMaterial(), region.getProvince(), region.getCity());
			if(materials != null && materials.size() >0){
				materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);
				materials = FixtureBiz.cookieFilter(exchange, materials, tags);
			}
		}
		//2) 默认素材
		if(materials ==null || materials.size() ==0){
			materials = mls.getAllDefaultEletextMaterial();
			if(materials ==null || materials.size() ==0){
				logger.warn("The AllDefaultEletextMaterial is empty | null >>>>>>>>>>>>>【warn】>【warn】【warn】>【warn】>>>>>>>>>>>>");
			}
			materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);
		}
		//3) 频次限定
		FixtureBiz.returnMaterial(null,exchange, materials,query.getPid(),query.getDvid());
	}
	
	public void setQuery(Query query) {
		this.query = query;
	}
}
