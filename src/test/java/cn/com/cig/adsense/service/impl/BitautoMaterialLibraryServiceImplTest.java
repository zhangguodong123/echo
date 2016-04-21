package cn.com.cig.adsense.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.MaterialType;

/**
 * @File: BitautoMaterialLibraryServiceImpl.java
 * @Package cn.com.cig.adsense.service.impl
 * @Description: TODO
 * @author zhangguodong
 * @date 2015年9月16日 下午3:12:20
 * @version V1.0
 */
public class BitautoMaterialLibraryServiceImplTest {
	private static Logger logger = LoggerFactory.getLogger(BitautoMaterialLibraryServiceImplTest.class);

	@Test
	public void sortedByCTRTest() {
		BitautoMaterial one = new BitautoMaterial();
		one.setMatType(MaterialType.IMAGE);
		one.setWidth(750);
		one.setHeight(140);
		one.setId(126268);
		BitautoMaterial two = new BitautoMaterial();
		two.setMatType(MaterialType.IMAGE);
		two.setWidth(740);
		two.setHeight(80);
		two.setId(100000);
		BitautoMaterial three = new BitautoMaterial();
		three.setWidth(720);
		three.setHeight(100);
		three.setMatType(MaterialType.IMAGE);
		three.setId(131020);
		List<BitautoMaterial> list = new ArrayList<BitautoMaterial>();
		list.add(one);
		list.add(two);
		list.add(three);

		Collection<BitautoMaterial> sortedByCTR = sortedByCTR(list, 1000,
				130000, 130100, 4600);
		sortedByCTR.forEach(new Consumer<BitautoMaterial>() {
			@Override
			public void accept(BitautoMaterial t) {
				// TODO Auto-generated method stub
				System.out.println(t);
			}
		});
	}

	public Collection<BitautoMaterial> sortedByCTR(Collection<BitautoMaterial> bitautoMaterials, Integer pid,
			Integer provId, Integer cityId, Integer urlReffererTag) {
		// TODO update code
		if(bitautoMaterials == null || bitautoMaterials.size() == 0 || pid == null ){
			logger.error("bitautoMaterials is:"+bitautoMaterials+" or pid is:"+pid);
			return null;
		}
		StringBuilder materials=new StringBuilder();
		Map<Integer,BitautoMaterial> bitautoMap=new HashMap<Integer,BitautoMaterial>();
		Iterator<BitautoMaterial> it = bitautoMaterials.iterator();
		while(it.hasNext()){
			BitautoMaterial bitauto = it.next();
			String materIds=String.format("%s_%s_%s_%s", bitauto.getId(),bitauto.getMatType().getIndex(),bitauto.getWidth(),bitauto.getHeight());
			materials.append(materIds);
			materials.append(",");
			bitautoMap.put(bitauto.getId(), bitauto);
		}
		materials.deleteCharAt(materials.length()-1);
		String urlFormat="place=%s&province=%s&city=%s&materials=%s&modelid=%s";
		String params=String.format(urlFormat, pid,provId,cityId,materials,urlReffererTag);
		String url = String.format(Constant.CTR_URL_FORMAT,"echotest.adsense.cig.com.cn",Constant.CTR_URL_PORT,Constant.CTR_URL_PRIFIX,params);
		
		String sortByCtrMaterials = getCTRMaterials(url);
		if(sortByCtrMaterials == null || "".equals(sortByCtrMaterials) || sortByCtrMaterials.length()>3000000){
			logger.error("invoke url response is error:>>"+url+" reponseBody_size:"+sortByCtrMaterials.length());
			return null;
		}
		Collection<BitautoMaterial> result=new ArrayList<>();
		JSONArray data=new JSONArray(sortByCtrMaterials);
		
		for(int i=0;i<data.length();i++){
			JSONObject obj = data.getJSONObject(i);
			int mid = obj.getInt("materialId");
			BitautoMaterial bitautoMaterial = bitautoMap.get(mid);
			result.add(bitautoMaterial);
		}
		return result;
	}

	public String getCTRMaterials(String url) {
		return "";
	}

}
