package cn.com.cig.adsense.dao.impl;
import static cn.com.cig.adsense.utils.http.HttpUtils.httpClient;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import cn.com.cig.adsense.dao.ModelMaterialDao;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.vo.HttpMethodEnum;
import cn.com.cig.adsense.vo.fix.ModelMaterial;

/**
 * @File: ModelMaterialDaoImplTest_Interface.java
 * @Package cn.com.cig.adsense.dao.impl
 * @Description: TODO
 * @author zhangguodong
 * @date 2015年4月29日 上午11:47:07
 * @version V1.0
 */
public class ModelMaterialDaoImplTest_Interface {
	ModelMaterialDao dao = new ModelMaterialDaoImpl();

	// @Test
	public void getGrouponModelMaterialListTest() {
		ModelMaterialDaoImpl mDao = new ModelMaterialDaoImpl();
		List<ModelMaterial> groupons = mDao.getGrouponModelMaterialList();
		System.out.println(groupons.size());
	}

//	 @Test
	public void getGrouponHotModelMaterialListTest() {
		ModelMaterialDaoImpl mDao = new ModelMaterialDaoImpl();
		List<ModelMaterial> groupons = mDao.getGrouponHotModelMaterialList();
		List<ModelMaterial> grouponModelMaterialList = mDao.getGrouponModelMaterialList();
		System.out.println(groupons.size());
		System.out.println(grouponModelMaterialList.size());
	}

//	@Test
	public void getActivtyMapTest() { 
		/*File carsFile = new File("d://活动全部车型.csv");
		if(carsFile.exists()){
			 carsFile.deleteOnExit();
		 }
		StringBuilder sb = new StringBuilder();
		sb.append("cid");
		sb.append(",");
		sb.append("name");
		sb.append(",");
		sb.append("cityId");
		sb.append(",\n");*/
		Map<Integer, List<ModelMaterial>> map = dao.getActivityModelMaterialMap();
		List<ModelMaterial> all = map.get(1);
		List<ModelMaterial> all1 = map.get(2);
		System.out.println(all.size());
		System.out.println(all1.size());
		/*all.forEach(new Consumer<ModelMaterial>() {
			@Override
			public void accept(ModelMaterial t) {
				if(t.getRegion() == 201){
					Integer modelId = t.getModelId();
					Integer region = t.getRegion();
					String name = t.getName();
					
					sb.append(modelId+ ","+name+ ","
							+ region+ ",\n");
				}
			}
		});
		
		try {
			Files.write(carsFile.toPath(), sb.toString().getBytes("GBK"));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
		
		/*System.out.println(map.get(1).size());
		System.out.println(map.get(2).size());*/
	}

	//@Test
	public void getMallListTest() {
		List<ModelMaterial> map = dao.getYiCheMallKpiModelMaterialList();
		int size = map.size();
		System.out.println(size);
		
		List<ModelMaterial> all = dao.getYiCheMallCarModelMaterialList();
		int allsize = all.size();
		System.out.println(allsize);
	}
	//@Test
	public void getYxTest(){
		Map<String, List<ModelMaterial>> c = dao.getPrecisionAdvertisingList();
		List<ModelMaterial> kpi = c.get("kpi");
		List<ModelMaterial> list = c.get("all");
		System.out.println(kpi.size());
		System.out.println(list.size());
	}
	@Test
	public void getYchTest(){
		StringBuilder builder=new StringBuilder();
		String content = httpClient(Constant.YICHEHUI_INTERFACE_URL,HttpMethodEnum.POST,builder.append(Constant.YICHEHUI_INTERFACE_KEY));
		System.out.println(content);
	}
}
