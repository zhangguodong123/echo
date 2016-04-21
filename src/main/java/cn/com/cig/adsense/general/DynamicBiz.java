package cn.com.cig.adsense.general;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.orbitz.consul.Consul;
import com.orbitz.consul.KeyValueClient;
import com.orbitz.consul.cache.ConsulCache;
import com.orbitz.consul.cache.KVCache;
import com.orbitz.consul.model.kv.Value;

import cn.com.cig.adsense.service.ModelMaterialService;
import cn.com.cig.adsense.service.impl.ModelMaterialServiceImpl;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.utils.consul.ConsulUtils;
import cn.com.cig.adsense.utils.date.DateUtil;
import cn.com.cig.adsense.vo.DateEnum;
import cn.com.cig.adsense.vo.Region;
import cn.com.cig.adsense.vo.fix.ModelMaterial;
import cn.com.cig.adsense.vo.fix.Size;
import cn.com.cig.adsense.vo.fix.UserStruct;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.undertow.server.HttpServerExchange;
/**
 * @File: CommonHander.java
 * @Package cn.com.cig.adsense.biz
 * @Description: TODO
 * @author zhangguodong
 * @date 2015年5月20日 上午11:04:44
 * @version V1.0
 */
public class DynamicBiz extends CommonBiz{
	private static Logger logger = LoggerFactory.getLogger(DynamicBiz.class);
	private static final ModelMaterialService mmService = ModelMaterialServiceImpl.getInstance();
	private static final Map<Integer, Integer> matchAdsRegions = Utils.matchAdsRegions();
	public static KeyValueClient kv = Consul.builder().withUrl(Constant.CONSUL_URL).build().keyValueClient();
	private static Configuration cfg;
	private static StringTemplateLoader tempLoader=new StringTemplateLoader();
	static {
		initTemplate();
		cfg = new Configuration(new Version(2, 3, 22));
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateLoader(tempLoader);
	}
	
	private static void initTemplate() {
		try {
			KVCache nc = KVCache.newCache(kv, Constant.TEMPLATE_CONFIG, 1);
			nc.addListener(new ConsulCache.Listener<String, Value>() {
				@Override
				public void notify(Map<String, Value> newValues) {
					Map<String, String> kvs = ConsulUtils.cacheStringKVMap(newValues);
					Set<String> ks = kvs.keySet();
					Iterator<String> it = ks.iterator();
					while(it.hasNext()){
						String next = it.next();
						String value = kvs.get(next);
						tempLoader.putTemplate(next, value);
					}
				}
				
			});	
			nc.start();
		} catch (Exception e) {
			logger.info("error:"+e.getMessage(),e);
		}
	}
	
	/**
	 * @description: 1)第一个请求有ddv并且>0，只输出尺寸 2)第二个请求没有ddv，输出html。
	 * @param exchange
	 * @return true(第一个请求)|false(第二个请求)
	 */
	public static boolean getFirstRequestMaterialOfDDV(HttpServerExchange exchange) {
		boolean inIframe = true;
		Deque<String> ddvDeque = exchange.getQueryParameters().get("ddv");
		if ((ddvDeque != null) && (ddvDeque.size() > 0)) {
			String ddvStr = ddvDeque.peek();
			try {
				int ddv = Integer.parseInt(ddvStr);
				if (ddv > 0) {
					inIframe = false;
				}
			} catch (NumberFormatException e) {
				logger.warn("Illegal ddv! ddv:" + ddvStr);
			}
		}
		return inIframe;
	}
	/**
	 * @description:
	 * @param exchange
	 * @param positionId
	 * @return
	 */
	public static int getPositionFlag(String positionId) {
		int positionIdTyp = 0;
		if (Constant.BITAUTO_LIST.contains(positionId)) {
			positionIdTyp = Constant.BITO;
		} else if (Constant.GROUPON_LIST.contains(positionId)) {
			positionIdTyp = Constant.GROUP_ON;
		} else if (Constant.ACTIVITY_LIST.contains(positionId)) {
			positionIdTyp = Constant.ACTIVITY;
		} else if (Constant.HUIMAICHE_DLIST.contains(positionId)) {
			positionIdTyp = Constant.HUIMAICHE_FIX;
		} else if (Constant.USEDCAR_LIST.contains(positionId)) {
			positionIdTyp = Constant.USED_CAR;
		} else if (Constant.YX_DLIST.contains(positionId)) {
			positionIdTyp = Constant.YIXIN_DYN;
		} else if (Constant.YCH_LIST.contains(positionId)) {
			positionIdTyp = Constant.YICHEHUI_DYN;
		} else if (Constant.HMC_ONE_LIST.contains(positionId)) {
			positionIdTyp = Constant.HUIMAICHE_DYN_ONE;
		} else if (Constant.HMC_TWO_LIST.contains(positionId)) {
			positionIdTyp = Constant.HUIMAICHE_DYN_TWO;
		} else if (Constant.HMC_FOUR_LIST.contains(positionId)) {
			positionIdTyp = Constant.HUIMAICHE_DYN_FOUR;
		}else {
			logger.error("Unvalidated positionId:" + positionId);
		}
		return positionIdTyp;
	}

	/**
	 * @description:获取用户物料
	 * @param matchAdsRegions
	 * @param mmService
	 * @param ip
	 * @param us
	 * @param locationArr locationArr都为0也是无法识别的地域。
	 * @param modelFrequency
	 * @param positionIdTyp
	 * @return
	 */
	public static Collection<ModelMaterial> getModelMaterialss(String ip, UserStruct us,Region region, Map<Integer, Integer> modelFrequency,int positionIdTyp) {
		//TODO 需要再次筛选物料问题
		Collection<ModelMaterial> modelMaterialss = null;// 投放素材+匹配上的车型ids
		if ((region == null) || (region.getProvince() == 0 && region.getCity() == 0)) {
			logger.warn("Unrecognized IP! ip:" + ip);
			if (us == null) {
				modelMaterialss = mmService.getTop9(positionIdTyp);
			} else {
				modelMaterialss = mmService.getTopNByAllModels(modelFrequency.keySet(), positionIdTyp);
			}
		} else {
			Region rt = regionTransform(region,positionIdTyp);
			if (us == null) {
				modelMaterialss = mmService.getTopNByProvinceAndcity(rt.getProvince(),rt.getCity(), positionIdTyp);// 3)
			} else {
				modelMaterialss = mmService.getTopNByModelsProvinceAndcity(modelFrequency.keySet(), rt.getProvince(), rt.getCity(), positionIdTyp);
			}
		}
		return modelMaterialss;
	}
	
	/**
	 * @description 生成最终输出HTML页面
	 * @param modelMaterialss
	 * @param size
	 * @param positionId
	 * @param positionIdTyp
	 * @return
	 * @throws TemplateException
	 * @throws IOException
	 */
	public static String getResponseString(Collection<ModelMaterial> modelMaterialss,Size size,String positionId,int positionIdTyp) throws TemplateException, IOException {
		//TODO 需要再次重构
		Map<String, Object> data = new HashMap<String, Object>();
		StringWriter writer = new StringWriter();
		
		switch(positionIdTyp){
			case Constant.GROUP_ON:{
				for (ModelMaterial m : modelMaterialss) {
					long group = DateUtil.getFormatDateMillis(DateEnum.FORMAT_DATE_0, m.getGroupEndTime());
					long now =  DateUtil.getFormatDateMillis(DateEnum.FORMAT_DATE_0,new Date());
					
					double res = (group - now)/ Constant.ONE_DAY;
					if (res <= 0) {
						m.setEndDay(0);
						logger.error(("物料" + m.getModelId() + "超过日期" + (group - now)));
					} else {
						m.setEndDay((int) res);
					}
				}

				if (modelMaterialss.size() < 9) {
					logger.info("Groupon_materials→modelMaterialss less than 9,size:"+ modelMaterialss.size());
				}

				if (size.equals(Constant.SIZE_720_80)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 2, true));
					Template template = cfg.getTemplate("groupon720x80","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				if (size.equals(Constant.SIZE_740_80)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 3, true));
					Template template = cfg.getTemplate("groupon740x80","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				break;
			}
			case Constant.ACTIVITY:{
				if (size.equals(Constant.SIZE_740_80)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 3, true));
					Template template = cfg.getTemplate("activity740x80","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				if (size.equals(Constant.SIZE_240_155)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 1, true));
					Template template = cfg.getTemplate("activity240x155","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				if (size.equals(Constant.SIZE_720_80)) {   
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 2, true));
					Template template = cfg.getTemplate("activity720x80","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				break;
			}
			case Constant.HUIMAICHE_FIX:{
				if (size.equals(Constant.SIZE_990_80)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 3, true));
					Template template = cfg.getTemplate("hmc990x80_0","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				break;
			}
			case Constant.USED_CAR:{
				if (size.equals(Constant.SIZE_240_200)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 1, true));
					Template template = cfg.getTemplate("usedCar240x200","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				if (size.equals(Constant.SIZE_990_80)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 3, true));
					Template template = cfg.getTemplate("usedCar990x80","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				break;
			}
			
			case Constant.YIXIN_DYN:{
				if (size.equals(Constant.SIZE_750_80)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 1, true));
					data.put("pid", positionId);
					data.put("sid", Constant.SERVER_ID);
					Template template = cfg.getTemplate("yx750x80","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				break;
			}
			case Constant.YICHEHUI_DYN:{
				if (size.equals(Constant.SIZE_740_80)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 2, true));
					Template template = cfg.getTemplate("yichehui740x80","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				break;
			}
			case Constant.HUIMAICHE_DYN_ONE:{
				if (size.equals(Constant.SIZE_990_80)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 3, true));
					Template template = cfg.getTemplate("hmc990x80_1","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				break;
			}
			case Constant.HUIMAICHE_DYN_TWO:{
				if (size.equals(Constant.SIZE_990_80)) {
					for (ModelMaterial m : modelMaterialss) {
						long group = DateUtil.getFormatDateMillis(DateEnum.FORMAT_DATE_0,m.getGroupEndTime());
						long now = DateUtil.getFormatDateMillis(DateEnum.FORMAT_DATE_0,new Date());
						double res = (group - now)/ Constant.ONE_DAY;
						if (res <= 0) {
							m.setEndDay(0);
							logger.error(("物料" + m.getModelId() + "超过日期" + (group - now)));
						} else {
							m.setEndDay((int) res);
						}
					}
					if (modelMaterialss.size() < 9) {
						logger.info("huimaiche_two_materials→modelMaterialss less than 9,size:"+ modelMaterialss.size());
					}
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 3, true));
					Template template = cfg.getTemplate("hmc990x80_2","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				break;
			}
			case Constant.HUIMAICHE_DYN_FOUR:{
				if (size.equals(Constant.SIZE_990_80)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 3, true));
					Template template = cfg.getTemplate("hmc990x80_4","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				if (size.equals(Constant.SIZE_240_310)) {
					data.put("modelMaterials", Utils.generateMonitorCode(Constant.BITAUTO, positionId, modelMaterialss, 1, true));
					Template template = cfg.getTemplate("hmc240x310_4","utf-8");
					if(template!=null){
						template.process(data, writer);
					}
				}
				break;
			}
			default:{
				logger.warn("Illegal positionIdType:" + positionIdTyp);
				return writer.toString();
			}
		}
		return writer.toString();
	}
	
	public static Region regionTransform(Region region,int positionIdTyp){
		int province = region.getProvince();
		int city = region.getCity();
		
		switch (positionIdTyp) {
			case Constant.HUIMAICHE_FIX:
			case Constant.YIXIN_DYN:
			case Constant.YICHEHUI_DYN:
			case Constant.HUIMAICHE_DYN_ONE:
			case Constant.HUIMAICHE_DYN_TWO:
			case Constant.HUIMAICHE_DYN_FOUR:
				if(province != 0){
					boolean isProvince = matchAdsRegions.containsKey(province);
					if (isProvince) {
						region.setProvince(matchAdsRegions.get(province));
					}else{
						logger.warn("The flag:"+positionIdTyp+" privice_id:"+province+" is not in matchAdsRegions #############");
					}
				}
				if(city != 0){
					boolean isCity = matchAdsRegions.containsKey(city);
					if (isCity) {
						region.setCity(matchAdsRegions.get(city));
					}else{
						logger.warn("The flag:"+positionIdTyp+" city_id:"+city+" is not in matchAdsRegions #############");
					}
				}
				logger.info("region_transform:province="+province+" city:"+city);
				break;
		default:
			break;
		}
		return region;
	}
}
