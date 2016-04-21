package cn.com.cig.adsense.delivery.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.delivery.BitautoFixtureAbstractHandler;
import cn.com.cig.adsense.delivery.BitautoFixtureHandler;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.vo.fix.BitautoFixtureDeliveryAttribute;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;

public class SortMaterialsByModelFrequencyForBitauto extends
		BitautoFixtureAbstractHandler implements BitautoFixtureHandler {

	private static Logger logger = LoggerFactory
			.getLogger(SortMaterialsByModelFrequencyForBitauto.class);

	@Override
	public void operator(BitautoFixtureDeliveryAttribute da) {

		if ((da != null) && (da.getTags() != null)
				&& (da.getMaterials() != null)) {

			// Material name 中包含&,说明是易车中间的八个广告位.
			Stream<BitautoMaterial> stream = da.getMaterials().stream();
			List<BitautoMaterial> filtered = stream.filter(
					(material) -> {
						return material.getName().contains(
								Constant.BITAUTO_SPECIAL_POSITION_SEPARATOR);
					}).collect(Collectors.toList());
			stream.close();
			if ((filtered != null) && (filtered.size() > 0)) {
				da.setMaterials(filtered);
			}

			// 根据投放策略,从所有关联这个广告位的素材中找出优先级最高的返回.
			// 车型-频次 就是车型-权重
			Map<Integer, Integer> modelFrequency = Utils.calculateModelFrequency(da.getTags());

			// 投放策略1.根据兴趣权重优先显示权重最大者；
			// 根据兴趣权重(频次)对素材进行排序；
			Map<String, BitautoMaterial> sortedMaterials = new LinkedHashMap<>();
			// 只关联了地域并且没有关联车型的素材。
			// Map<Integer, BitautoMaterial> onlyRegionMaterials = new
			// LinkedHashMap<>();
			if (modelFrequency != null) {
				Iterator<Entry<Integer, Integer>> iter = modelFrequency.entrySet().iterator();
				// int maxFreq = 0;
				while (iter.hasNext()) {
					Entry<Integer, Integer> entry = iter.next();
					int modelId = entry.getKey();
					// int freq = entry.getValue();
					// 只投最高频次(包括等于)的车型
					// if(freq < maxFreq){
					// continue;
					// }
					Iterator<BitautoMaterial> iterM = da.getMaterials().iterator();
					while (iterM.hasNext()) {
						BitautoMaterial m = iterM.next();
						Collection<Integer> models = m.getModels();
						String key = m.getCampaignId() + "_" + m.getAudienceId() + "_" + m.getId();
						// 本品匹配
						if (models!=null && models.contains(modelId)) {
							if (!sortedMaterials.containsKey(key)) {
								BitautoMaterial copiedMaterial = Utils
										.bitautoMaterialCopy(m);
								sortedMaterials.put(key, copiedMaterial);
								// maxFreq = freq;
								Collection<Integer> matchedTags = copiedMaterial
										.getMatchedTags();
								if (matchedTags == null) {
									matchedTags = new ArrayList<>();
									copiedMaterial.setMatchedTags(matchedTags);
								}
								matchedTags.add(modelId);
							}
						} else {
							// 本品不匹配，才使用竞品就行匹配。
							Collection<Integer> competitiveModels = m.getCompetitiveModels();
							if ((competitiveModels != null) && (competitiveModels.contains(modelId))) {
								if (!sortedMaterials.containsKey(key)) {
									BitautoMaterial copiedMaterial = Utils.bitautoMaterialCopy(m);
									sortedMaterials.put(key, copiedMaterial);
									// maxFreq = freq;
									Collection<Integer> matchedCompetitiveModels = copiedMaterial.getMatchedCompetitiveModels();
									if (matchedCompetitiveModels == null) {
										matchedCompetitiveModels = new ArrayList<>();
										copiedMaterial.setMatchedCompetitiveModels(matchedCompetitiveModels);
									}
									matchedCompetitiveModels.add(modelId);
								}
							}
						}
					}
				}
			}
			
			if(sortedMaterials.values() != null && sortedMaterials.values().size() >0){
				da.setMaterials(sortedMaterials.values());
				return;
			}else{
				//兴趣[0]，地域[1]
				Stream<BitautoMaterial> modelStream = da.getMaterials().stream();//如果兴趣不为空，返回兴趣素材，前提条件是地域符合
				List<BitautoMaterial> filter = modelStream.filter(
						(material) -> {
							Collection<Integer> regions = material.getRegions();
							Collection<Integer> models = material.getModels();
							return  (regions != null && regions.size()>0) && (models ==null || models.size() == 0);
						}).collect(Collectors.toList());
				modelStream.close();
				da.setMaterials(filter);
				return;
			}
		} else {
			logger.warn("DeliveryAttribute is null! DeliveryAttribute:" + da);
		}
		if (getHandler() != null) {
			getHandler().operator(da);
		}
	}

}
