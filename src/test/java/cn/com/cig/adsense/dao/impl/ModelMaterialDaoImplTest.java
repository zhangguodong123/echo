package cn.com.cig.adsense.dao.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.dao.ModelMaterialDao;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.vo.fix.ModelMaterial;

/**
 * @File: ModelMaterialDaoImplTest.java
 * @Package cn.com.cig.adsense.dao.impl
 * @Description: TODO
 * @author zhangguodong
 * @date 2014年12月2日 下午2:18:34
 * @version V1.0
 */
public class ModelMaterialDaoImplTest {

	private static Logger logger = LoggerFactory
			.getLogger(ModelMaterialDaoImplTest.class);

	ModelMaterialDao dao = new ModelMaterialDaoImpl();

	// @Test
	public void getYiCheMallCarModelMaterialList() throws SQLException {
		ModelMaterialDaoImpl mDao = new ModelMaterialDaoImpl();

		File carsFile = new File("d://精准车型.csv");
		if (carsFile.exists()) {
			carsFile.deleteOnExit();
		}
		Map<Integer, Integer> matchYipaiRegions = Utils.matchYipaiRegions();
		Map<Integer, ModelMaterial> mysqlModels = dao.getAllModelMaterial();
		Map<Integer, ModelMaterial> allModelMaterialMap = dao
				.getAllModelMaterial();

		List<ModelMaterial> yipaiData = mDao.getYiCheMallCarModelMaterialList();
		StringBuilder sb = new StringBuilder();
		sb.append("CityId");
		sb.append(",");
		sb.append("RegionId");
		sb.append(",");
		sb.append("name");
		sb.append(",");
		sb.append("ModelId");
		sb.append(",");
		sb.append("MinPrice");
		sb.append(",");
		sb.append("MaxPrice");
		sb.append(",");
		sb.append("Slogan");
		sb.append(",");
		sb.append("LandingPage");
		sb.append(",");
		sb.append("MaxPromotionPrice");
		sb.append(",");
		sb.append("Gift");
		sb.append("\n");
		for (ModelMaterial ymm : yipaiData) {
			System.out.println(ymm.toString());
			ModelMaterial myModelMetal = mysqlModels.get(ymm.getModelId());

			ModelMaterial mm = allModelMaterialMap.get(ymm.getModelId());

			if (myModelMetal != null) {
				String name = myModelMetal.getName() == null ? ""
						: myModelMetal.getName();
				ymm.setName(name);

				Integer region = ymm.getRegion();
				Integer regionId = 0;
				if (region != null) {
					Integer in = matchYipaiRegions.get(region);
					if (in != null) {
						regionId = in;
					}
				}

				sb.append(regionId + "," + region + "," + ymm.getName() + ","
						+ ymm.getModelId() + "," + mm.getMinPrice() + ","
						+ mm.getMaxPrice() + "," + ymm.getSlogan() + ","
						+ ymm.getLandingPage() + ","
						+ ymm.getMaxPromotionPrice() + "," + ymm.getGift()
						+ "\n");
			}

		}
		try {
			Files.write(carsFile.toPath(), sb.toString().getBytes("gbk"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void getYiCheMallKpiModelMaterialListTest() throws SQLException {
		ModelMaterialDaoImpl mDao = new ModelMaterialDaoImpl();

		File carsFile = new File("d://KPI车型.csv");
		if (carsFile.exists()) {
			carsFile.deleteOnExit();
		}
		Map<Integer, Integer> matchYipaiRegions = Utils.matchYipaiRegions();
		Map<Integer, ModelMaterial> mysqlModels = dao.getAllModelMaterial();
		Map<Integer, ModelMaterial> allModelMaterialMap = dao
				.getAllModelMaterial();

		List<ModelMaterial> kpis = mDao.getYiCheMallKpiModelMaterialList();
		StringBuilder sb = new StringBuilder();
		sb.append("CityId");
		sb.append(",");
		sb.append("RegionId");
		sb.append(",");
		sb.append("name");
		sb.append(",");
		sb.append("ModelId");
		sb.append(",");
		sb.append("MinPrice");
		sb.append(",");
		sb.append("MaxPrice");
		sb.append(",");
		sb.append("Slogan");
		sb.append(",");
		sb.append("LandingPage");
		sb.append(",");
		sb.append("MaxPromotionPrice");
		sb.append(",");
		sb.append("Gift");
		sb.append("\n");
		for (ModelMaterial ymm : kpis) {
			System.out.println(ymm.toString());
			ModelMaterial myModelMetal = mysqlModels.get(ymm.getModelId());

			ModelMaterial mm = allModelMaterialMap.get(ymm.getModelId());

			if (myModelMetal != null) {
				String name = myModelMetal.getName() == null ? ""
						: myModelMetal.getName();
				ymm.setName(name);

				Integer region = ymm.getRegion();
				Integer regionId = 0;
				if (region != null) {
					Integer in = matchYipaiRegions.get(region);
					if (in != null) {
						regionId = in;
					}
				}

				sb.append(regionId + "," + region + "," + ymm.getName() + ","
						+ ymm.getModelId() + "," + mm.getMinPrice() + ","
						+ mm.getMaxPrice() + "," + ymm.getSlogan() + ","
						+ ymm.getLandingPage() + ","
						+ ymm.getMaxPromotionPrice() + "," + ymm.getGift()
						+ "\n");
			}

		}
		try {
			Files.write(carsFile.toPath(), sb.toString().getBytes("gbk"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void testJunitTime() {
		long nanos = TimeUnit.SECONDS.toNanos(60);
		long nhours = TimeUnit.HOURS.toNanos(1);
		long nanos2 = TimeUnit.MINUTES.toNanos(1);
		System.out.println(nanos);
		System.out.println(nhours);
		System.out.println(nanos2);
	}

	// @Test
	public void getCarMallMaterialListTest() {
		ModelMaterialDaoImpl mDao = new ModelMaterialDaoImpl();
		List<ModelMaterial> groupons = mDao.getYiCheMallCarModelMaterialList();
		List<ModelMaterial> year = new ArrayList<>();
		for (ModelMaterial mm : groupons) {
			if (mm.getShowyear() != null) {
				year.add(mm);
			}
		}
		System.out.println("Car长度:" + groupons.size());
		System.out.println("year_size:" + year.size());
	}

	// @Test
	public void getKpiMaterialListTest() {
		ModelMaterialDaoImpl mDao = new ModelMaterialDaoImpl();
		List<ModelMaterial> groupons = mDao.getYiCheMallKpiModelMaterialList();
		List<ModelMaterial> year = new ArrayList<>();
		for (ModelMaterial mm : groupons) {
			if (mm.getShowyear() != null) {
				year.add(mm);
			}
		}
		System.out.println("kpi长度:" + groupons.size());
		System.out.println("year_size:" + year.size() + year.get(0));
	}

	// @Test
	public void getGrouponModelMaterialListTestExcel() throws Exception {
		ModelMaterialDaoImpl mDao = new ModelMaterialDaoImpl();
		List<ModelMaterial> groupons = mDao.getGrouponModelMaterialList();
		File carsFile = new File("d://团购全部车型.csv");
		if (carsFile.exists()) {
			carsFile.deleteOnExit();
		}
		Map<Integer, Integer> matchYipaiRegions = Utils.matchYipaiRegions();
		Map<Integer, ModelMaterial> mysqlModels = dao.getAllModelMaterial();
		Map<Integer, ModelMaterial> allModelMaterialMap = dao
				.getAllModelMaterial();

		List<ModelMaterial> kpis = groupons;
		StringBuilder sb = new StringBuilder();
		sb.append("CityId");
		sb.append(",");
		sb.append("CityName");
		sb.append(",");
		sb.append("RegionId");
		sb.append(",");
		sb.append("name");
		sb.append(",");
		sb.append("ModelId");
		sb.append(",");
		sb.append("MinPrice");
		sb.append(",");
		sb.append("MaxPrice");
		sb.append(",");
		sb.append("Slogan");
		sb.append(",");
		sb.append("LandingPage");
		sb.append(",");
		sb.append("MaxPromotionPrice");
		sb.append(",");
		sb.append("Gift");
		sb.append("\n");
		for (ModelMaterial ymm : kpis) {
			System.out.println(ymm.toString());
			ModelMaterial myModelMetal = mysqlModels.get(ymm.getModelId());

			ModelMaterial mm = allModelMaterialMap.get(ymm.getModelId());

			if (myModelMetal != null) {
				String name = myModelMetal.getName() == null ? ""
						: myModelMetal.getName();
				ymm.setName(name);

				Integer region = ymm.getRegion();
				Integer regionId = 0;
				if (region != null) {
					Integer in = matchYipaiRegions.get(region);
					if (in != null) {
						regionId = in;
					}
				}

				String city = "";
				Map<Integer, String> cityNames = Utils.matchCityNameRegions();
				if (regionId != null) {
					city = cityNames.get(regionId);
				}

				sb.append(regionId + "," + city + "," + region + ","
						+ ymm.getName() + "," + ymm.getModelId() + ","
						+ mm.getMinPrice() + "," + mm.getMaxPrice() + ","
						+ ymm.getSlogan() + "," + ymm.getLandingPage() + ","
						+ ymm.getMaxPromotionPrice() + "," + ymm.getGift()
						+ "\n");
			}

		}
		try {
			Files.write(carsFile.toPath(), sb.toString().getBytes("gbk"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// @Test
	public void getGrouponHotModelMaterialListTestExcel() throws Exception {
		ModelMaterialDaoImpl mDao = new ModelMaterialDaoImpl();
		List<ModelMaterial> groupons = mDao.getGrouponHotModelMaterialList();
		File carsFile = new File("d://团购热门车型.csv");
		if (carsFile.exists()) {
			carsFile.deleteOnExit();
		}
		Map<Integer, Integer> matchYipaiRegions = Utils.matchYipaiRegions();
		Map<Integer, ModelMaterial> mysqlModels = dao.getAllModelMaterial();
		Map<Integer, ModelMaterial> allModelMaterialMap = dao
				.getAllModelMaterial();

		List<ModelMaterial> kpis = groupons;
		StringBuilder sb = new StringBuilder();
		sb.append("CityId");
		sb.append(",");
		sb.append("CityName");
		sb.append(",");
		sb.append("RegionId");
		sb.append(",");
		sb.append("name");
		sb.append(",");
		sb.append("ModelId");
		sb.append(",");
		sb.append("MinPrice");
		sb.append(",");
		sb.append("MaxPrice");
		sb.append(",");
		sb.append("Slogan");
		sb.append(",");
		sb.append("LandingPage");
		sb.append(",");
		sb.append("MaxPromotionPrice");
		sb.append(",");
		sb.append("Gift");
		sb.append("\n");
		for (ModelMaterial ymm : kpis) {
			System.out.println(ymm.toString());
			ModelMaterial myModelMetal = mysqlModels.get(ymm.getModelId());

			ModelMaterial mm = allModelMaterialMap.get(ymm.getModelId());

			if (myModelMetal != null) {
				String name = myModelMetal.getName() == null ? ""
						: myModelMetal.getName();
				ymm.setName(name);

				Integer region = ymm.getRegion();
				Integer regionId = 0;
				if (region != null) {
					Integer in = matchYipaiRegions.get(region);
					if (in != null) {
						regionId = in;
					}
				}
				String city = "";
				Map<Integer, String> cityNames = Utils.matchCityNameRegions();
				if (regionId != null) {
					city = cityNames.get(regionId);
				}

				sb.append(regionId + "," + city + "," + region + ","
						+ ymm.getName() + "," + ymm.getModelId() + ","
						+ mm.getMinPrice() + "," + mm.getMaxPrice() + ","
						+ ymm.getSlogan() + "," + ymm.getLandingPage() + ","
						+ ymm.getMaxPromotionPrice() + "," + ymm.getGift()
						+ "\n");
			}

		}
		try {
			Files.write(carsFile.toPath(), sb.toString().getBytes("gbk"));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// @Test
	public void testCityName() throws Exception {
		Map<Integer, String> cityNames = Utils.matchCityNameRegions();
		Set<Integer> keys = cityNames.keySet();
		Iterator<Integer> it = keys.iterator();
		while (it.hasNext()) {
			Integer nextKey = it.next();
			String cityName = cityNames.get(nextKey);
			System.out.println(nextKey + ":" + cityName);
		}
	}

	// @Test
	public void getGrouponModelMaterialListTest() {
		ModelMaterialDaoImpl mDao = new ModelMaterialDaoImpl();
		List<ModelMaterial> groupons = mDao.getGrouponModelMaterialList();
		System.out.println(groupons.size());
	}

	// @Test
	public void getGrouponHotModelMaterialListTest() {
		ModelMaterialDaoImpl mDao = new ModelMaterialDaoImpl();
		List<ModelMaterial> groupons = mDao.getGrouponHotModelMaterialList();
		System.out.println(groupons.size());
	}

	// @Test
	public void getActivtyMapTest() {
		Map<Integer, List<ModelMaterial>> map = dao
				.getActivityModelMaterialMap();
		System.out.println(map.get(1).size());
		System.out.println(map.get(2).size());
	}

	// @Test
	public void getMallKpiTest() {
		List<ModelMaterial> yipaiData = dao.getYiCheMallKpiModelMaterialList();
		System.out.println(yipaiData.size());
	}

	@Test
	public void getHuimaicheMaterials() {
		List<ModelMaterial> huimaicheModelMaterialMap = getHuimaicheModelMaterialMap();
		for (int i = 0; i < huimaicheModelMaterialMap.size(); i++) {
			ModelMaterial modelMaterial = huimaicheModelMaterialMap.get(i);
			//boolean promotion = modelMaterial.isPromotion();
			/*if(modelMaterial.getModelId() == 106307){
				System.out.println(modelMaterial);
			}*/
			//if (!promotion) {
				Integer region = modelMaterial.getRegion();
				if(region == 1001){
					System.out.println(modelMaterial.getRegion()+"  "+modelMaterial.getName()+"   "+modelMaterial.getModelId()+ " "+modelMaterial.isPromotion()+" "+modelMaterial.getLandingPage());
				}
			//}
		}
		
		/*File carsFile = new File("d://惠买车红包标识车型集.csv");
		if (carsFile.exists()) {
			carsFile.deleteOnExit();
		}
		StringBuilder sb = new StringBuilder();
		sb.append("modelName");
		sb.append(",");
		sb.append("modelId");
		sb.append(",");
		sb.append("promotion");
		sb.append(",");
		sb.append("cityId");
		sb.append("\n");
		for (int i = 0; i < huimaicheModelMaterialMap.size(); i++) {
			ModelMaterial modelMaterial = huimaicheModelMaterialMap.get(i);
			boolean promotion = modelMaterial.isPromotion();
			if (promotion) {
				String name = modelMaterial.getName();
				Integer modelId = modelMaterial.getModelId();
				Integer region = modelMaterial.getRegion();
				sb.append(name + "," + modelId + "," + region+","+ "\n");
			}
		}
		try {
			Files.write(carsFile.toPath(), sb.toString().getBytes("gbk"));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	public List<ModelMaterial> getHuimaicheModelMaterialMap() {
		// TODO 惠买车接口
		List<ModelMaterial> promotion = new ArrayList<ModelMaterial>();
		List<ModelMaterial> unPromotion = new ArrayList<ModelMaterial>();
		List<ModelMaterial> result = new ArrayList<ModelMaterial>();
		String data = httpURLConnRequest(Constant.HUIMAICHE_INTERFACE_URL,"GET");
		JSONArray jarray = null;
		try {
			jarray = new JSONArray(data);
		} catch (JSONException e) {
			logger.error("error load data:", e);
			return result;
		}
		if (jarray.length() > 200000)
			logger.error("data is too big****************:/%", jarray.length());
		for (int i = 0; i < jarray.length(); i++) {
			ModelMaterial mo = new ModelMaterial();
			try {
				JSONObject object = jarray.getJSONObject(i);
				int moid = object.getInt("SerialID");
				if (moid == 0 || moid < 0)
					continue;
				String url = object.getString("Url") == null ? "" : object
						.getString("Url");
				String mUrl = object.getString("MUrl") == null ? "" : object
						.getString("MUrl");
				String img = object.getString("Img") == null ? "" : object
						.getString("Img");
				String serialName = object.getString("SerialName") == null ? ""
						: object.getString("SerialName");
				String desc = object.getString("Desc") == null ? "" : object
						.getString("Desc");
				double referPrice = object.getDouble("ReferPrice");
				double minPrice = object.getDouble("MinPrice");
				int cityId = object.getInt("City");
				boolean isPromotion = object.getBoolean("IsPromotion");

				mo.setModelId(moid);
				mo.setLandingPage(url);
				mo.setName(serialName);
				mo.setMinPrice(String.valueOf(minPrice));
				mo.setRegion(cityId);
				mo.setPrefer_info(desc);
				mo.setPic210X140(img);
				mo.setPrice(String.valueOf(referPrice));
				mo.setmUrl(mUrl);
				mo.setPromotion(isPromotion);// 是否是双11投放的车型

				if (isPromotion) {
					promotion.add(mo);
				} else {
					unPromotion.add(mo);
				}
			} catch (JSONException e) {
				logger.equals("parse huimaiche_interface is error");
				continue;
			}
		}
		if (promotion != null && promotion.size() > 0) {
			result.addAll(promotion);
		}
		if (unPromotion != null && unPromotion.size() > 0) {
			result.addAll(unPromotion);
		}
		promotion = null;
		unPromotion = null;
		return result;
	}

	public static String httpURLConnRequest(String requestUrl, String method) {
		StringBuffer buffer = new StringBuffer();
		HttpURLConnection conn = null;
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try {
			URL url = new URL(requestUrl);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(120000);// 2分钟超时时间
			conn.setRequestMethod(method);
			conn.setUseCaches(false);
			conn.connect();
			// 将返回的输入流转换成字符串
			int response_code = conn.getResponseCode();
			if (response_code == HttpURLConnection.HTTP_OK) {
				inputStream = conn.getInputStream();
				inputStreamReader = new InputStreamReader(inputStream, "utf-8");
				bufferedReader = new BufferedReader(inputStreamReader);
				while (true) {
					final String line = bufferedReader.readLine();
					if (line == null)
						break;
					buffer.append(line);
				}
			} else {
				logger.warn("request:" + requestUrl + " is fault:"
						+ response_code);
			}
		} catch (Exception e) {
			logger.error("error:%s /n", requestUrl, e);
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					bufferedReader = null;
				}
			}

			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					inputStreamReader = null;
				}
			}

			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					inputStream = null;
				}
			}

			if (conn != null) {
				conn.disconnect();
			}
		}
		return buffer.toString();
	}
}
