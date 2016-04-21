package cn.com.cig.adsense.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.dao.ModelMaterialDao;
import cn.com.cig.adsense.dao.impl.ModelMaterialDaoImpl;
import cn.com.cig.adsense.service.ModelMaterialService;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.utils.date.DateUtil;
import cn.com.cig.adsense.utils.job.ScheduledJob;
import cn.com.cig.adsense.vo.DateEnum;
import cn.com.cig.adsense.vo.fix.CityMapping;
import cn.com.cig.adsense.vo.fix.ModelMaterial;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.concurrent.ConcurrentHashMap;


public class ModelMaterialServiceImpl implements ModelMaterialService{
	private static Logger logger = LoggerFactory.getLogger(ModelMaterialServiceImpl.class);
	
	/* 1)  matchYipaiRegionsTask*/
	private Map<Integer, Integer> matchYipaiRegions = null;
	private Map<Integer, List<Integer>> competitiveModels = null;
	
	/* 2) grouponTask*/
	//private List<ModelMaterial> grouponModelMaterKpi = new ArrayList<>();//
	private List<Integer> grouponHotModelMaterIds = new ArrayList<>();//
	private List<ModelMaterial> grouponModelMaterials = new ArrayList<>();
	private Map<Integer, ModelMaterial> grouponSomeModelMaterials = null;// modelId:促销信息。促销信息不全，每个车型只使用第一个促销信息。
	private List<ModelMaterial> grouponTopN = new ArrayList<>();//团购热门车型
	private List<ModelMaterial> grouponTop9 = new ArrayList<>(Constant.ONE_DELIVERY_NUM);// 团购默认的9个热门车型
	
	/* 3)activityTask*/
	private List<Integer> activityHotModelMaterIds = new ArrayList<>();//
	private List<ModelMaterial> activityModelMaterials = new ArrayList<>();
	private Map<Integer, ModelMaterial> activitySomeModelMaterials = null;// modelId:促销信息。促销信息不全，每个车型只使用第一个促销信息。
	private List<ModelMaterial> activityTopN = new ArrayList<>();//团购热门车型
	private List<ModelMaterial> activityTop9 = new ArrayList<>(Constant.ONE_DELIVERY_NUM);// 团购默认的9个热门车型
	
	/* 4)huimaicheTask*/
	private List<ModelMaterial> huimaicheModelMaterials = new ArrayList<>();
	private Map<Integer, ModelMaterial> huimaicheAllModelMaterials = null;
	private List<ModelMaterial> huimaicheTopN = new ArrayList<>();
	private List<ModelMaterial> huimaicheTop9 = new ArrayList<>(Constant.ONE_DELIVERY_NUM);
	
	/* 5)usedCarTask*/
	private List<Integer> usedCarModelMaterKpiIds = new ArrayList<>();//
	private List<ModelMaterial> usedCarModelMaterials = new ArrayList<>();
	private Map<Integer, ModelMaterial> usedCarAllModelMaterials = null;
	private List<ModelMaterial> usedCarTopN = new ArrayList<>();
	private List<ModelMaterial> usedCarTop9 = new ArrayList<>(Constant.ONE_DELIVERY_NUM);
	
	/* 6)yixinSynTask*/
	private Set<Integer> yixinDynModelMaterKpiIds = new LinkedHashSet<>();//
	private List<ModelMaterial> yixinDynModelMaterials = new ArrayList<>();
	private Map<Integer, ModelMaterial> yixinDynAllModelMaterials = null;
	private List<ModelMaterial> yixinDynTopN = new ArrayList<>();
	private List<ModelMaterial> yixinDynTop9 = new ArrayList<>(Constant.ONE_DELIVERY_NUM);
	
	/* 7)yichehuiDynTask*/
	private Set<Integer> yichehuiDynModelMaterKpiIds = new LinkedHashSet<>();
	private List<ModelMaterial> yichehuiDynModelMaterials = new ArrayList<>();
	private Map<Integer, ModelMaterial> yichehuiDynAllModelMaterials = null;
	private List<ModelMaterial> yichehuiDynTopN = new ArrayList<>();
	private List<ModelMaterial> yichehuiDynTop9 = new ArrayList<>(Constant.ONE_DELIVERY_NUM);
	
	/* 8.1) huimaicheDynTask*/
	private Set<Integer> hmc_mmds_one_ids = new LinkedHashSet<>();
	private List<ModelMaterial> hmc_mmds_one_list = new ArrayList<>();
	private Map<Integer, ModelMaterial> hmc_mmds_one_map = null;
	private List<ModelMaterial> hmc_mmds_one_topN = new ArrayList<>();
	private List<ModelMaterial> hmc_mmds_one_top9 = new ArrayList<>(Constant.ONE_DELIVERY_NUM);
	
	/* 8.2) huimaicheDynTask*/
	/*private Set<Integer> hmc_mmds_two_ids = new LinkedHashSet<>();
	private List<ModelMaterial> hmc_mmds_two_list = new ArrayList<>();
	private Map<Integer, ModelMaterial> hmc_mmds_two_map = null;
	private List<ModelMaterial> hmc_mmds_two_topN = new ArrayList<>();
	private List<ModelMaterial> hmc_mmds_two_top9 = new ArrayList<>(Constant.ONE_DELIVERY_NUM);*/
	
	/* 8.3) huimaicheDynTask*/
	private Set<Integer> hmc_mmds_four_ids = new LinkedHashSet<>();
	private List<ModelMaterial> hmc_mmds_four_list = new ArrayList<>();
	private Map<Integer, ModelMaterial> hmc_mmds_four_map = null;
	private List<ModelMaterial> hmc_mmds_four_topN = new ArrayList<>();
	private List<ModelMaterial> hmc_mmds_four_top9 = new ArrayList<>(Constant.ONE_DELIVERY_NUM);
	
	/* 0)all materials*/
	private Map<Integer, ModelMaterial> whiteModelMaterialSizeMap=null;
	private Map<String, ModelMaterial> whiteMaterialSizeYearMap=null;
	
	private final ModelMaterialDao dao = new ModelMaterialDaoImpl();
	private Map<Integer,CityMapping> cityMapping=Utils.getCityMapping();//城市指定所属投放中心城市

	public static boolean flag=true;
	public static boolean usedCar_flag=true;
	public static boolean yx_flag=true;
	//TODO 
	private static Map<String,Integer> map=new ConcurrentHashMap<>();
	
	
	private ModelMaterialServiceImpl() {
		ScheduledJob.getInstance().scheduleAtFixedRate(matchYipaiRegionsTask,0, 1, TimeUnit.HOURS);// 每天更新一次。
		ScheduledJob.getInstance().scheduleAtFixedRate(whiteMaterial,0,1,TimeUnit.HOURS);
		ScheduledJob.getInstance().scheduleAtFixedRate(huimaicheTask,10,120,TimeUnit.SECONDS);//每天更新一次
		ScheduledJob.getInstance().scheduleAtFixedRate(usedcarTask,20,120,TimeUnit.SECONDS);  //每天更新一次
		ScheduledJob.getInstance().scheduleAtFixedRate(yichehuiTask,10, 60,TimeUnit.SECONDS);  //一分钟更新一次
		ScheduledJob.getInstance().scheduleAtFixedRate(counter,0, 60,TimeUnit.SECONDS);
		ScheduledJob.getInstance().scheduleAtFixedRate(yxDyninTask,25, 120,TimeUnit.SECONDS); //每天更新一次
		ScheduledJob.getInstance().scheduleAtFixedRate(hmc_job_1,10, 140,TimeUnit.SECONDS);    //一分钟更新一次
		//ScheduledJob.getInstance().scheduleAtFixedRate(hmc_job_2,15, 150,TimeUnit.SECONDS);  //一分钟更新一次
		ScheduledJob.getInstance().scheduleAtFixedRate(hmc_job_4,15, 150,TimeUnit.SECONDS);  //一分钟更新一次
	}
	
	private Runnable yichehuiTask=new Runnable(){

		@Override
		public void run() {
			try{
				if(whiteModelMaterialSizeMap == null || whiteModelMaterialSizeMap.size() ==0){
					logger.error("The whiteModelMaterialSizeMap is empty >>>>>>>>");
					return;
				}
				if(whiteMaterialSizeYearMap == null || whiteMaterialSizeYearMap.size() ==0){
					logger.error("The whiteMaterialSizeYearMap is empty >>>>>>>>");
					return;
				}
				List<ModelMaterial> allData = dao.getYiCheHuiModelMaterialList();
				logger.info("The yichehui data_size:{}",allData.size());
				if(allData==null || allData.size() == 0){
					logger.error("The YiCheHuiModelMaterialList is empty >>>>>>>>");
					return;
				}
				Map<Integer, ModelMaterial> tempAllModelMaterials = new LinkedHashMap<Integer, ModelMaterial>();
				Set<Integer> tempIds=new LinkedHashSet<>(4000);
				for(int i=0;i<allData.size();i++){
					ModelMaterial origModel = allData.get(i);
					Integer csId = origModel.getModelId();
					if(csId == null || csId ==0){
						logger.warn("The material:ID:{} is Invalided!",csId);
						continue;
					}
					String showyear = origModel.getShowyear();
					String img = origModel.getPic210X140();
					ModelMaterial tempModel=null;
					if( img == null || "0".equals(img) || "".equals(img)){
						if(!"".equals(showyear) && showyear != null){
							String key = csId + "_" + showyear;
							if(whiteMaterialSizeYearMap.containsKey(key)){
								tempModel = whiteMaterialSizeYearMap.get(key);
							}
						}
						if(tempModel == null){
							tempModel = whiteModelMaterialSizeMap.get(csId);
						}
						
						if (tempModel == null) {
							logger.warn("Have not find model material in mysql! modelId:"+ csId);
							continue;
						} 
						origModel.setPic210X140(tempModel.getPic210X140());
					}
					
					if(!tempAllModelMaterials.containsKey(csId)){
						tempAllModelMaterials.put(csId, origModel);
						tempIds.add(csId);
					}
				}
				
				if (tempAllModelMaterials.size() == 0 || tempAllModelMaterials.size() ==0) {
					logger.error("yichehui tempYichehui:"+tempAllModelMaterials.size()+" or tempYichehui is:"+tempAllModelMaterials.size());
				} else {
					yichehuiDynModelMaterials =allData;
					yichehuiDynAllModelMaterials = tempAllModelMaterials;
					yichehuiDynModelMaterKpiIds=tempIds;
					yichehuiDynTopN=allData;
					yichehuiDynTop9 = allData.subList(0,Constant.ONE_DELIVERY_NUM);
				}
				
			}catch(Exception e){
				logger.error("run yichehuiTask is fail >>>>>>>>>>>>");
			}
		}
		
	};
	
	private Runnable whiteMaterial=new Runnable(){
		@Override
		public void run() {
			// TODO 更新白底图物料
			try{
				Map<Integer, ModelMaterial> whiteMaterialMap = dao.getAllModelMaterial();
				Map<String, ModelMaterial> whiteMaterialMapYearByMap = dao.getAllMallModelMaterial();
				if(whiteMaterialMap == null || whiteMaterialMap.size() == 0){
					logger.error("whiteMaterials is empty");
					return;
				}
				whiteModelMaterialSizeMap=whiteMaterialMap;
				if(whiteMaterialMapYearByMap == null || whiteMaterialMapYearByMap.size() ==0){
					logger.error("whiteMaterials is empty");
					return;
				}
				whiteMaterialSizeYearMap=whiteMaterialMapYearByMap;
			}catch(Exception e){
				logger.error("init whiteMaterials is errror >>>>>>>>>>>>", e);
			}
		}
	};
	
	private Runnable yxDyninTask=new Runnable(){
		@Override
		public void run() {
			//TODO 易鑫资本
			try{
				if(yx_flag){
					initYxData();
					yx_flag=false;
				}else{
					if(DateUtil.getHourseOfDay()==3 || DateUtil.getHourseOfDay()==18){
						initYxData();
					}
				}
			}catch(Exception e){
				logger.error("invoke yixinDynTask is error!", e);
			}
			logger.info("yixinDynTask to complete___");
		}

	};
	
	// 二手车数据定时器
	private Runnable usedcarTask=new Runnable(){

		@Override
		public void run() {
			try{
				if(usedCar_flag){
					initUsedCarData();
					usedCar_flag=false;
				}else{
					if(DateUtil.getHourseOfDay()==2){
						initUsedCarData();
					}
				}
			}catch(Exception e){
				logger.error("invoke usedcarTask is error!", e);
			}
			logger.info("usedcarTask to complete.");
		}
		
	};
	
	//统计UV曝光数
	private Runnable counter=new Runnable() {
		
		@Override
		public void run() {
			// TODO 给老莫加曝光统计量
			try{
				map.forEach(new BiConsumer<String, Integer>() {
					@Override
					public void accept(String t, Integer u) {
						long t1 = DateUtil.getFormatDateMillis(DateEnum.FORMAT_DATE_2, new Date());
						
						String[] kt=t.split("_");
						String key=kt[0];
						Integer pid=Integer.parseInt(kt[1]);
						Integer deviceId=Integer.parseInt(kt[2]);
						
						long t2 = DateUtil.textFormatDate(DateEnum.FORMAT_DATE_2, key).getTime();
						if(t1>t2){
							if(dao.insertUserViewCountOfMinute(pid,key,u,deviceId)){
								logger.info(map.toString());
								map.remove(t);
							}
						}
					}
				});
			}catch(Exception e){
				logger.error("invoke counter is error!", e);
			}
			
		}
	};
	
	
	private Runnable matchYipaiRegionsTask = new Runnable() {
		@Override
		public void run() {
			try{
				matchYipaiRegions = Utils.matchYipaiRegions();
				competitiveModels = dao.getCompetitiveModels();
			} catch (Exception e) {
				logger.error("Get competitive models task failed!", e);
			}
		}
	};
	
	private Runnable huimaicheTask = new Runnable(){

		@Override
		public void run() {
			logger.info("huimaicheTask start....");
			//TODO 重构
			if(flag){//第一次执行
				initHuiMaiCheTask();
				flag=false;
			}else{
				if(DateUtil.getHourseOfDay()==1){
					initHuiMaiCheTask();
				}
			}
			logger.info("huimaicheTask to  complete....................");
		}
	};
	
	private Runnable hmc_job_1 = new Runnable(){
		@Override
		public void run() {
			try{
				if(whiteModelMaterialSizeMap == null || whiteModelMaterialSizeMap.size() ==0){
					logger.error("The whiteModelMaterialSizeMap is empty >>>>>>>>");
					return;
				}
				Map<Integer, List<ModelMaterial>> huiMaiCheProduct = dao.getHuiMaiCheProduct();
				List<ModelMaterial> list = huiMaiCheProduct.get(1);
				if(list == null || list.size() == 0){
					logger.error("The huimaicheProductOne job get Data source_1 is null**************");
					return;
				}
				Map<String, ModelMaterial> tempMaterials = new LinkedHashMap<>();
				Map<Integer, ModelMaterial> tempKeysMaterials = new LinkedHashMap<>();
				Set<Integer> tempIds=new LinkedHashSet<>();
				List<ModelMaterial> temp_list = new ArrayList<>();
				List<Integer> temp_ids_list=new ArrayList<>();
				for(ModelMaterial model:list){
					Integer modelId = model.getModelId();
					if(model  == null || modelId == null || modelId == 0){
						logger.warn("The model is empty!");
						continue;
					}
					if(model.getCityIds() == null || model.getCityIds().size() == 0 ){
						continue;
					}
					ModelMaterial mm = whiteModelMaterialSizeMap.get(modelId);	
					if(model.getName() == null || "".equals(model.getName())){
						model.setName(mm.getName());
					}
					if(model.getPic210X140() == null || model.getPic210X140().equals("0") || model.getPic210X140().equals("")){
						model.setPic210X140(mm.getPic210X140());
					}
					if(!tempMaterials.containsKey(modelId+model.getCityIds().toString())){
						tempMaterials.put(modelId+model.getCityIds().toString(), model);
						temp_list.add(model);
						temp_ids_list.add(modelId);
						tempKeysMaterials.put(modelId, model);
						tempIds.add(modelId);
					}
				}
				hmc_mmds_one_list = temp_list;//赋值给团购全物料
				hmc_mmds_one_ids=tempIds;
				hmc_mmds_one_map = tempKeysMaterials;
				hmc_mmds_one_topN = temp_list;
				hmc_mmds_one_top9 = temp_list.subList(0,Constant.ONE_DELIVERY_NUM);
			}catch(Exception e){
				logger.error("huimaicheProductOne scheduled job error!", e);
			}
			logger.info("huimaicheProductOne to  complete....................");
		}
	};
	
	/*private Runnable hmc_job_2 = new Runnable(){
		@Override
		public void run() {
			try{
				if(whiteModelMaterialSizeMap == null || whiteModelMaterialSizeMap.size() ==0){
					logger.error("The whiteModelMaterialSizeMap is empty >>>>>>>>");
					return;
				}
				Map<Integer, List<ModelMaterial>> huiMaiCheProduct = dao.getHuiMaiCheProduct();
				List<ModelMaterial> list = huiMaiCheProduct.get(2);
				if(list == null || list.size() == 0){
					logger.error("The huimaicheProductOne job get Data source_2 is null**************");
					return;
				}
				Map<String, ModelMaterial> tempMaterials = new LinkedHashMap<>();
				Map<Integer, ModelMaterial> tempKeysMaterials = new LinkedHashMap<>();
				List<ModelMaterial> temp_list = new ArrayList<>();
				List<Integer> temp_ids_list=new ArrayList<>();
				Set<Integer> tempIds=new LinkedHashSet<>();
				for(ModelMaterial model:list){
					Integer modelId = model.getModelId();
					if(model  == null || modelId == null || modelId == 0){
						logger.warn("The model is empty!");
						continue;
					}
					if(model.getCityIds() == null || model.getCityIds().size() == 0 ){
						continue;
					}
					ModelMaterial mm = whiteModelMaterialSizeMap.get(modelId);	
					if(model.getName() == null || "".equals(model.getName())){
						model.setName(mm.getName());
					}
					if(model.getPic210X140() == null || model.getPic210X140().equals("0") || model.getPic210X140().equals("")){
						model.setPic210X140(mm.getPic210X140());
					}
					if(!tempMaterials.containsKey(modelId+model.getCityIds().toString())){
						tempMaterials.put(modelId+model.getCityIds().toString(), model);
						temp_list.add(model);
						temp_ids_list.add(modelId);
						tempKeysMaterials.put(modelId, model);
						tempIds.add(modelId);
					}
				}
				hmc_mmds_two_list = temp_list;//赋值给团购全物料
				hmc_mmds_two_ids=tempIds;
				hmc_mmds_two_map = tempKeysMaterials;
				hmc_mmds_two_topN = temp_list;
				hmc_mmds_two_top9 = temp_list.subList(0,Constant.ONE_DELIVERY_NUM);
			}catch(Exception e){
				logger.error("huimaicheProductTwo scheduled job error!", e);
			}
			logger.info("huimaicheProductTwo to  complete....................");
		}
	};*/
	
	private Runnable hmc_job_4 = new Runnable(){
		@Override
		public void run() {
			try{
				if(whiteModelMaterialSizeMap == null || whiteModelMaterialSizeMap.size() ==0){
					logger.error("The whiteModelMaterialSizeMap is empty >>>>>>>>");
					return;
				}
				Map<Integer, List<ModelMaterial>> huiMaiCheZXProduct = dao.getHuiMaiCheZXProduct();
				List<ModelMaterial> list = huiMaiCheZXProduct.get(4);
				if(list == null || list.size() == 0){
					logger.error("The huimaicheProductOne job get Data source_1 is null**************");
					return;
				}
				Map<String, ModelMaterial> tempMaterials = new LinkedHashMap<>();
				Map<Integer, ModelMaterial> tempKeysMaterials = new LinkedHashMap<>();
				Set<Integer> tempIds=new LinkedHashSet<>();
				List<ModelMaterial> temp_list = new ArrayList<>();
				List<Integer> temp_ids_list=new ArrayList<>();
				for(ModelMaterial model:list){
					Integer modelId = model.getModelId();
					if(model  == null || modelId == null || modelId == 0){
						logger.warn("The model is empty!");
						continue;
					}
					if(model.getCityIds() == null || model.getCityIds().size() == 0 ){
						continue;
					}
					ModelMaterial mm = whiteModelMaterialSizeMap.get(modelId);	
					if(model.getName() == null || "".equals(model.getName())){
						model.setName(mm.getName());
					}
					if(model.getPic210X140() == null || model.getPic210X140().equals("0") || model.getPic210X140().equals("")){
						model.setPic210X140(mm.getPic210X140());
					}
					if(!tempMaterials.containsKey(modelId+model.getCityIds().toString())){
						tempMaterials.put(modelId+model.getCityIds().toString(), model);
						temp_list.add(model);
						temp_ids_list.add(modelId);
						tempKeysMaterials.put(modelId, model);
						tempIds.add(modelId);
					}
				}
				hmc_mmds_four_list = temp_list;//赋值给团购全物料
				hmc_mmds_four_ids=tempIds;
				hmc_mmds_four_map = tempKeysMaterials;
				hmc_mmds_four_topN = temp_list;
				hmc_mmds_four_top9 = temp_list.subList(0,Constant.ONE_DELIVERY_NUM);
			}catch(Exception e){
				logger.error("huimaicheProductFour scheduled job error!", e);
			}
			logger.info("huimaicheProductFour to  complete....................");
		}
	};
	
	
	
	@Override
	public List<ModelMaterial> getTop9(int positionIdTyp) {
		switch(positionIdTyp){
			case Constant.GROUP_ON:{
				return grouponTop9;
			}
			case Constant.ACTIVITY:{
				return activityTop9;
			}
			case Constant.HUIMAICHE:{
				return huimaicheTop9;
			}
			case Constant.HUIMAICHE_FIX:{
				//return huimaicheTop9;
				return hmc_mmds_one_top9;
			}
			case Constant.USED_CAR:{
				return usedCarTop9;
			}
			case Constant.YIXIN:{
				return yixinDynTop9;
			}
			case Constant.YIXIN_DYN:{
				return yixinDynTop9;
			}
			case Constant.YICHEHUI_DYN:{
				return yichehuiDynTop9;
			}
			case Constant.HUIMAICHE_DYN_ONE:{
				return hmc_mmds_one_top9;
			}
			/*case Constant.HUIMAICHE_DYN_TWO:{
				return hmc_mmds_two_top9;
			}*/
			case Constant.HUIMAICHE_DYN_FOUR:{
				return hmc_mmds_four_top9;
			}
			default:{
				logger.warn("Illegal positionIdTyp:" + positionIdTyp);
				return new ArrayList<>(1);
			}
		}
	}
	
	@Override
	public Collection<ModelMaterial> getTopNByAllModels(Set<Integer> models,int positionIdTyp) {
		switch(positionIdTyp){
			case Constant.GROUP_ON:{
				return getGenTopNBySomeModelsAndTopN(positionIdTyp,models,grouponSomeModelMaterials,grouponTopN);
			}
			case Constant.ACTIVITY:{
				return getGenTopNBySomeModelsAndTopN(positionIdTyp,models,activitySomeModelMaterials,activityTopN);
			}
			case Constant.HUIMAICHE:{
				return getGenTopNBySomeModelsAndTopN(positionIdTyp,models,huimaicheAllModelMaterials,huimaicheTopN);
			}
			case Constant.HUIMAICHE_FIX:{
				return getGenTopNBySomeModelsAndTopN(positionIdTyp,models,hmc_mmds_one_map,hmc_mmds_one_topN);
			}
			case Constant.USED_CAR:{
				return getGenTopNBySomeModelsAndTopN(positionIdTyp,models,usedCarAllModelMaterials,usedCarTopN);
			}
			case Constant.YIXIN:{
				return getGenTopNBySomeModelsAndTopN(positionIdTyp,models,yixinDynAllModelMaterials,yixinDynTopN);
			}
			case Constant.YIXIN_DYN:{
				return getGenTopNBySomeModelsAndTopN(positionIdTyp,models,yixinDynAllModelMaterials,yixinDynTopN);
			}
			case Constant.YICHEHUI_DYN:{
				return getGenTopNBySomeModelsAndTopN(positionIdTyp,models,yichehuiDynAllModelMaterials,yichehuiDynTopN);
			}
			case Constant.HUIMAICHE_DYN_ONE:{
				return getGenTopNBySomeModelsAndTopN(positionIdTyp,models,hmc_mmds_one_map,hmc_mmds_one_topN);
			}
			/*case Constant.HUIMAICHE_DYN_TWO:{
				return getGenTopNBySomeModelsAndTopN(positionIdTyp,models,hmc_mmds_two_map,hmc_mmds_two_topN);
			}*/
			case Constant.HUIMAICHE_DYN_FOUR:{
				return getGenTopNBySomeModelsAndTopN(positionIdTyp,models,hmc_mmds_four_map,hmc_mmds_four_topN);
			}
			default:{
				logger.warn("Illegal positionIdTyp:" + positionIdTyp);
				return new ArrayList<>(1);
			}
		}
	}
	
	@Override
	public Collection<ModelMaterial> getTopNByProvinceAndcity(int province, int city,int positionIdTyp) {
		switch(positionIdTyp){
			case Constant.GROUP_ON:{
				return getGenTopNByProvinceAndcity(positionIdTyp,grouponHotModelMaterIds,grouponTopN,grouponModelMaterials,province,city);
			}
			case Constant.ACTIVITY:{
				return getGenTopNByProvinceAndcity(positionIdTyp,activityHotModelMaterIds,activityTopN,activityModelMaterials,province,city);
			}
			case Constant.HUIMAICHE:{
				return getHuiMaiCheGenTopNByProvinceAndcity(positionIdTyp,huimaicheModelMaterials,huimaicheTopN,province,city);
			}
			case Constant.HUIMAICHE_FIX:{
				return getGenTopNByProvinceAndcity(positionIdTyp,hmc_mmds_one_ids,hmc_mmds_one_topN,hmc_mmds_one_list,province,city);
			}
			case Constant.USED_CAR:{
				return getGenTopNByProvinceAndcity(positionIdTyp,usedCarModelMaterKpiIds,usedCarTopN,usedCarModelMaterials,province,city);
			}
			case Constant.YIXIN:{
				return getGenTopNByProvinceAndcity(positionIdTyp,yixinDynModelMaterKpiIds,yixinDynTopN,yixinDynModelMaterials,province,city);
			}
			case Constant.YIXIN_DYN:{
				return getGenTopNByProvinceAndcity(positionIdTyp,yixinDynModelMaterKpiIds,yixinDynTopN,yixinDynModelMaterials,province,city);
			}
			case Constant.YICHEHUI_DYN:{
				return getGenTopNByProvinceAndcity(positionIdTyp,yichehuiDynModelMaterKpiIds,yichehuiDynTopN,yichehuiDynModelMaterials,province,city);
			}
			case Constant.HUIMAICHE_DYN_ONE:{
				return getGenTopNByProvinceAndcity(positionIdTyp,hmc_mmds_one_ids,hmc_mmds_one_topN,hmc_mmds_one_list,province,city);
			}
			/*case Constant.HUIMAICHE_DYN_TWO:{
				return getGenTopNByProvinceAndcity(positionIdTyp,hmc_mmds_two_ids,hmc_mmds_two_topN,hmc_mmds_two_list,province,city);
			}*/
			case Constant.HUIMAICHE_DYN_FOUR:{
				return getGenTopNByProvinceAndcity(positionIdTyp,hmc_mmds_four_ids,hmc_mmds_four_topN,hmc_mmds_four_list,province,city);
			}
			default:{
				logger.warn("Illegal positionIdTyp:" + positionIdTyp);
				return new ArrayList<>(1);
			}
		}
	}

	@Override
	public Collection<ModelMaterial> getTopNByModelsProvinceAndcity(Set<Integer> keySet, int province, int city, int positionIdTyp) {
		switch(positionIdTyp){
			case Constant.GROUP_ON:{
				return getGenTopNByModelsProvinceAndcity(positionIdTyp,grouponHotModelMaterIds,grouponTopN,grouponModelMaterials,keySet, province, city);
			}
			case Constant.ACTIVITY:{
				return getGenTopNByModelsProvinceAndcity(positionIdTyp,activityHotModelMaterIds,activityTopN,activityModelMaterials,keySet, province, city);
			}
			case Constant.HUIMAICHE:{
				return getHuiMaiCheGenTopNByModelsProvinceAndcity(positionIdTyp,huimaicheModelMaterials,huimaicheTopN,keySet, province, city);
			}
			case Constant.HUIMAICHE_FIX:{
				return getGenTopNByModelsProvinceAndcity(positionIdTyp,hmc_mmds_one_ids,hmc_mmds_one_topN,hmc_mmds_one_list,keySet, province, city);
			}
			case Constant.USED_CAR:{
				return getGenTopNByModelsProvinceAndcity(positionIdTyp,usedCarModelMaterKpiIds,usedCarTopN,usedCarModelMaterials,keySet, province, city);
			}
			case Constant.YIXIN:{
				return getGenTopNByModelsProvinceAndcity(positionIdTyp,yixinDynModelMaterKpiIds,yixinDynTopN,yixinDynModelMaterials,keySet, province, city);
			}
			case Constant.YIXIN_DYN:{
				return getGenTopNByModelsProvinceAndcity(positionIdTyp,yixinDynModelMaterKpiIds,yixinDynTopN,yixinDynModelMaterials,keySet, province, city);
			}
			case Constant.YICHEHUI_DYN:{
				return getGenTopNByModelsProvinceAndcity(positionIdTyp,yichehuiDynModelMaterKpiIds,yichehuiDynTopN,yichehuiDynModelMaterials,keySet, province, city);
			}
			case Constant.HUIMAICHE_DYN_ONE:{
				return getGenTopNByModelsProvinceAndcity(positionIdTyp,hmc_mmds_one_ids,hmc_mmds_one_topN,hmc_mmds_one_list,keySet, province, city);
			}
			/*case Constant.HUIMAICHE_DYN_TWO:{
				return getGenTopNByModelsProvinceAndcity(positionIdTyp,hmc_mmds_two_ids,hmc_mmds_two_topN,hmc_mmds_two_list,keySet, province, city);
			}*/
			case Constant.HUIMAICHE_DYN_FOUR:{
				return getGenTopNByModelsProvinceAndcity(positionIdTyp,hmc_mmds_four_ids,hmc_mmds_four_topN,hmc_mmds_four_list,keySet, province, city);
			}
			default:{
				logger.warn("Illegal positionIdTyp:" + positionIdTyp);
				return new ArrayList<>(1);
			}
		}
	}
	
	private Collection<ModelMaterial> getGenTopNByProvinceAndcity(int type,Collection<Integer> modelMaterIds,Collection<ModelMaterial> topN,Collection<ModelMaterial> modelMaterials,int province,int city) {
		// 1.符合地域的热门车型+2.符合地域的普通车型+3.还不够用热门车型补充（不管地域限制）
		Map<Integer, ModelMaterial> map = getMaterialByProvinceOrcity(type,topN,province, city);
		if((map==null) || (map.size()==0)){
			return getTop9(type);
		} else {
			Map<Integer, ModelMaterial> topX = new LinkedHashMap<>();
			for(int modelId : modelMaterIds){
				if(topX.size() >= Constant.ONE_DELIVERY_NUM){
					break;
				}
				if(map.containsKey(modelId)){
					ModelMaterial sourceMaterial = map.get(modelId);
					ModelMaterial material = new ModelMaterial();
					// 不能修改sourceMaterial的内容，复制一个新的material.
					try {
						BeanUtils.copyProperties(material, sourceMaterial);
					} catch (IllegalAccessException | InvocationTargetException e) {
						logger.warn("Copy ModelMaterial failed! sourceMaterial:" + sourceMaterial);
						continue;
					}
					topX.put(modelId, material);
				}
			}
			
			Map<Integer, ModelMaterial> materialByProvinceOrcity = getMaterialByProvinceOrcity(type,modelMaterials,province, city);
			
			//2.符合地域的普通车型+
			if(topX.size() < Constant.ONE_DELIVERY_NUM){
				Set<Integer> keys = materialByProvinceOrcity.keySet();
				for(Integer id:keys){
					if(topX.size() >= Constant.ONE_DELIVERY_NUM){
						break;
					}
					if(!topX.containsKey(id)){
						topX.put(id, materialByProvinceOrcity.get(id));
					}
				}
			}
			
			// 少于9个素材用top9中的素材补全，同时不能有重复的车型。
			if(topX.size() < Constant.ONE_DELIVERY_NUM){
				for(ModelMaterial topModel : topN){
					if(!topX.containsKey(topModel.getModelId())){
						topX.put(topModel.getModelId(), topModel);
					}
					if(topX.size() >= Constant.ONE_DELIVERY_NUM){
						break;
					}
				}
			}
			return topX.values();
		}
	}
	
	
	private Collection<ModelMaterial> getHuiMaiCheGenTopNByProvinceAndcity(int type,Collection<ModelMaterial> all,Collection<ModelMaterial> huimaicheBJTopN,int province,int city) {
		//TODO 惠买车固定位投放
		// 1.符合地域的热门车型+2.符合地域的普通车型+3.还不够用热门车型补充（不管地域限制）
		Map<Integer, ModelMaterial> map = getMaterialByProvinceOrcity(type,all,province, city);
		Map<Integer, ModelMaterial> topX = new LinkedHashMap<>();//存放9个车型物料
		// 2.方法进来，先获取该城市所归属城市的车型集。
		Map<Integer, ModelMaterial> rewriteMap =null;
		CityMapping cityMap = cityMapping.get(city);
		if(cityMap != null){
			Integer rewriteCityId = cityMap.getRewriteCityId();
			if(rewriteCityId != null && rewriteCityId !=0){
				rewriteMap = getMaterialByProvinceOrcity(type,all,province, rewriteCityId);
			}
		}
		
		if((map==null) || (map.size()==0)){
			if(rewriteMap == null || rewriteMap.size() == 0){
				return getTop9(type);
			}
			
			rewriteMap.forEach(new BiConsumer<Integer, ModelMaterial>() {
				@Override
				public void accept(Integer t, ModelMaterial u) {
					if(topX.size() >= Constant.ONE_DELIVERY_NUM) return;
					if(!topX.containsKey(t)){
						topX.put(t, u);
					}
				}
			});
			
			//topx中不够9个，继续从全部北京里取
			if(topX.size() < Constant.ONE_DELIVERY_NUM){
				for(ModelMaterial mo:huimaicheBJTopN){
					if(topX.size() >= Constant.ONE_DELIVERY_NUM){
						break;
					}
					if(!topX.containsKey(mo.getModelId())){
						topX.put(mo.getModelId(), mo);
					}
				}
			}
			return topX.values();
		} else {
				map.forEach(new BiConsumer<Integer, ModelMaterial>() {
					@Override
					public void accept(Integer t, ModelMaterial u) {
						if(topX.size() >= Constant.ONE_DELIVERY_NUM) return;
						if(!topX.containsKey(t)){
							topX.put(t, u);
						}
					}
				});
				
				if(topX.size()<Constant.ONE_DELIVERY_NUM){
					rewriteMap.forEach(new BiConsumer<Integer, ModelMaterial>() {
						@Override
						public void accept(Integer t, ModelMaterial u) {
							if(topX.size() >= Constant.ONE_DELIVERY_NUM) return;
							if(!topX.containsKey(t)){
								topX.put(t, u);
							}
						}
					});
					
					//topx中不够9个，继续从全部北京里取
					if(topX.size() < Constant.ONE_DELIVERY_NUM){
						for(ModelMaterial mo:huimaicheBJTopN){
							if(topX.size() >= Constant.ONE_DELIVERY_NUM){
								break;
							}
							if(!topX.containsKey(mo.getModelId())){
								topX.put(mo.getModelId(), mo);
							}
						}
					}
					
					return topX.values();
				}
				return topX.values();
			}
	}
	
	
	private Collection<ModelMaterial> getGenTopNByModelsProvinceAndcity(int type,Collection<Integer> modelMaterIds,Collection<ModelMaterial> topN,Collection<ModelMaterial> modelMaterials,Set<Integer> models, int province, int city) {
		// 没有兴趣返回地域mallTop9.
		if ((models == null) || (models.size() == 0)) {
			logger.warn("models is empty!");
			return getGenTopNByProvinceAndcity(type,modelMaterIds,topN,modelMaterials,province, city);
		}
		// 符合地域的普通车型
		Map<Integer, ModelMaterial> map = getMaterialByProvinceOrcity(type,modelMaterials,province, city);
		// 符合地域的热门车型
		Map<Integer, ModelMaterial> topNByProvinceAndcity = getMaterialByProvinceOrcity(type,topN,province, city);
		Collection<ModelMaterial> result = null;
		if((map==null) || (map.size()==0)){
			if((topNByProvinceAndcity==null) || (topNByProvinceAndcity.size()==0)){
				logger.warn("topNByProvinceAndcity map is empty");
				return getTop9(type);
			}else {
				// 0.符合地域和兴趣的热门车型+1.符合地域的热门车型+2.还不够用热门车型补充（不管地域限制）
				result = getGenTopNBySomeModelsAndTopN(type,models,topNByProvinceAndcity,topNByProvinceAndcity.values());
			}
		} else {
			// 0.符合地域和兴趣的普通车型+1.符合地域的热门车型+2.符合地域的普通车型+3.还不够用热门车型补充（不管地域限制）
			result = getGenTopNBySomeModelsAndTopN(type,models,map,topNByProvinceAndcity.values());
		}
		Map<Integer, ModelMaterial> topX = new LinkedHashMap<>();
		if(result != null){
			for(ModelMaterial mm : result){
				topX.put(mm.getModelId(), mm);
			}
		}
		// 3.还不够用热门车型补充（不管地域限制）
		if (topX.size() < Constant.ONE_DELIVERY_NUM) {
			for (ModelMaterial topModel : topN) {
				if (!topX.containsKey(topModel.getModelId())) {
					topX.put(topModel.getModelId(), topModel);
				}
				if (topX.size() >= Constant.ONE_DELIVERY_NUM) {
					break;
				}
			}
		}
		return topX.values();
	}
	
	/**
	 * @description:cookie，地区:兴趣:符合(惠买车)
	 * @param type
	 * @param modelMaterIds
	 * @param topN
	 * @param modelMaterials
	 * @param models
	 * @param province
	 * @param city
	 * @return
	 */
	private Collection<ModelMaterial> getHuiMaiCheGenTopNByModelsProvinceAndcity(int type,Collection<ModelMaterial> huimaicheModelMaterials,Collection<ModelMaterial> huimaicheTopN,Set<Integer> models, int province, int city) {
		Collection<ModelMaterial> result = null;
		Map<Integer, ModelMaterial> map = getMaterialByProvinceOrcity(type,huimaicheModelMaterials,province, city);
		// 没有兴趣返回地域Top9.
		if ((models == null) || (models.size() == 0) || (map==null) || (map.size()==0)) {
			logger.warn("models is empty or map is null...");
			return getHuiMaiCheGenTopNByProvinceAndcity(type,huimaicheModelMaterials,huimaicheTopN,province,city);
		}
		// 符合地域的普通车型
		try{
			Integer cid = Integer.valueOf(city);
			CityMapping cityMap = cityMapping.get(cid);
			if(cityMap!=null){
				Integer rewriteCityId = cityMap.getRewriteCityId();
				if(rewriteCityId==null){
					return getTop9(type);
				}
				Map<Integer, ModelMaterial> rewriteMap = getMaterialByProvinceOrcity(type,huimaicheModelMaterials,province, rewriteCityId);
				if(rewriteMap == null || rewriteMap.size() <= 0){
					return getTop9(type);
				}
				result = getGenTopNBySomeModelsAndTopN(type,models,map,rewriteMap.values());
			}
		}catch(Exception e){
			logger.error("parse cityId error",e);
			return getTop9(type);
		}
		
		Map<Integer, ModelMaterial> topX = new LinkedHashMap<>();
		if(result != null){
			for(ModelMaterial mm : result){
				topX.put(mm.getModelId(), mm);
			}
		}
		// 3.还不够用热门车型补充（不管地域限制）
		if (topX.size() < Constant.ONE_DELIVERY_NUM) {
			for (ModelMaterial topModel : huimaicheTopN) {
				if (!topX.containsKey(topModel.getModelId())) {
					topX.put(topModel.getModelId(), topModel);
				}
				if (topX.size() >= Constant.ONE_DELIVERY_NUM) {
					break;
				}
			}
		}
		return topX.values();
	}
	
	
	private Map<Integer, ModelMaterial> getMaterialByProvinceOrcity(int type, Collection<ModelMaterial> allModelMaterials,int province,int city) {
		Map<Integer, ModelMaterial> result = new LinkedHashMap<Integer, ModelMaterial>(100);
		Iterator<ModelMaterial> iter = allModelMaterials.iterator();
		while(iter.hasNext()){
			ModelMaterial material = iter.next();
			Integer region = material.getRegion();
			List<String> origCityIds = material.getOrigCityIds();
			List<Integer> cityIds = material.getCityIds();
			if(type == Constant.YIXIN_DYN || type == Constant.YIXIN || type== Constant.YICHEHUI_DYN){
				if(origCityIds != null && origCityIds.size() >0){
					if (origCityIds.contains(String.valueOf(0)) 
							|| origCityIds.contains(String.valueOf(province))
							||origCityIds.contains(String.valueOf(city)) 
							|| (origCityIds.contains(String.valueOf(0))&&origCityIds.contains(String.valueOf(province*10)))
							|| (province==0 && city ==0)
							) {
						result.put(material.getModelId(), material);
					}
				}
			}if(type == Constant.HUIMAICHE_FIX || type== Constant.HUIMAICHE_DYN_ONE || type== Constant.HUIMAICHE_DYN_TWO || type== Constant.HUIMAICHE_DYN_FOUR){
				if(cityIds != null && cityIds.size() >0){
					if (cityIds.contains(0) 
							||cityIds.contains(province)
							||cityIds.contains(city) 
							||(cityIds.contains(0)&&cityIds.contains(province*10))
							||(province==0 && city ==0)) {
						result.put(material.getModelId(), material);
					}
				}
			}else{
				if(region != null){
					// 0=全国
					if (region==0 || region==province|| region==city || (city==0&&region==province*10)) {
						result.put(material.getModelId(), material);
					}
				} 
			}
		}
		return result;
	}
	
	private Collection<ModelMaterial> getGenTopNBySomeModelsAndTopN(int type,Set<Integer> models,Map<Integer, ModelMaterial> map,Collection<ModelMaterial> topN) {
		// 没有兴趣也没有地域直接返回Top9
		if(models == null || models.size() == 0){
			logger.warn("getGenTopNBySomeModels models is empty!");
			return getTop9(type);
		}
		if (map == null || map.size() == 0) {
			logger.warn("getGenTopNBySomeModels map is empty");
			return getTop9(type);
		}
		Map<Integer, ModelMaterial> topX = getGenTopNBySomeModels(type, models, map);
		// 1.符合地域的热门车型
		if (topX.size() < Constant.ONE_DELIVERY_NUM) {
			for (ModelMaterial topModel : topN) {
				if (!topX.containsKey(topModel.getModelId())) {
					topX.put(topModel.getModelId(), topModel);
				}
				if (topX.size() >= Constant.ONE_DELIVERY_NUM) {
					break;
				}
			}
		}
		// 2.符合地域的普通车型
		if (topX.size() < Constant.ONE_DELIVERY_NUM) {
			for (ModelMaterial modelMaterial : map.values()) {
				if (!topX.containsKey(modelMaterial.getModelId())) {
					topX.put(modelMaterial.getModelId(), modelMaterial);
				}
				if (topX.size() >= Constant.ONE_DELIVERY_NUM) {
					break;
				}
			}
		}
		return topX.values();
	}
	
	private Map<Integer, ModelMaterial> getGenTopNBySomeModels(int type,
			Set<Integer> models, Map<Integer, ModelMaterial> map) {
		Map<Integer, ModelMaterial> topX = new LinkedHashMap<>();
		// 匹配上的车型
		for (int modelId : models) {
			// 一个兴趣车型+两个竞品车型，否则放两个竞品。
			int cmCount = 2;
			if ((map.containsKey(modelId)) && (!topX.containsKey(modelId))) {
				ModelMaterial sourceMaterial = map.get(modelId);
				ModelMaterial material = new ModelMaterial();
				// 不能修改sourceMaterial的内容，复制一个新的material.
				try {
					BeanUtils.copyProperties(material, sourceMaterial);
				} catch (IllegalAccessException | InvocationTargetException e) {
					logger.warn("Copy ModelMaterial failed! sourceMaterial:"
							+ sourceMaterial);
					continue;
				}

				Collection<Integer> matchedTags = material.getMatchedTags();
				if (matchedTags == null) {
					matchedTags = new ArrayList<>();
					material.setMatchedTags(matchedTags);
				}
				matchedTags.add(modelId);
				topX.put(modelId, material);
			}
			if (competitiveModels.containsKey(modelId)) {
				// 竞品车型list
				List<Integer> cms = competitiveModels.get(modelId);
				if (cms != null) {
					int cmNum = 0;
					for (int cm : cms) {
						// 每个有兴趣的车型最多只取两个竞品车型。
						if ((cmNum >= cmCount)
								|| (topX.size() >= Constant.ONE_DELIVERY_NUM)) {
							break;
						}
						if ((map.containsKey(cm)) && (!topX.containsKey(cm))) {
							ModelMaterial sourceMaterial = map.get(cm);
							ModelMaterial material = new ModelMaterial();
							// 不能修改sourceMaterial的内容，复制一个新的material.
							try {
								BeanUtils.copyProperties(material,
										sourceMaterial);
							} catch (IllegalAccessException
									| InvocationTargetException e) {
								logger.warn("Copy ModelMaterial failed! sourceMaterial:"
										+ sourceMaterial);
								continue;
							}

							topX.put(cm, material);
							cmNum++;
							Collection<Integer> matchedCompetitiveModels = material
									.getMatchedCompetitiveModels();
							if (matchedCompetitiveModels == null) {
								matchedCompetitiveModels = new ArrayList<>();
								material.setMatchedCompetitiveModels(matchedCompetitiveModels);
							}
							matchedCompetitiveModels.add(cm);
						}
					}
				}
			}
			if (topX.size() >= Constant.ONE_DELIVERY_NUM) {
				break;
			}
		}
		return topX;
	}
	
	/**
	 * 初始化惠买车接口
	 */
	public void initHuiMaiCheTask(){
		try{
			List<ModelMaterial> huimaiches = dao.getHuimaicheModelMaterialMap();
			List<ModelMaterial> tempAllHuimaicheList=new ArrayList<>();
			Map<Integer, ModelMaterial> tempAllModelMaterials = new LinkedHashMap<Integer, ModelMaterial>();
			Map<Integer, ModelMaterial> tempKpiModelMaterials = new LinkedHashMap<Integer, ModelMaterial>();
			List<ModelMaterial> tempHuimaicheHot=new ArrayList<>();
			for(ModelMaterial mo:huimaiches){
				if(mo.getRegion()==201){
					if(!tempKpiModelMaterials.containsKey(mo.getModelId())){
						tempKpiModelMaterials.put(mo.getModelId(), mo);
						tempHuimaicheHot.add(mo);
					}
				}
				
				if(tempAllModelMaterials.containsKey(mo.getModelId())){
					tempAllModelMaterials.put(mo.getModelId(), mo);
				}
			}
			
			if (tempHuimaicheHot.size() == 0 || huimaiches.size() ==0) {
				logger.error("Huimaiche tempHuimaicheHot:"+tempHuimaicheHot.size()+" or tempAllHuimaicheList is:"+tempAllHuimaicheList.size());
			} else {
				huimaicheModelMaterials = huimaiches;//赋值给团购全物料
				huimaicheAllModelMaterials =tempAllModelMaterials;
				huimaicheTopN = tempHuimaicheHot;
				huimaicheTop9 = huimaicheTopN.subList(0,Constant.ONE_DELIVERY_NUM);
			}
		}catch (Exception e) {
			logger.error("ModelMaterialService scheduled job error!", e);
		}
	}
	
	private void initUsedCarData(){
		// 二手车数据定时器
		if (matchYipaiRegions == null) {
			throw new RuntimeException("Match yipai regions is null!");
		}
		if(whiteModelMaterialSizeMap == null || whiteModelMaterialSizeMap.size() ==0){
			logger.error("The whiteModelMaterialSizeMap is empty >>>>>>>>");
			return;
		}
		List<ModelMaterial> usedCarList = dao.getUsedCarList();
		if(usedCarList == null || usedCarList.size() == 0) {logger.error("usedcarTask invoke dao.getUsedCarModelMaterialList() is null!"); return;}
		Map<Integer, ModelMaterial> tempAllUsedCarModelMaterials=new LinkedHashMap<>();
		List<ModelMaterial> usedCarAllTemp = new ArrayList<>();//40000条存储
		List<Integer> usedCarTempIds=new ArrayList<>(1000);
		
		usedCarList.forEach(new Consumer<ModelMaterial>() {
			@Override
			public void accept(ModelMaterial t) {
				// TODO Auto-generated method stub
				Integer modelId = t.getModelId();
				
				ModelMaterial mm = whiteModelMaterialSizeMap.get(modelId);
				if(mm == null){
					return;
				}else{
					ModelMaterial modelMaterial = new ModelMaterial();
					try {
						BeanUtils.copyProperties(modelMaterial, t);
						if(mm.getName()!=null){
							modelMaterial.setName(t.getName());
						}
						if(mm.getPic210X140()!=null){
							modelMaterial.setPic210X140(mm.getPic210X140());
						}
					} catch (IllegalAccessException| InvocationTargetException e) {
						logger.error("Copy ModelMaterial failed! source ModelMaterial from mysql and mall. mysql:"+ mm + " mall:" + mm);
						return;
					}
					
					// modelId+regionId才能唯一标识一个素材。
					int cityId = modelMaterial.getRegion();
					int regionId = -1;
					if (matchYipaiRegions.containsKey(cityId)) {
						regionId = matchYipaiRegions.get(cityId);
					} else {
						logger.warn("Have not match yipai region! yipaiCityId:"+ cityId);
					}
					modelMaterial.setRegion(regionId);//设置地域ID
					
					Integer key=modelId+regionId;
					
					if(!tempAllUsedCarModelMaterials.containsKey(key)){
						tempAllUsedCarModelMaterials.put(key, modelMaterial);
						usedCarAllTemp.add(modelMaterial);
						usedCarTempIds.add(modelId);
					}
				}
			}
		});
		
		
		if (tempAllUsedCarModelMaterials.size() == 0 || tempAllUsedCarModelMaterials.size() ==0) {
			logger.error("usedCarAllTemp"+usedCarAllTemp.size()+" or usedCarAllTemp is:"+usedCarAllTemp.size());
		} else {
			usedCarModelMaterKpiIds=usedCarTempIds;
			usedCarModelMaterials = usedCarAllTemp;//赋值给团购全物料
			usedCarAllModelMaterials = tempAllUsedCarModelMaterials;
			usedCarTopN = usedCarAllTemp;
			usedCarTop9 = usedCarTopN.subList(0,Constant.ONE_DELIVERY_NUM);
		}
	}
	
	private void initYxData() {
		if(whiteModelMaterialSizeMap == null || whiteModelMaterialSizeMap.size() ==0){
			logger.error("The whiteModelMaterialSizeMap is empty >>>>>>>>");
			return;
		}
		Map<String, List<ModelMaterial>> precisionAdvertisingList = dao.getPrecisionAdvertisingList();
		List<ModelMaterial> kpi = precisionAdvertisingList.get("kpi");
		if(kpi.size() == 0 || kpi ==null){
			logger.error("yixinDynTask get kpi models is null___________");
			return;
		}
		Set<Integer> tempKpiIds=new LinkedHashSet<>();
		List<ModelMaterial> tempModels=new ArrayList<>();
		Map<Integer, ModelMaterial> tempKpiModelMaterials=new LinkedHashMap<>();
		kpi.forEach(new Consumer<ModelMaterial>() {
			@Override
			public void accept(ModelMaterial t) {
				// 循环处理易鑫热门车型
				Integer modelId = t.getModelId();
				if(!tempKpiModelMaterials.containsKey(modelId)){
					ModelMaterial modelMaterial = whiteModelMaterialSizeMap.get(modelId);
					if(modelMaterial == null){
						logger.info("invoke yxDyninTask ModelMaterial is null:"+modelId);
						return;
					}
					t.setName(modelMaterial.getName());
					t.setPic210X140(modelMaterial.getPic210X140());
					
					tempKpiModelMaterials.put(modelId, t);
					tempKpiIds.add(modelId);
					tempModels.add(t);
				}
			}
		});
		
		yixinDynModelMaterKpiIds=tempKpiIds;
		yixinDynTopN=tempModels;
		
		Map<Integer, ModelMaterial> top9=new LinkedHashMap<>();
		List<ModelMaterial> top9List=new ArrayList<>();
		
		tempModels.forEach(new Consumer<ModelMaterial>() {
			@Override
			public void accept(ModelMaterial t) {
				Integer id=t.getModelId();
				if(!top9.containsKey(id)){
					top9.put(id, t);
					top9List.add(t);
				}
			}
		});
		yixinDynTop9=top9List.subList(0, Constant.ONE_DELIVERY_NUM);
		
		List<ModelMaterial> all = precisionAdvertisingList.get("all");
		if(all.size() == 0 || all ==null){
			logger.error("yixinDynTask get all models is null_______>>");
			return;
		}
		
		Map<Integer, ModelMaterial> allModels = new LinkedHashMap<Integer, ModelMaterial>();
		List<ModelMaterial> models=new ArrayList<>();
		all.forEach(new Consumer<ModelMaterial>() {
			@Override
			public void accept(ModelMaterial t) {
				// 循环处理易鑫全部车型
				Integer modelId = t.getModelId();
					
				ModelMaterial modelMaterial = whiteModelMaterialSizeMap.get(modelId);
				if(modelMaterial == null){
					logger.info("invoke yxDyninTask ModelMaterial is null:"+modelId);
					return;
				}
				t.setName(modelMaterial.getName());
				t.setPic210X140(modelMaterial.getPic210X140());
				
				models.add(t);
				allModels.put(modelId, t);
			}
		});
		
		yixinDynModelMaterials=models;
		yixinDynAllModelMaterials=allModels;
	}
	
	/*获取竞品*/
	public Map<Integer, List<Integer>> getCompetitiveModels(){
		return competitiveModels;
	}
	/*单例*/
	private static class MmServiceHolder {
		private static ModelMaterialService mmService = new ModelMaterialServiceImpl();
	}
	
	public static ModelMaterialService getInstance() {
		return MmServiceHolder.mmService;
	}

	public Map<Integer, Integer> getMatchYipaiRegions() {
		return matchYipaiRegions;
	}
	
	public static Map<Integer, Integer> matchYipaiRegions(){
		return MmServiceHolder.mmService.getMatchYipaiRegions();
	}
	
	@Override
	public void add(Integer positionId,Integer deviceId) {
		// TODO 统计接口UV
		String minute=DateUtil.dateFormatText(DateEnum.FORMAT_DATE_2, new Date())+"_"+positionId+"_"+deviceId;
		if(map.containsKey(minute)){
			map.put(minute, map.get(minute)+1);
		}else{
			map.put(minute, 1);
		}
	}
}