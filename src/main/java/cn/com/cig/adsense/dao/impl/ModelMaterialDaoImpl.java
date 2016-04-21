package cn.com.cig.adsense.dao.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cn.com.cig.adsense.dao.ModelMaterialDao;
import cn.com.cig.adsense.dao.wsdl.mall.KpiEntity;
import cn.com.cig.adsense.dao.wsdl.mall.YiCheMallForAdvertizement;
import cn.com.cig.adsense.dao.wsdl.mall.YiCheMallForAdvertizementSoap;
import cn.com.cig.adsense.dao.wsdl.yipai.YjkForAdvertizement;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.utils.date.DateUtil;
import cn.com.cig.adsense.vo.DateEnum;
import cn.com.cig.adsense.vo.HttpMethodEnum;
import cn.com.cig.adsense.vo.dyn.Block;
import cn.com.cig.adsense.vo.dyn.Feed;
import cn.com.cig.adsense.vo.dyn.Product;
import cn.com.cig.adsense.vo.fix.Data;
import cn.com.cig.adsense.vo.fix.ModelMaterial;
import cn.com.cig.adsense.vo.fix.Structer;
import cn.com.cig.adsense.vo.fix.YipaiModelMaterial;
import static cn.com.cig.adsense.utils.Utils.httpURLConnRequest;
import static cn.com.cig.adsense.utils.Utils.readJsonFile;
import static cn.com.cig.adsense.general.CommonBiz.sortModelsByWeigth;
import static cn.com.cig.adsense.utils.http.HttpUtils.httpClient;
import static cn.com.cig.adsense.utils.secret.EDncryUtil.MD5;
import static cn.com.cig.adsense.utils.secret.EDncryUtil.decryptMode;

public class ModelMaterialDaoImpl implements ModelMaterialDao {

	private static Logger logger = LoggerFactory.getLogger(ModelMaterialDaoImpl.class);
	
	private static final String TOPN_SEPARATOR = ",";
	// 只取90x60的图,其他图暂时用不到。
	private static final String GET_MODEL_PIC_URL = "SELECT images.*, m.DisplayName AS name FROM WhiteCoverImages AS images, Model AS m "
			+ "WHERE images.DefaultCarYear =1 AND m.id = images.Model AND images.specification = 3";
	//private static final String GET_MODEL_PIC_URL = "SELECT images.*, m.DisplayName AS name FROM WhiteCoverImages AS images, Model AS m "
	//		+ "WHERE images.DefaultCarYear =1 AND m.id = images.Model AND images.specification = 5";
	
//	private static final String GET_MODEL_PIC_URL_YEAR = "SELECT images.*, m.DisplayName AS name FROM WhiteCoverImages AS images, Model AS m "
//			+ "WHERE m.id = images.Model AND images.specification = 3 AND images.Model=? AND images.CarYear=? ";
	private static final String GET_MODEL_PIC_URL_YEAR = "SELECT images.*, m.DisplayName AS name FROM WhiteCoverImages AS images, Model AS m "
			+ "WHERE m.id = images.Model AND images.specification = 3 ";
	
	private static final String INSERT_BITAI_BY_PID_DATE="INSERT into BitImpressions(date,hour,device,pid,impression) values(?,?,?,?,?)";//TODO 新增曝光量记录SQL
	private static final String UPDATE_BITAI_BY_PID_DATE="UPDATE BitImpressions set impression=? where id=?";//TODO 更新曝光量数据SQL
	private static final String QUERY_BITAI_BY_PID_DATE="SELECT id,date,hour,device,pid,impression from BitImpressions WHERE pid=? AND date=? and hour=? and device=?";//查曝光量SQL					   //TODO 新增统计查询SQL修改
	private static Gson gson = new Gson();
	@Override
	public List<Integer> getTopN() {
		List<Integer> topN = new ArrayList<>(4000);
		try {
			// 使用外部文件夹，方便更新。以后可以考虑把这些文件从hdfs上拿。
			File topnFolder = new File(Constant.TOPN_MODELS_FOLDER);
			if (!topnFolder.exists()) {
				logger.error("Topn folder not exist! topnFolder:"
						+ topnFolder.getAbsolutePath());
			}
			Path lastFile = getNewlyCreatedFile(topnFolder.toURI(),
					"topk_model");
			if (lastFile == null) {
				logger.error("lastFile is null! topnFolder:"
						+ topnFolder.getAbsolutePath());
				return topN;
			}
			logger.info("getTopN lastFile:" + lastFile.toString());
			try (Stream<String> lines = Files.lines(lastFile, Charset.forName("utf-8"))) {
				lines.filter(line -> {
					String modelIdStr = line.split(TOPN_SEPARATOR)[0];
					try {
						int modelId = Integer.parseInt(modelIdStr);
						// 吴老师遇到无法识别车型的页面会把车型id标注为5位，这些没法用，需要过滤掉。
						if (modelId > 9999) {
							return false;
						}
						return true;
					} catch (NumberFormatException e) {
						logger.warn("Model id is not number! line:" + line);
						return false;
					}
				}).forEach(line -> {
					String modelIdStr = line.split(TOPN_SEPARATOR)[0];
					topN.add(Integer.parseInt(modelIdStr));
				});
			} catch (Exception e) {
				logger.error("Processing topN failed!", e);
			}
		} catch (Exception e) {
			logger.error("Get topN failed!", e);
		}
		logger.info("Get topN end.");
		return topN;
	}

	@Override
	public Map<Integer, List<Integer>> getCompetitiveModels() {
		Map<Integer, List<Integer>> cm = new HashMap<>(2000);
		try {
			// 使用外部文件夹，方便更新。以后可以考虑把这些文件从hdfs上拿。
			File competitiveModelsFolder = new File(
					Constant.COMPETITIVE_MODELS_FOLDER);
			if (!competitiveModelsFolder.exists()) {
				logger.error("Competitive models folder not exist! topnFolder:"
						+ competitiveModelsFolder.getAbsolutePath());
			}
			Path lastFile = getNewlyCreatedFile(
					competitiveModelsFolder.toURI(), "competitive_model");
			if (lastFile == null) {
				logger.error("lastFile is null! Competitive models folder:"
						+ competitiveModelsFolder.getAbsolutePath());
				return cm;
			}
			logger.info("Competitive models lastFile:" + lastFile.toString());
			try (Stream<String> lines = Files.lines(lastFile,
					Charset.forName("utf-8"))) {
				lines.forEach(line -> {
					String[] modelArr = line.split(",");
					if (modelArr.length >= 2) {
						int modelId = 0;
						List<Integer> competitiveModels = new ArrayList<>(
								modelArr.length - 1);
						for (int i = 0; i < modelArr.length; i++) {
							if (i == 0) {
								// 车型的id格式不对，不需要处理后边的竞品了。
								try {
									modelId = Integer.parseInt(modelArr[0]
											.trim());
								} catch (NumberFormatException e) {
									logger.warn("Illegal model id. modelId:"
											+ modelArr[0]);
									break;
								}
							} else {
								// 当前竞品车型id格式不对，忽略，继续处理下一个。
								try {
									competitiveModels.add(Integer
											.parseInt(modelArr[i].trim()));
								} catch (NumberFormatException e) {
									logger.warn("Illegal competitive model id. modelId:"
											+ modelArr[i]);
									continue;
								}
							}
						}
						cm.put(modelId, competitiveModels);
					} else {
						logger.warn("Illegal competitive models line. line:"
								+ line);
					}
				});
			} catch (Exception e) {
				logger.error("Processing competitive models failed!", e);
			}
		} catch (Exception e) {
			logger.error("Get competitive models failed!", e);
		}
		logger.info("Get competitive models end.");
		return cm;
	}

	/*
	 * 获取获取文件夹下最新创建的文件 The newly created file
	 */
	private static Path getNewlyCreatedFile(URI directoryURI,
			String fileNameStartsWith) throws IOException {
		if ((fileNameStartsWith == null) || ("".equals(fileNameStartsWith))) {
			logger.error("fileNameStartsWith is empty! fileNameStartsWith:"
					+ fileNameStartsWith);
			return null;
		}
		File directory = new File(directoryURI);
		if (!directory.exists()) {
			logger.error("Folder not exist! directory:"
					+ directory.getAbsolutePath());
			return null;
		}
		
		Path lastFile = null;
		try (Stream<Path> paths = Files.list(directory.toPath())) {
			lastFile = paths.filter(file -> {
				return file.getFileName().toString()
						.startsWith(fileNameStartsWith);
				// .startsWith("topk_model");
			})
			.max((file1, file2) -> {
				BasicFileAttributes attr1 = null;
				try {
					attr1 = Files.readAttributes(file1,
							BasicFileAttributes.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
				FileTime file1CreateTime = attr1.creationTime();
				BasicFileAttributes attr2 = null;
				try {
					attr2 = Files.readAttributes(file2,
							BasicFileAttributes.class);
				} catch (Exception e) {
					e.printStackTrace();
				}
				FileTime file2CreateTime = attr2.creationTime();
				return file1CreateTime.compareTo(file2CreateTime);
			}).orElse(null);
		} catch (Exception e) {
			logger.error("Get lastFile failed!", e);
		}
		if (lastFile == null) {
			logger.error("lastFile is null! directory:"
					+ directory.getAbsolutePath());
			return null;
		}
		return lastFile;
	}

	@Override
	public Map<Integer, ModelMaterial> getAllModelMaterial() {
		// 从mysql获取所有ModelMaterial
		Map<Integer, ModelMaterial> result = new HashMap<>();
		Connection dbConnection = getDBConnection();
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = dbConnection
					.prepareStatement(GET_MODEL_PIC_URL);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				ModelMaterial mm = null;
				int modelId = rs.getInt("Model");
				if (result.containsKey(modelId)) {
					mm = result.get(modelId);
				} else {
					mm = new ModelMaterial();
					mm.setModelId(modelId);
					result.put(modelId, mm);
				}
				if ((mm.getName() == null) || ("".equals(mm.getName()))) {
					mm.setName(rs.getString("name"));
				}
				String modelPicUrl = rs.getString("ImageUrl");
				int spec = rs.getInt("specification");
				switch (spec) {
//				case 1:
//					mm.setPic150X100(modelPicUrl);
//					break;
//				case 2:
//					mm.setPic120X80(modelPicUrl);
//					break;
				case 3:
					mm.setPic210X140(modelPicUrl);
					break;
				case 5:
					mm.setPic90x60(modelPicUrl);
					break;
//				case 6:
//					mm.setPic300x200(modelPicUrl);
//					break;
//				case 7:
//					mm.setPic150x100(modelPicUrl);
//					break;
//				case 21:
//					mm.setPic120x80(modelPicUrl);
//					break;
				default:
					logger.warn("Illegal model pic specification! spec:" + spec);
					break;
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return result;
	}

	private static Connection getDBConnection() {

		Connection dbConnection = null;
		try {
			Class.forName(Constant.DB_DRIVER);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

		try {
			dbConnection = DriverManager.getConnection(Constant.MODEL_PIC_DB_CONNECTION,
					Constant.MODEL_PIC_DB_USER, Constant.MODEL_PIC_DB_PASSWORD);
			return dbConnection;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

		return dbConnection;
	}
	
	/**
	 * @description:新增统计按时间划分
	 * @return
	 */
	private static Connection getCountDBConnection() {
		//TODO 需要这里加数据库连接地址和表名字
		Connection dbConnection = null;
		try {
			Class.forName(Constant.DB_DRIVER);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

		try {
			dbConnection = DriverManager.getConnection(Constant.BITA_DB_STATISTICS_CONNECTION,
					Constant.BITA_DB_STATISTICS_USER, Constant.BITA_DB_STATISTICS_PASSWORD);
			return dbConnection;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

		return dbConnection;
	}
	
	
	public List<YipaiModelMaterial> getYipaiModelMaterial() {
		List<YipaiModelMaterial> allYipaiModelMaterial = new ArrayList<>(1500);
		//TODO 使用易集客的接口
//		http://app.easypass.cn/yijike/api/YjkForAdvertizement.asmx
//		host：192.168.200.51
		
		YjkForAdvertizement yipaiService = new YjkForAdvertizement();
		String yipaiData = yipaiService.getYjkForAdvertizementSoap().getYjkBasicForAdvertizement();
		//logger.info("yipaiInterfaceData:" + yipaiData);
		Document doc = Jsoup.parse(yipaiData);
		Elements elements = doc.select("Items");
		Iterator<Element> iter = elements.select("item").iterator();
		while (iter.hasNext()) {
			Element item = iter.next();
			YipaiModelMaterial ymm = new YipaiModelMaterial();
			try {
				String cityId = item.select("cityid").first().text();
				ymm.setCityId(Integer.parseInt(cityId));
				String modelId = item.select("csid").first().text();
				ymm.setModelId(Integer.parseInt(modelId));
				ymm.setSlogan(item.select("slogan").first().text());
				ymm.setLandingPage(item.select("adlink").first().text());
				ymm.setMaxPromotionPrice(item.select("maxpromotionprice")
						.first().text());
				ymm.setGift(item.select("gift").first().text());
				ymm.setMinPrice(item.select("minprice").first().text());
				ymm.setMaxPrice(item.select("maxprice").first().text());
				allYipaiModelMaterial.add(ymm);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return allYipaiModelMaterial;
	}

	/**
	 * author:zhanggd
	 * since:2014-12-02 15:10
	 * 描述：获取所有KPI商品
	 * @return
	 */
	@Override
	public List<ModelMaterial> getYiCheMallKpiModelMaterialList() {
		List<ModelMaterial>  kpiList=new ArrayList<ModelMaterial>();
		
		YiCheMallForAdvertizement  yicheService=new YiCheMallForAdvertizement();
		YiCheMallForAdvertizementSoap yiCheMallSoap = yicheService.getYiCheMallForAdvertizementSoap();
		
		List<KpiEntity> kpiCarInfo = yiCheMallSoap.getKpiCarInfo().getItem();;//获取所有KPI商品
		
		for(int i=0;i<kpiCarInfo.size();i++){
			if(i >= Constant.RECEIVE_DATA_LIMIT){
				logger.error(this.getClass().getClass().getSimpleName()+".getYiCheMallKpiModelMaterialList()→location 0:More than 10W data");
				break;
			}else{
				KpiEntity kpi = kpiCarInfo.get(i);
				ModelMaterial kpiDest=new ModelMaterial();
				kpiDest.setModelId(kpi.getCsId());//id
				kpiDest.setRegion(kpi.getCityId());
				kpiDest.setSlogan(kpi.getSlogan());
				kpiDest.setLandingPage(kpi.getAdlink());
				kpiDest.setPrice(kpi.getMallPrice());
				kpiDest.setShowcarid(kpi.getShowcarid());
				kpiDest.setShowyear(kpi.getShowyear());
				kpiList.add(kpiDest);
			}
		}
		return kpiList;
	}
	
	/**
	 * author:zhanggd
	 * since:2014-12-02 15:10
	 * 描述：获取所有上架状态的商品
	 * @return
	 */
	@Override
	public List<ModelMaterial> getYiCheMallCarModelMaterialList() {
		List<ModelMaterial>  carList=new ArrayList<ModelMaterial>();
		YiCheMallForAdvertizement  yicheService=new YiCheMallForAdvertizement();
		YiCheMallForAdvertizementSoap yiCheMallSoap = yicheService.getYiCheMallForAdvertizementSoap();
		List<KpiEntity> allCarInfoList = yiCheMallSoap.getAllCarInfoList().getItem();//获取所有上架状态的商品
		
		for(int i=0;i<allCarInfoList.size();i++){
			if(i >= Constant.RECEIVE_DATA_LIMIT){
				logger.error(this.getClass().getClass().getSimpleName()+".getYiCheMallCarModelMaterialList()→location 0:More than 10W data");
				break;
			}else{
				KpiEntity car =allCarInfoList.get(i);
				ModelMaterial carDest=new ModelMaterial();
				carDest.setModelId(car.getCsId());//id
				carDest.setRegion(car.getCityId());
				carDest.setSlogan(car.getSlogan());
				carDest.setLandingPage(car.getAdlink());
				carDest.setPrice(car.getMallPrice());
				carDest.setShowcarid(car.getShowcarid());
				carDest.setShowyear(car.getShowyear());
				carDest.setName(car.getCarName());
				carDest.setPic210X140(car.getImageUrl());
				carList.add(carDest);
			}
		}
		return carList;
	}

	@Override
	public List<ModelMaterial> getGrouponModelMaterialList() {
		List<ModelMaterial> resultData=new ArrayList<>();
		try {
			Document doc = Jsoup.parse(new URL(Constant.GROUP_INTERFACE_URL).openStream(), "UTF-8", Constant.GROUP_INTERFACE_URL);
			Elements items = doc.select("items");
			if(items!=null && items.size() == 1){
				Elements item = items.select("item");
				for(int i=0;i<item.size();i++){
					if(i >= Constant.RECEIVE_DATA_LIMIT){
						logger.error(this.getClass().getClass().getSimpleName()+".getGrouponModelMaterialList()→location 0:More than 10W data");
						break;
					}else{
						Element e = item.get(i);
						ModelMaterial mm=new ModelMaterial();
						String moid=e.getElementsByTag("CsId").text();
						if(moid != null && !"".equals(moid) && !moid.equals("0")){
							Integer mid=null;
							try{
								mid=Integer.valueOf(moid.trim());
								if(mid == null){
									logger.error(this.getClass().getClass().getSimpleName()+".getGrouponModelMaterialList()→location 3:mid="+mid);
									continue;
								}
								mm.setModelId(mid);
							}catch(NumberFormatException en){
								logger.error(this.getClass().getClass().getSimpleName()+".getGrouponModelMaterialList()→location 2:mid="+mid, en);
								continue;
							}
							
							String regionId = e.getElementsByTag("RegionId").text();
							if(regionId !=null && !"".equals(regionId)){
								Integer regId=null;
								try{
									regId=Integer.valueOf(regionId.trim());
									if(regId == null){
										logger.error(this.getClass().getClass().getSimpleName()+".getGrouponModelMaterialList()→location 5:regId="+regId);
										continue;
									}
									mm.setRegion(regId);
								}catch(NumberFormatException enn){
									logger.error(this.getClass().getClass().getSimpleName()+".getGrouponModelMaterialList()→location 4:regionId="+regionId, enn);
									continue;
								}
								
							}else{
								logger.error(this.getClass().getClass().getSimpleName()+".getGrouponModelMaterialList()→location 6:regionId="+regionId);
								continue;
							}
							mm.setSlogan(e.getElementsByTag("Slogan").text().trim());
							mm.setLandingPage(e.getElementsByTag("Adlink").text().trim());
							
							String grouponCount=e.getElementsByTag("GrouponNumber").text();
							if(grouponCount != null && !"".equals(grouponCount)){
								Integer gCount=null;
								try{
									gCount=Integer.valueOf(grouponCount.trim());
									mm.setGroupoNum(gCount);
								}catch(NumberFormatException ennn){
									logger.error(this.getClass().getClass().getSimpleName()+".getGrouponModelMaterialList()→location 7:moid="+moid+"  GrouponNumber="+regionId);
									mm.setGroupoNum(0);
								}
							}else{
								mm.setGroupoNum(0);
							}
							String text = e.getElementsByTag("GrouponEndTime").text();
							if(text == null || text.equals("")){
								logger.error(e.toString());
								continue;
							}else{
								mm.setGroupEndTime(DateUtil.textFormatDate(DateEnum.FORMAT_DATE_2, text.trim()));
								resultData.add(mm);
							}	
						}else{
							logger.error(this.getClass().getClass().getSimpleName()+".getGrouponModelMaterialList()→location 1:moid="+moid);
						}
					}
				}
			}else{
				logger.error("read grouponAll interface is items error:items_size="+items.size());
			}
		} catch (IOException e) {
			logger.error("read grouponAll interface is error:",e);
		}
		return resultData;
	}

	@Override
	public List<ModelMaterial> getGrouponHotModelMaterialList() {
		List<ModelMaterial> resultData=new ArrayList<>();
		try {
			Document doc = Jsoup.parse(new URL(Constant.GROUPHOT_INTERFACE_URL).openStream(), "UTF-8", Constant.GROUPHOT_INTERFACE_URL);
			Elements items = doc.select("items");
			if(items!=null && items.size() == 1){
				Elements item = items.select("item");
				for(int i=0;i<item.size();i++){
					if(i >= Constant.RECEIVE_DATA_LIMIT){
						logger.error(this.getClass().getClass().getSimpleName()+".getGrouponHotModelMaterialList()→location 0:More than 10W data");
						break;
					}else{
						Element e = item.get(i);
						ModelMaterial mm=new ModelMaterial();
						String moid=e.getElementsByTag("CsId").text();
						if(moid != null && !"".equals(moid) && !moid.equals("0")){
							Integer mid=null;
							try{
								mid=Integer.valueOf(moid.trim());
								if(mid == null){
									logger.error(this.getClass().getClass().getSimpleName()+".getGrouponHotModelMaterialList()→location 3:mid="+mid);
									continue;
								}
								mm.setModelId(mid);
							}catch(NumberFormatException en){
								logger.error(this.getClass().getClass().getSimpleName()+".getGrouponHotModelMaterialList()→location 2:mid="+mid, en);
								continue;
							}
							
							String regionId = e.getElementsByTag("RegionId").text();
							if(regionId !=null && !"".equals(regionId)){
								Integer regId=null;
								try{
									regId=Integer.valueOf(regionId.trim());
									if(regId == null){
										logger.error(this.getClass().getClass().getSimpleName()+".getGrouponHotModelMaterialList()→location 5:regId="+regId);
										continue;
									}
									mm.setRegion(regId);
								}catch(NumberFormatException enn){
									logger.error(this.getClass().getClass().getSimpleName()+".getGrouponHotModelMaterialList()→location 4:regionId="+regionId, enn);
									continue;
								}
							}else{
								logger.error(this.getClass().getClass().getSimpleName()+".getGrouponHotModelMaterialList()→location 6:regionId="+regionId);
								continue;
							}
							mm.setSlogan(e.getElementsByTag("Slogan").text().trim());
							mm.setLandingPage(e.getElementsByTag("Adlink").text().trim());
							
							String grouponCount=e.getElementsByTag("GrouponNumber").text();
							if(grouponCount != null && !"".equals(grouponCount)){
								Integer gCount=null;
								try{
									gCount=Integer.valueOf(grouponCount.trim());
									mm.setGroupoNum(gCount);
								}catch(NumberFormatException ennn){
									logger.error(this.getClass().getClass().getSimpleName()+".getGrouponHotModelMaterialList()→location 7:moid="+moid+"  GrouponNumber="+regionId);
									mm.setGroupoNum(0);
								}
							}else{
								mm.setGroupoNum(0);
							}
							String text = e.getElementsByTag("GrouponEndTime").text();
							if(text == null || text.equals("")){
								logger.error(e.toString());
								continue;
							}else{
								mm.setGroupEndTime(DateUtil.textFormatDate(DateEnum.FORMAT_DATE_2,text.trim()));
								resultData.add(mm);
							}	
						}else{
							logger.error(this.getClass().getClass().getSimpleName()+".getGrouponHotModelMaterialList()→location 1:moid="+moid);
						}
					}
				}
			}else{
				logger.error("read grouponHot interface is items error:items_size="+items.size());
			}
		} catch (IOException e) {
			logger.error("read grouponHot interface is error:",e);
		}
		return resultData;
	}
	
	@Override
	public Map<Integer, List<ModelMaterial>> getActivityModelMaterialMap() {
		Map<Integer, List<ModelMaterial>> resultData=new HashMap<>();
		List<ModelMaterial> activeModelMaterials=new ArrayList<>();
		List<ModelMaterial> activeTopModelMaterials=new ArrayList<>();
		try {
			//Document doc = Jsoup.connect(Constant.Activity_INTERFACE_URL).timeout(Constant.CONNECTION_TIME_OUT).get();
			Document doc = Jsoup.parse(new URL(Constant.Activity_INTERFACE_URL).openStream(), "UTF-8", Constant.Activity_INTERFACE_URL);
			Elements items = doc.select("items");
			if(items!=null && items.size() == 1){
				Elements item = items.select("item");
				if(item.size() >=Constant.RECEIVE_DATA_LIMIT){
					logger.error(this.getClass().getClass().getSimpleName()+".getActivityModelMaterialMap():ActivityDAO More than 100000 data");
					return resultData;
				}
				for(int i=0;i<item.size();i++){
					Element e = item.get(i);
					String reginText = e.getElementsByTag("RegionId").text();
					if(reginText != null && !"".equals(reginText)){
						Integer region =null;
						try{
							region = Integer.valueOf(reginText.trim());
							if(region == null){
								logger.error(this.getClass().getClass().getSimpleName()+".getActivityModelMaterialMap():location_3 region_id="+region);
								continue;
							}
						}catch(NumberFormatException en){
							logger.error(this.getClass().getClass().getSimpleName()+".getActivityModelMaterialMap():regionId formatNumber error:location_2", en);
							continue;
						}

						String model_id=e.getElementsByTag("Model_id").text();
						if(!"".equals(model_id) && model_id != null && !"0".equals(model_id)){
							ModelMaterial mm=new ModelMaterial();
							Integer moid=null;
							try{
								moid=Integer.valueOf(model_id.trim());
							}catch(NumberFormatException een){
								logger.error(this.getClass().getClass().getSimpleName()+".getActivityModelMaterialMap():location_4 model_id formatNumber error!", een);
								continue;
							}
							mm.setModelId(moid);
							mm.setRegion(region);
							mm.setPrefer_info(e.getElementsByTag("Prefer_info").text().trim());
							mm.setName(e.getElementsByTag("Model_name").text().trim());
							mm.setSlogan(e.getElementsByTag("Slogan").text().trim());
							mm.setLandingPage(e.getElementsByTag("Adlink").text().trim());
							mm.setBrandName(e.getElementsByTag("brand_name").text());//新增车系字段
							String signupNumber=e.getElementsByTag("SignupNumber").text();
							if("".equals(signupNumber) || signupNumber == null){
								mm.setSignupNumber(0);
								logger.error(this.getClass().getClass().getSimpleName()+".getActivityModelMaterialMap():location_5 signupNumber="+signupNumber);
							}else{
								Integer personNumber=null;
								try{
									personNumber=Integer.valueOf(signupNumber.trim());
								}catch(NumberFormatException eeen){
									logger.error("personNumber formatNumber error!", eeen);
									mm.setSignupNumber(0);
								}
								if(personNumber == null || personNumber == 0){
									mm.setSignupNumber(0);
									logger.error(this.getClass().getClass().getSimpleName()+".getActivityModelMaterialMap():location_6 personNumber="+personNumber);
								}else{
									mm.setSignupNumber(personNumber);
								}
							}
							if(mm.getRegion() == 0){
								activeTopModelMaterials.add(mm);
							}else{
								activeModelMaterials.add(mm);
							}
						}else{
							logger.error(this.getClass().getClass().getSimpleName()+".getActivityModelMaterialMap():location_7 model_id="+model_id);
						}
						
					}else{
						logger.error(this.getClass().getClass().getSimpleName()+".getActivityModelMaterialMap():location_8 region_id="+reginText);
					}
				}
				resultData.put(1, activeModelMaterials);
				resultData.put(2, activeTopModelMaterials);
			}
		} catch (IOException e) {
			logger.error("load activty interface is error:", e);
		}
		return resultData;
	}
	
	
	@Override
	public Map<String, ModelMaterial> getAllMallModelMaterial() {
		// 从mysql获取所有ModelMaterial
		Map<String, ModelMaterial> result = new HashMap<>();
		Connection dbConnection = getDBConnection();
		PreparedStatement preparedStatement = null;
		try {
			preparedStatement = dbConnection.prepareStatement(GET_MODEL_PIC_URL_YEAR);
//			preparedStatement.setInt(1, mid);
//			preparedStatement.setString(2, year);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				ModelMaterial mm = null;
				int modelId = rs.getInt("Model");
				int carYear = rs.getInt("CarYear");
				String key = modelId + "_" + carYear;
				if (result.containsKey(key)) {
					mm = result.get(key);
				} else {
					mm = new ModelMaterial();
					mm.setModelId(modelId);
					mm.setCarYear(carYear);
					result.put(key, mm);
				}
				if ((mm.getName() == null) || ("".equals(mm.getName()))) {
					mm.setName(rs.getString("name"));
				}
				String modelPicUrl = rs.getString("ImageUrl");
				int spec = rs.getInt("specification");
				switch (spec) {
//				case 1:
//					mm.setPic150X100(modelPicUrl);
//					break;
//				case 2:
//					mm.setPic120X80(modelPicUrl);
//					break;
				case 3:
					mm.setPic210X140(modelPicUrl);
					break;
				case 5:
					mm.setPic90x60(modelPicUrl);
					break;
//				case 6:
//					mm.setPic300x200(modelPicUrl);
//					break;
//				case 7:
//					mm.setPic150x100(modelPicUrl);
//					break;
//				case 21:
//					mm.setPic120x80(modelPicUrl);
//					break;
				default:
					logger.warn("Illegal model pic specification! spec:" + spec);
					break;
				}
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (preparedStatement != null) {
				try {
					preparedStatement.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return result;
	}

	@Override
	public List<ModelMaterial> getHuimaicheModelMaterialMap() {
		//TODO 惠买车接口
		List<ModelMaterial> promotion=new ArrayList<ModelMaterial>();
		List<ModelMaterial> unPromotion=new ArrayList<ModelMaterial>();
		List<ModelMaterial> result=new ArrayList<ModelMaterial>();
		//http拉取数据
		String data = httpURLConnRequest(Constant.HUIMAICHE_INTERFACE_URL,"GET");
		//备份数据路径
		Path newlyCreatedFile = Utils.getNewlyCreatedFileException(Constant.HUIMAICHE_DATA_PATH, "json");
		String path = newlyCreatedFile.toString();
		boolean flag=true;
		if(data == null || data.length() == 0 || "".equals(data) || !data.startsWith("[") || !data.endsWith("]")){
			logger.error("get url:"+Constant.HUIMAICHE_INTERFACE_URL+" is error>>>>>>>>>>>>>>>>>>>[error>>>>>>>>>]");
			data = readJsonFile(path);
			flag=false;
		}
		
		if(data == null || "".equals(data)){
			logger.error("get url:"+Constant.HUIMAICHE_INTERFACE_URL+" is error>>>>>>>>>>>>>>>>>>>[error>>>>>>>>>]");
			return null;
		}
		JSONArray jarray =null;
		try{
			jarray = new JSONArray(data);
		}catch(JSONException e){
			logger.error("error load data:",e);
			return result;
		}
		if(jarray.length() > 200000)logger.error("data is too big****************:/%",jarray.length());
		for(int i=0;i<jarray.length();i++){
			ModelMaterial mo=new ModelMaterial();
			try{
				JSONObject object=jarray.getJSONObject(i);
				int moid=object.getInt("SerialID");
				if(moid == 0 || moid <0) continue;
				String url=object.getString("Url")== null ?"":object.getString("Url");
				String mUrl=object.getString("MUrl") == null ?"":object.getString("MUrl");
				String img=object.getString("Img") == null ?"":object.getString("Img");
				String serialName=object.getString("SerialName")== null ?"":object.getString("SerialName");
				String desc=object.getString("Desc")== null ?"":object.getString("Desc");
				double referPrice=object.getDouble("ReferPrice");
				double minPrice=object.getDouble("MinPrice");
				int cityId=object.getInt("City");
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
				mo.setPromotion(isPromotion);//是否是双11投放的车型
				
				if(isPromotion){
					promotion.add(mo);
				}else{
					unPromotion.add(mo);
				}
			}catch(JSONException e){
				logger.equals("parse huimaiche_interface is error");
				continue;
			}
		}
		if(promotion != null && promotion.size() >0){
			result.addAll(promotion);
		}
		if(unPromotion != null && unPromotion.size() >0){
			result.addAll(unPromotion);
		}
		promotion = null;
		unPromotion =null;
		if(flag){
			if(data != null && !"".equals(data)){
				writeDataToFile(path,data);
			}
		}
		return result;
	}

	@Override
	public boolean insertUserViewCountOfMinute(Integer positionId,String t,Integer u,Integer deviceId) {
		// TODO 新增用户Impressions统计数按时间
		String[] date = DateUtil.parseTextDateToArray(t);//t yyyy-MM-dd HH:mm
		String day = date[0];//java.sql.Date day = parseStringToSQLDate(string);
		int hour=Integer.parseInt(date[1]);
		
		Connection countDBConnection = getCountDBConnection();
		PreparedStatement preparedStatement = null;
		PreparedStatement inert=null;
		PreparedStatement update=null;
		try{
			preparedStatement = countDBConnection.prepareStatement(QUERY_BITAI_BY_PID_DATE);			preparedStatement.setInt(1, positionId);
			preparedStatement.setInt(1, positionId);//TODO加特殊处理
			preparedStatement.setString(2, day);//TODO加特殊处理
			preparedStatement.setInt(3, hour);//TODO加特殊处理
			preparedStatement.setInt(4, deviceId);//TODO加特殊处理
			ResultSet rs = preparedStatement.executeQuery();
			rs.last();
			int row = rs.getRow();
			if(row == 0){
				//insert table
				inert = countDBConnection.prepareStatement(INSERT_BITAI_BY_PID_DATE);
				inert.setString(1, day);
				inert.setInt(2, hour);
				inert.setInt(3, deviceId);
				inert.setInt(4, positionId);
				inert.setInt(5, u);
				inert.execute();
				return true;
			}
			if(row == 1){
				rs.beforeFirst();
				while (rs.next()) {
					int id = rs.getInt("id");
					int count = rs.getInt("impression")+u;
					//updateTable()
					update = countDBConnection.prepareStatement(UPDATE_BITAI_BY_PID_DATE);
					update.setInt(1,count);
					update.setInt(2, id);
					update.execute();
					return true;
				}
			}
		}catch (SQLException e) {
			logger.error(e.getMessage(), e);
			return false;
		}finally{
			if(update != null){
				try {
					update.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(),e);
				}
			}
			if(inert != null){
				try {
					inert.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if(countDBConnection != null){
				try {
					countDBConnection.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}
	
	/**
	 * 描述:获取二手车接口数据
	 */
	@Override
	public List<ModelMaterial> getUsedCarList(){
		List<ModelMaterial> result=new ArrayList<ModelMaterial>();
		String content = httpClient(Constant.USEDCAR_INTERFACE_URL,HttpMethodEnum.GET,null);
		/*服务器备份*/
		Path newlyCreatedFile = Utils.getNewlyCreatedFileException(Constant.USEDCAR_DATA_PATH, "used_car");
		String path = newlyCreatedFile.toString();
		boolean flag=true;
		if(content == null || "".equals(content) || (!content.startsWith("{")) || (!content.endsWith("}"))){
			logger.error("get url:"+Constant.USEDCAR_INTERFACE_URL+" is null");
			content = readJsonFile(path);
			flag=false;
		}
		if(content == null || "".equals(content)){
			logger.error("The "+Constant.USEDCAR_INTERFACE_URL+" json context is empty!");
			return null;
		}
		
		Data data = gson.fromJson(content, Data.class);
		if(data.isSuccess()){
			List<Structer> context = data.getData();
			if(content != null && content.length() > 0){
				if(content.length() > 200000)logger.error("usedCar of data is too big:d%",content.length());
				context.forEach(new Consumer<Structer>() {
					@Override
					public void accept(Structer t) {
						ModelMaterial model=new ModelMaterial();
						Integer csId = t.getCsId();
						if(csId == null|| csId <= 0) return;
						model.setModelId(csId);
						List<String> regionId = t.getRegionId();
						if(((regionId.contains("0") && regionId.size() == 0) || regionId ==null)) return;
						model.setName(t.getCsName());
						model.setSlogan(t.getSlogan());
						model.setLandingPage(t.getAdLink());
						model.setmUrl(t.getMLink());
						String isWarranty = t.getCustom().get("IsWarranty");
						String maxPrice = t.getCustom().get("MaxPrice");
						String minPrice = t.getCustom().get("MinPrice");
						model.setIsWarranty(Integer.parseInt(isWarranty));
						model.setMaxPrice(maxPrice);
						model.setMinPrice(minPrice);
						model.setPic210X140("0");
						if(regionId != null && regionId.size() == 1){
							model.setRegion(Integer.parseInt(regionId.get(0)));
						}
						result.add(model);
					}
				});
			}
		}
			
		if(flag){
			if(content != null && !"".equals(content)){
				writeDataToFile(path,content);
			}
		}
		return result;
	}
	
	@Override
	public Map<String, List<ModelMaterial>> getPrecisionAdvertisingList() {
		Map<String, List<ModelMaterial>> resultMap = new HashMap<>();
		List<ModelMaterial> modelsKpi=new ArrayList<ModelMaterial>();
		List<ModelMaterial> modelsAll=new ArrayList<ModelMaterial>();
		// TODO 易鑫金融数据接口
		
		String url_PrecisionAdvertisingList = String.format(Constant.YX_PADS_URL,"GetPrecisionAdvertisingList");
		String paramFormat = String.format("_pid=%s&_ts=%s&format=%s",Constant.YX_PADS_API_ID,String.valueOf((System.currentTimeMillis() / 1000)),Constant.YX_PADS_DATA_TYPE);
		String signatureStr = paramFormat + Constant.YX_PADS_API_KEY;
		String postData=paramFormat+"&_sign="+MD5(signatureStr);
		String data=httpURLConnRequest(url_PrecisionAdvertisingList+"&"+postData,"GET");
		Path newlyCreatedFile = Utils.getNewlyCreatedFileException(Constant.YX_DATA_PATH, "yx_json");
		String path = newlyCreatedFile.toString();
		boolean flag=true;
		
		if(data == null || "".equals(data) || (!data.startsWith("{")) || (!data.endsWith("}"))){
			logger.error("invoke "+url_PrecisionAdvertisingList+"&"+postData+" is null......");
			data = readJsonFile(path);
		}
		JSONObject json=null;
		try{
			json=new JSONObject(data);
		}catch(JSONException e){
			logger.error("error:",e);
			return resultMap;
		}
		JSONObject head = json.getJSONObject("Head");	
		boolean result = head.getBoolean("Result");
		int statusCode = head.getInt("StatusCode");
		String message = head.getString("Message");
		int reconds = head.getInt("Reconds");
		
		if((result ==false) || (statusCode != 100) || !"success".equals(message) || reconds <= 0){
			logger.error("invoke "+url_PrecisionAdvertisingList+postData+" >>>> is parse json head{p1,p2} is illegitimacy");
			return resultMap;
		}
		
		String body = json.getString("Body");
		if(body == null || "".equals(body)){
			logger.error("invoke "+url_PrecisionAdvertisingList+postData+" result body of data is error!");
			return resultMap;
		}
		
		String result_data = decryptMode(body, Constant.YX_PADS_API_KEY);
		JSONArray jarry=new JSONArray(result_data);
		if(jarry.length() > 200000)logger.error("yinxin of data is too big:d%",jarry.length());
		for(int i=0;i<jarry.length();i++){
			 JSONObject obj = jarry.getJSONObject(i);
			 if(obj != null){
				 ModelMaterial mo=new ModelMaterial();
				 
				 int cid = obj.getInt("CarSeriesId");
				 if(cid ==0 || cid < 0){
					 logger.error("========>GetPrecisionAdvertisingList model id:",cid);
					 continue;
				 }
				 String citys = obj.getString("CityIds");
				 if(citys == null || "".equals(citys)){
					 logger.error("========>GetPrecisionAdvertisingList model cityIds:",cid);
					 continue;
				 }
				 List<String> cityIds = Arrays.asList(citys.split(","));
				 boolean isHot = obj.getBoolean("IsHot");  //是否热门
				 double weight = obj.getDouble("Weight");  //权重
				 String model_url = obj.getString("Url");
				 int applyCount = obj.getInt("ApplyCount"); //申请人数
				 String monthPay=obj.getString("MonthPay");//月供
				 
				 mo.setModelId(cid);
				 mo.setOrigCityIds(cityIds);
				 mo.setLandingPage(model_url);
				 mo.setWeight(weight);
				 mo.setApplyCount(applyCount);
				 mo.setMonthPay(monthPay);
				 if(isHot){
					 modelsKpi.add(mo);
				 }
				 modelsAll.add(mo);
			 }
		}
		
		sortModelsByWeigth(modelsKpi);//降序by权重
		sortModelsByWeigth(modelsAll);//降序by权重
		
		resultMap.put("kpi", modelsAll);
		resultMap.put("all", modelsAll);
		
		if(flag){
			if(data != null && !"".equals(data)){
				writeDataToFile(path,data);
			}
		}
		return resultMap;
	}
	
	public static void writeDataToFile(String filePath,String text){
		if(filePath == null || "".equals(filePath)){
			logger.warn("The filePath is empty....");
			return;
		}
		if(text == null || "".equals(text)){
			logger.warn("The text is empty....");
			return;
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(filePath);
			fos.write(text.getBytes());
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
		}finally{
			try {
				if(fos != null){
					fos.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage(),e);
			} 
		}
	}

	@Override
	public List<ModelMaterial> getYiCheHuiModelMaterialList() {
		List<ModelMaterial> modelsAll=new ArrayList<ModelMaterial>();
		StringBuilder builder=new StringBuilder();
		String content = httpClient(Constant.YICHEHUI_INTERFACE_URL,HttpMethodEnum.POST,builder.append(Constant.YICHEHUI_INTERFACE_KEY));
		if(content == null || "".equals(content) || (!content.startsWith("{")) || (!content.endsWith("}"))){
			logger.warn("The {} is reponse is error_______",Constant.YICHEHUI_INTERFACE_URL);
			return null;
		}
		Data data = gson.fromJson(content, Data.class);
		if(data.isSuccess()){
			List<Structer> structers = data.getData();
			structers.forEach(new Consumer<Structer>() {
				@Override
				public void accept(Structer t) {
					ModelMaterial model=new ModelMaterial();
					model.setModelId(Integer.valueOf(t.getCsId()));
					model.setName(t.getCsName());
					model.setOrigCityIds(t.getRegionId());
					model.setSlogan(t.getSlogan());
					model.setPic210X140("0");
					model.setLandingPage(t.getAdLink());
					model.setmUrl(t.getMLink());
					model.setWeight(t.getWeight());
					model.setBrandName(t.getBrandName());
					model.setShowyear(t.getCustom().get("showyear"));
					if(model.getSlogan() != null && !"".equals(model.getSlogan())){
						modelsAll.add(model);
					}
				}
				
			});
		}
		return modelsAll;
	}

	@Override
	public Map<Integer,List<ModelMaterial>> getHuiMaiCheProduct() {
		Map<Integer,List<ModelMaterial>> result=new HashMap<>();
		try{
			String content = httpClient(Constant.HUIMAICHE_PRODUCT_INF,HttpMethodEnum.GET,null);
			if(content == null || "".equals(content)){
				logger.info("Request "+Constant.HUIMAICHE_PRODUCT_INF+" is faild,request content is null ************");
				return null;
			}
			Product prob = gson.fromJson(content, Product.class);
			boolean success = prob.isSuccess();
			if(success == false){
				logger.info("Request "+Constant.HUIMAICHE_PRODUCT_INF+" is faild,request status is false ************");
				return result;
			}
			if(success){
				List<Feed> feeds = prob.getFeeds();
				feeds.forEach(new Consumer<Feed>() {
					@Override
					public void accept(Feed t) {
						Integer feedid = t.getFeedid();
						List<Block> data = t.getData();
						if(data.size() > 100000){
							logger.warn("The Data is more than a few thousand ##############");
						}
						List<ModelMaterial> temp=new ArrayList<>();
						data.forEach(new Consumer<Block>() {
							@Override
							public void accept(Block t) {
								ModelMaterial model=new ModelMaterial();
								model.setModelId(t.getCsId());
								model.setName(t.getCsName());
								model.setPic210X140(t.getImg());
								model.setBrandName(t.getBrandName());
								model.setLandingPage(t.getAdLink());
								model.setmUrl(t.getmLink());
								model.setCityIds(t.getRegionId());
								model.setOrigCityIds(t.getRegionName());
								model.setPrice(String.valueOf(t.getPrice()));
								model.setReferPrice(String.valueOf(t.getReferPrice()));
								Map<String, String> custom = t.getCustom();
								if(feedid == 1){
									String title = custom.get("title");
									if(title !=null && !"".equals(title)){
										model.setSlogan(title);
									}
								}else{
									model.setSlogan(t.getSlogan());
								}
								if(custom != null){
									String signupNumber = custom.get("SignupNumber");
									if(feedid == 2 && signupNumber !=null && !"".endsWith(signupNumber)){
										model.setSignupNumber(Integer.parseInt(signupNumber));
									}
									String endTime = custom.get("EndTime");
									if(feedid == 2 ){
										if(endTime !=null && !"".endsWith(endTime)){
											model.setGroupEndTime(DateUtil.textFormatDate(DateEnum.FORMAT_DATE_0, endTime.trim()));
										}else{
											return;
										}
									}
								}
								if(t.getWeight() != null){
									model.setWeight(t.getWeight());
								}
								if(model.getModelId() !=null && model.getCityIds().size() >0){
									temp.add(model);
								}
							}
						});
						result.put(feedid, temp);
					}
				});
			}
		}catch(Exception e){
			logger.error("invoke getHuiMaiCheProduct is error....", e);
		}
		return result;
	}

	@Override
	public Map<Integer, List<ModelMaterial>> getHuiMaiCheZXProduct() {
		Map<Integer,List<ModelMaterial>> result=new HashMap<>();
		try{
			String content = httpClient(Constant.HUIMAICHE_PRODUCT_ZX,HttpMethodEnum.GET,null);
			if(content == null || "".equals(content)){
				logger.info("Request "+Constant.HUIMAICHE_PRODUCT_ZX+" is faild,request content is null ************");
				return null;
			}
			Product prob = gson.fromJson(content, Product.class);
			boolean success = prob.isSuccess();
			if(success == false){
				logger.info("Request "+Constant.HUIMAICHE_PRODUCT_ZX+" is faild,request status is false ************");
				return result;
			}
			if(success){
				List<Feed> feeds = prob.getFeeds();
				feeds.forEach(new Consumer<Feed>() {
					@Override
					public void accept(Feed t) {
						Integer feedid = t.getFeedid();
						List<Block> data = t.getData();
						if(data.size() > 100000){
							logger.warn("The Data is more than a few thousand ##############");
						}
						List<ModelMaterial> temp=new ArrayList<>();
						data.forEach(new Consumer<Block>() {
							@Override
							public void accept(Block t) {
								ModelMaterial model=new ModelMaterial();
								model.setModelId(t.getCsId());
								model.setName(t.getCsName());
								model.setPic210X140(t.getImg());
								model.setBrandName(t.getBrandName());
								model.setLandingPage(t.getAdLink());
								model.setmUrl(t.getmLink());
								model.setCityIds(t.getRegionId());
								model.setOrigCityIds(t.getRegionName());
								model.setPrice(String.valueOf(t.getPrice()));
								model.setReferPrice(String.valueOf(t.getReferPrice()));
								if(t.getWeight() != null){
									model.setWeight(t.getWeight());
								}
								if(model.getModelId() !=null && model.getCityIds().size() >0){
									temp.add(model);
								}
								model.setSlogan(t.getSlogan());
							}
						});
						result.put(feedid, temp);
					}
				});
			}
		}catch(Exception e){
			logger.error("invoke getHuiMaiCheProduct is error....", e);
		}
		return result;
	}
}
