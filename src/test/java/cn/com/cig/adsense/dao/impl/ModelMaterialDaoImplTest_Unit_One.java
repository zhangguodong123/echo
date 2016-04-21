package cn.com.cig.adsense.dao.impl;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cn.com.cig.adsense.utils.date.DateUtil;
import cn.com.cig.adsense.vo.DateEnum;
import cn.com.cig.adsense.vo.dyn.Block;
import cn.com.cig.adsense.vo.dyn.Feed;
import cn.com.cig.adsense.vo.dyn.Product;
import cn.com.cig.adsense.vo.fix.ModelMaterial;

/**   
 * @File: ModelMaterialDaoImplTest_Unit_One.java 
 * @Package cn.com.cig.adsense.dao.impl 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年1月27日 下午2:20:03 
 * @version V1.0   
 */
public class ModelMaterialDaoImplTest_Unit_One {
	private static Logger logger = LoggerFactory.getLogger(ModelMaterialDaoImplTest_Unit_One.class);
	private static Gson gson = new Gson();
	
	public static Map<Integer,List<ModelMaterial>> getHuiMaiCheProduct() {
		Map<Integer,List<ModelMaterial>> result=new HashMap<>();
		String content = null;
		try {
			content = FileUtils.readFileToString(new File("E://data.json"),"utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File carsFile1 = new File("d://惠买车商品源1.csv");
		if(carsFile1.exists()){
			 carsFile1.deleteOnExit();
		 }
		
		
		File carsFile2 = new File("d://惠买车商品源2.csv");
		if(carsFile2.exists()){
			 carsFile2.deleteOnExit();
		 }
		
		
		StringBuilder sb1 = new StringBuilder();
		sb1.append("车型ID,");
		sb1.append("车型名称,");
		sb1.append("优惠价,");
		sb1.append("基本价,");
		sb1.append("广告语,");
		sb1.append("地域ID,");
		sb1.append("地域名称");
		sb1.append(",\n");
		
		
		
		StringBuilder sb2 = new StringBuilder();
		sb2.append("cid");
		sb2.append(",");
		sb2.append("name");
		sb2.append(",");
		sb2.append("cityId");
		sb2.append(",\n");
		
		
		//String product = httpClient(Constant.HUIMAICHE_PRODUCT_INF,EHttpMethod.GET,null);
		/*if(product == null  || "".equals(product)){
			logger.error("The "+Constant.HUIMAICHE_PRODUCT_INF+" request is error*********");
		}*/
		//PmpcStatus productObj = gson.fromJson(product, PmpcStatus.class);
		//if(productObj!=null && productObj.getRep_status().getIs_generated() ==3){
			//String download_url = productObj.getProduct_obj().getDownload_url();
			//String content = httpClient(download_url,EHttpMethod.GET,null);
			if(content == null || content.equals("")){
				//logger.info("Request "+download_url+" is faild,value is null ************");
			}
			Product prob = gson.fromJson(content, Product.class);
			boolean success = prob.isSuccess();
			if(success == false){
				//logger.info("Request "+download_url+" is faild,request status is false ************");
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
		//}
		
		/*try {
			Files.write(carsFile1.toPath(), sb.toString().getBytes("GBK"));
			Files.write(carsFile2.toPath(), sb.toString().getBytes("GBK"));
		} catch (IOException e) {
			e.printStackTrace();
		}*/
			
		
		return result;
	}
	
	public static void main(String[] args){
		Map<Integer, List<ModelMaterial>> huiMaiCheProduct = getHuiMaiCheProduct();
		System.out.println(huiMaiCheProduct.get(new Integer(1)));
		System.out.println(huiMaiCheProduct.get(new Integer(2)));
	}
	
	
	
	 public static void text(String text){
		  try {
		   FileWriter fw=new FileWriter("d://json.txt");
		   fw.write(text);
		   fw.flush();
		   fw.close();
		  } catch (IOException e) {
		   e.printStackTrace();
		  }
	 }
	
}
