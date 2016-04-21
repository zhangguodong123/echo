package cn.com.cig.adsense.dao.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import cn.com.cig.adsense.vo.HttpMethodEnum;
import cn.com.cig.adsense.vo.dyn.Block;
import cn.com.cig.adsense.vo.dyn.Feed;
import cn.com.cig.adsense.vo.dyn.Product;
import cn.com.cig.adsense.vo.fix.ModelMaterial;
import static cn.com.cig.adsense.utils.http.HttpUtils.httpClient;

/**   
 * @File: ModelMaterialDaoImplTest_Unit_Two.java 
 * @Package cn.com.cig.adsense.dao.impl 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年2月1日 上午9:52:42 
 * @version V1.0   
 */
public class ModelMaterialDaoImplTest_Unit_Two {
	private static Logger logger = LoggerFactory.getLogger(ModelMaterialDaoImplTest_Unit_Two.class);
	private static Gson gson = new Gson();
	
	public static void main(String[] args) {
		getHuiMaiCheProduct();
	}
	
	public static Map<Integer,List<ModelMaterial>> getHuiMaiCheProduct() {
		File carsFile1 = new File("d://惠买车商品源1.csv");
		if(carsFile1.exists()){
			 carsFile1.deleteOnExit();
		}
		StringBuilder sbd1 = new StringBuilder();
		sbd1.append("车型ID,");
		sbd1.append("车系名称,");
		sbd1.append("车型名称,");
		sbd1.append("车型图片,");
		sbd1.append("PC落地页,");
		sbd1.append("移动端落地页,");
		sbd1.append("优惠价,");
		sbd1.append("基本价,");
		sbd1.append("广告语,");
		sbd1.append("地域ID,");
		sbd1.append("地域名称,");
		sbd1.append("\n");
		
		
		Map<Integer,List<ModelMaterial>> result=new HashMap<>();
		try{
			String url="http://img0.ctags.net/pmpc/v1.0/products/5b188a338566331cb364dbf546a7904f/product_cfb94baf3ffa4e330abbc5b20d5fb09e.ptd";
			String content = httpClient(url,HttpMethodEnum.GET,null);
			if(content == null || "".equals(content)){
				logger.info("Request "+url+" is faild,request content is null ************");
				return null;
			}
			Product prob = gson.fromJson(content, Product.class);
			boolean success = prob.isSuccess();
			if(success == false){
				logger.info("Request "+url+" is faild,request status is false ************");
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
						System.out.println("feed:"+t.getFeedid()+" size:"+t.getData().size());
						List<ModelMaterial> temp=new ArrayList<>();
						data.forEach(new Consumer<Block>() {
							@Override
							public void accept(Block t) {
								if(feedid == 1){
									Integer csId = t.getCsId();
									String brandName = t.getBrandName();
									String csName = t.getCsName();
									String img = t.getImg();
									String adLink = t.getAdLink();
									String getmLink = t.getmLink();
									double referPrice = t.getReferPrice();
									double price = t.getPrice();
									String title = t.getCustom().get("title");
									String ttle = title.replaceAll(",", "");
									List<Integer> regionId = t.getRegionId();
									List<String> regionName = t.getRegionName();
									StringBuilder regions=new StringBuilder();
									StringBuilder regionNames=new StringBuilder();
									regionId.forEach(new Consumer<Integer>() {
										@Override
										public void accept(Integer t) {
											regions.append(t);
											regions.append("、");
										}
									});
									
									regionName.forEach(new Consumer<String>() {
										@Override
										public void accept(String t) {
											regionNames.append(t);
											regionNames.append("、");
										}
									});
									
									
									sbd1.append(csId);
									sbd1.append(",");
									sbd1.append(brandName);
									sbd1.append(",");
									sbd1.append(csName);
									sbd1.append(",");
									sbd1.append(img);
									sbd1.append(",");
									sbd1.append(adLink);
									sbd1.append(",");
									sbd1.append(getmLink);
									sbd1.append(",");
									sbd1.append(referPrice);
									sbd1.append(",");
									sbd1.append(price);
									sbd1.append(",");
									sbd1.append(ttle);
									sbd1.append(",");
									sbd1.append(regions.toString());
									sbd1.append(",");
									sbd1.append(regionNames.toString());
									sbd1.append("\n");
								}
								
								
							}
						});
						result.put(feedid, temp);
					}
				});
			}
			
			//Files.write(carsFile1.toPath(), sbd1.toString().getBytes("GBK"));
		}catch(Exception e){
			logger.error("invoke getHuiMaiCheProduct is error....", e);
		}
		return result;
	}

}
