package cn.com.cig.adsense.dao;

import java.util.List;
import java.util.Map;

import cn.com.cig.adsense.vo.fix.ModelMaterial;
import cn.com.cig.adsense.vo.fix.YipaiModelMaterial;

public interface ModelMaterialDao {
	public Map<Integer, ModelMaterial> getAllModelMaterial();
	
	/**author:zhanggd since:2015-03-16 11:34 描述：取不同年份的车*/
	public Map<String, ModelMaterial> getAllMallModelMaterial();
	
	public List<Integer> getTopN();
	
	public Map<Integer, List<Integer>> getCompetitiveModels();
	
	public List<YipaiModelMaterial> getYipaiModelMaterial();
	
	/**author:zhanggd since:2014-12-02 15:10 描述：获取所有上架状态的商品*/
	public List<ModelMaterial> getYiCheMallKpiModelMaterialList();
	/**author:zhanggd since:2014-12-02 15:10 描述：获取所有KPI商品*/
	public List<ModelMaterial> getYiCheMallCarModelMaterialList();
	/**author:zhanggd since:2014-12-16 9:51 描述：获取所有团购车型*/
	public List<ModelMaterial> getGrouponModelMaterialList();
	/**author:zhanggd since:2014-12-16 11:23 描述：获取所有团购热门车型*/
	public List<ModelMaterial> getGrouponHotModelMaterialList();
	/**author:zhanggd since:2015-03-11 10:04 描述：获取活动车型*/
	public Map<Integer,List<ModelMaterial>> getActivityModelMaterialMap();
	/**author:zhanggd since:2015-05-07 10:04 描述：获取惠买车接口车型*/
	public List<ModelMaterial> getHuimaicheModelMaterialMap();
	/**author:zhanggd since:2015-12-24 14:36描述：获取易车惠接口车型*/
	public List<ModelMaterial> getYiCheHuiModelMaterialList();
	/**
	 * @author zhanggd
	 * @deprecated: 添加用户统计接口访问
	 * @since 2015-05-28
	 * @param positionId
	 * @param t
	 * @param u
	 * @param deviceId 
	 * @param pid 
	 * @return 
	 */
	public boolean insertUserViewCountOfMinute(Integer positionId, String t,Integer u,Integer deviceId);
	/**author:zhangguodong since:2015-06-29 16:28 描述：获取易鑫即溶接口车型**/
	public Map<String, List<ModelMaterial>> getPrecisionAdvertisingList();
	/**author:zhanggd since:2016-01-04 14:28描述：获取易车惠接口车型*/
	public List<ModelMaterial> getUsedCarList();
	/**author:zhanggd since:2016-01-28 14:28描述：获取易车惠接口车型*/
	public Map<Integer,List<ModelMaterial>> getHuiMaiCheProduct();
	/**author:zhanggd since:2016-01-28 14:28描述：获取易车惠直销接口车型*/
	public Map<Integer,List<ModelMaterial>> getHuiMaiCheZXProduct();
}
