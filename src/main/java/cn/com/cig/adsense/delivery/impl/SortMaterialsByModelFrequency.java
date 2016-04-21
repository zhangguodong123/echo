package cn.com.cig.adsense.delivery.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import cn.com.cig.adsense.delivery.FixtureAbstractHandler;
import cn.com.cig.adsense.delivery.FixtureHandler;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.vo.fix.FixtureDeliveryAttribute;
import cn.com.cig.adsense.vo.fix.Material;
import cn.com.cig.adsense.vo.fix.UserStruct;

public class SortMaterialsByModelFrequency extends FixtureAbstractHandler implements
		FixtureHandler {

	private static Logger logger = LoggerFactory
			.getLogger(SortMaterialsByModelFrequency.class);

	@Override
	public void operator(FixtureDeliveryAttribute da) {

		if ((da != null) && (da.getUserStruct() != null)
				&& (da.getMaterials() != null)) {

			// Material name 中包含&,说明是易车中间的八个广告位.
			List<Material> filtered = Lists.newArrayList(Iterables.filter(
					da.getMaterials(), new Predicate<Material>() {
						public boolean apply(Material m) {
							boolean result = (m.getName()
									.contains(Constant.BITAUTO_SPECIAL_POSITION_SEPARATOR));
							return result;
						}
					}));
			if((filtered!=null) && (filtered.size()>0)){
				da.setMaterials(filtered);
			}
			
			// 根据投放策略,从所有关联这个广告位的素材中找出优先级最高的返回.
			// 车型-频次 就是车型-权重
			UserStruct us = da.getUserStruct();
			Map<Integer, Integer> tags = new HashMap<>();
			if(us.getMtag() != null){
				tags.putAll(us.getMtag());
			}
			if(us.getOtag() != null){
				tags.putAll(us.getOtag());
			}
			Map<Integer, Integer> modelFrequency = Utils
					.calculateModelFrequency(tags);
			
			// 投放策略1.根据兴趣权重优先显示权重最大者；
			// 根据兴趣权重(频次)对素材进行排序；
			Map<String, Material> sortedMaterials = new LinkedHashMap<>();
			// 只关联了地域并且没有关联车型的素材。
			List<Material> onlyRegionMaterials = new ArrayList<>();
			if(modelFrequency != null){
				Iterator<Entry<Integer,Integer>> iter = modelFrequency.entrySet().iterator();
				//int maxFreq = 0;
				while (iter.hasNext()) {
					Entry<Integer,Integer> entry = iter.next();
					int modelId = entry.getKey();
					//int freq = entry.getValue();
					/*if(freq >= maxFreq){
						// 只投最高频次(包括等于)的车型
						maxFreq = freq;
					} else {
						// 其他的车型不投。
						continue;
					}*/
					Iterator<Material> iterM = da.getMaterials().iterator();
					while (iterM.hasNext()) {
						Material m = iterM.next();
						if((m.getModels()==null) || (m.getModels().size()==0)){
							onlyRegionMaterials.add(m);
						} else {
							if (m.getModels().contains(modelId)) {
								if(!sortedMaterials.containsKey(m.getId())){
									sortedMaterials.put(m.getId(), m);
									List<Integer> matchedTags = m.getMatchedTags();
									if(matchedTags == null){
										matchedTags = new ArrayList<>();
										m.setMatchedTags(matchedTags);
									}
									matchedTags.add(modelId);
								}
							}
						}
					}
				}
				// 如果关联了地域，又关联了车型，但是全被sortHandler筛掉(车型不匹配)了，
				// 再尝试找一个只关联了地域并且没有关联车型的素材(因为这种素材被sortHandler过滤掉了。)。
				// 找不到，投默认素材。
				if(sortedMaterials.size()==0){
					da.setMaterials(onlyRegionMaterials);
				} else {
					da.setMaterials(ImmutableList.copyOf(sortedMaterials.values()));
				}
			}

		} else {
			logger.warn("DeliveryAttribute is null! DeliveryAttribute:" + da);
		}
		if (getHandler() != null) {
			getHandler().operator(da);
		}
	}

}
