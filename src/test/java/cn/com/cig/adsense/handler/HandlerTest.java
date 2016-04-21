package cn.com.cig.adsense.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cn.com.cig.adsense.vo.fix.BitautoMaterial;

/**   
 * @File: HandlerTest.java 
 * @Package cn.com.cig.adsense.handler 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月24日 上午10:02:54 
 * @version V1.0   
 */
public class HandlerTest {
	public static void main(String[] args){
		
		Collection<BitautoMaterial> materials=new ArrayList<>();
		BitautoMaterial m1=new BitautoMaterial();
		m1.setId(1001);
		m1.setName("001");
		
		BitautoMaterial m2=new BitautoMaterial();
		m2.setId(1002);
		m2.setName("001");
		
		
		BitautoMaterial m3=new BitautoMaterial();
		m3.setId(1003);
		m3.setName("001");
		
		materials.add(m1);
		materials.add(m2);
		materials.add(m3);
		
		//===============================
		
		Collection<BitautoMaterial> cookieMaterials=new ArrayList<>();
		BitautoMaterial b3=new BitautoMaterial();
		b3.setId(1003);
		b3.setName("001");
		cookieMaterials.add(b3);
		
		Set<Integer> cookieId=new HashSet<>();
		Stream<BitautoMaterial> cookieStream=cookieMaterials.stream();
		cookieMaterials.stream().forEach((material)->{
			cookieId.add(material.getId());
		});cookieStream.close();
		
		System.out.println(cookieId);
		
		Stream<BitautoMaterial> stream = materials.stream();
		Collection<BitautoMaterial> filtered = stream
				.filter((material) -> {
					return !cookieId.contains(material.getId());
				}).collect(Collectors.toList());
		stream.close();
		
		System.out.println(filtered);
	}
}
