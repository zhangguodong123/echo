package cn.com.cig.adsense.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.com.cig.adsense.vo.fix.ModelMaterial;

public interface ModelMaterialService {
	public Map<Integer, List<Integer>> getCompetitiveModels();
	public Collection<ModelMaterial> getTop9(int positionIdTyp);
	public Collection<ModelMaterial> getTopNByAllModels(Set<Integer> keySet,int positionIdTyp);
	public Collection<ModelMaterial> getTopNByProvinceAndcity(int province, int city,int positionIdTyp);
	public Collection<ModelMaterial> getTopNByModelsProvinceAndcity(Set<Integer> keySet, int province, int city, int positionIdTyp);
	public Map<Integer, Integer> getMatchYipaiRegions();
	public void add(Integer positionId, Integer deviceId);
}
