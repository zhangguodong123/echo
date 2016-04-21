package cn.com.cig.adsense.handler;

import static cn.com.cig.adsense.general.CommonBiz.limitedAdvisterFrequency;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.dao.redis.RedisMasterSlaveManager;
import cn.com.cig.adsense.general.FixtureBiz;
import cn.com.cig.adsense.service.BitautoMaterialLibraryService;
import cn.com.cig.adsense.service.impl.BitautoMaterialLibraryServiceImpl;
import cn.com.cig.adsense.vo.Region;
import cn.com.cig.adsense.vo.dyn.AdvisterFrequency;
import cn.com.cig.adsense.vo.fix.Advertiser;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.Campaign;
import cn.com.cig.adsense.vo.fix.Query;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**   
 * @File: AppPMPDeliveryHandler.java 
 * @Package cn.com.cig.adsense.handler 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月7日 上午11:46:20 
 * @version V1.0   
 */
public class AppPMPDeliveryHandler implements HttpHandler {
	private static Logger logger = LoggerFactory.getLogger(AppPMPDeliveryHandler.class);
	private RedisMasterSlaveManager instance = RedisMasterSlaveManager.getInstance();//频次redis
	private static final BitautoMaterialLibraryService mls = BitautoMaterialLibraryServiceImpl.getInstance();
	private AppDeliveryHandler appFixture=new AppDeliveryHandler();
	private Query query;
	
	@Override
	public void handleRequest(HttpServerExchange exchange) throws Exception {
		if(query == null){
			exchange.setResponseCode(HttpServletResponse.SC_NOT_FOUND);// 404
			return;
		}
		if (exchange.isInIoThread()) {
			exchange.dispatch(this);
			return;
		}
		appFixture.setQuery(query);
		Integer advisterId = mls.getPMPAdvisterID(query.getPid());         //查找广告位关联pmp广告主ID;
		if(advisterId== null || advisterId ==0){
			logger.warn("The pid:"+query.getPid()+" relate advisterID is null");
			appFixture.handleRequest(exchange);
			return;
		}
		Advertiser advertiser = mls.getPMPAdvister(advisterId); //查找pmp广告主Bean;
		if(advertiser == null || advertiser.getId() == null){
			logger.warn("The PID:"+query.getPid()+" 's of advertiser is null");
			appFixture.handleRequest(exchange);
			return;
		}
		List<AdvisterFrequency> userPVFrequency = instance.getUserPVFrequency(query.getDvid());
		//判断访客在该广告主是否已经达到→每天每访客限定曝光阀值，如果达到去投公投;
		if(limitedAdvisterFrequency(userPVFrequency,advertiser)){
			logger.warn("The dvID: "+query.getDvid()+" have  over every visitor limited exposure advisterID:"+advisterId);
			appFixture.handleRequest(exchange);
			return;
		}
		
		Region region = FixtureBiz.getRegionId(query.getIp(),query.getCityid());
		Campaign fixedPmp = mls.getDirectPMPCampain(advisterId,query.getPid());// 定投PMP开启的广告计划;
		List<Campaign> unFixedPmp = advertiser.getUnDirectional();  		   //非定投PMP开启的广告计划;
		List<Campaign> unFixedUnPmp = advertiser.getGeneral();				   //非定投PMP关闭的广告计划;
		
		Map<Integer, Integer> tags = query.getTags();
		Collection<BitautoMaterial> materials = null;
		
		//1) 定投pmp
		if(fixedPmp != null){
			if(region != null){
				materials=mls.getDirectPMPEletextMaterialsBySizeAndRegion(fixedPmp,region.getProvince(), region.getCity());//投定投符合尺寸的地域素材
				materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);
			}
			if(materials!=null && materials.size()>0){
				materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);
				materials = FixtureBiz.cookieFilter(exchange,materials,tags);
			}
			if(materials == null || materials.size() ==0){
				materials=mls.getDirectPMPEletextDefaultMaterialsBySizeAndRegion(fixedPmp);     //投放默认素材
				materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);
			}
		}
		
		//2) 非定投pmp
		if(materials == null || materials.size() == 0){
			if(unFixedPmp != null && unFixedPmp.size() >0){
				if(region !=null){
					materials=mls.getUnDirectPMPEletextMaterialsBySizeAndRegion(unFixedPmp,region.getProvince(), region.getCity());
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);
				}
				if(materials!=null && materials.size()>0){
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);
					materials = FixtureBiz.cookieFilter(exchange,materials,tags);
				}
				if(materials == null || materials.size() ==0){
					materials=mls.getUnDirectPMPEletextDefaultMaterialsBySize(unFixedPmp);
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);
				}
			}
		}
		
		//3) 非定投非pmp
		if(materials == null || materials.size()== 0){
			if(unFixedUnPmp != null && unFixedUnPmp.size() >0){
				if(region !=null){
					materials=mls.getGeneralPMPEletextMaterialsBySizeAndRegion(unFixedUnPmp,region.getProvince(), region.getCity());
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);
				}
				if(materials!=null && materials.size()>0){	
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);
					materials = FixtureBiz.cookieFilter(exchange,materials,tags);
				}
				if(materials == null || materials.size() ==0){
					materials=mls.getGeneralPMPEletextDefaultMaterialsBySize(unFixedUnPmp);
					materials=FixtureBiz.selectedUserPVFrequency(userPVFrequency,materials);
				}
			}
		}
		//4) finally
		FixtureBiz.returnMaterial(appFixture,exchange, materials,query.getPid(),query.getDvid());
	}
	
	public void setQuery(Query query) {
		this.query = query;
	}
}
