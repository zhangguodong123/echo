package cn.com.cig.adsense.service;

import java.util.List;

import cn.com.cig.adsense.vo.fix.Material;


public interface MaterialLibraryService {
	
	public List<Material> getMaterialByPositionIdRegion(String positionId, int province, int city);

	public List<Material> getDefaultMaterial(String positionId);

	public Material getMaterialByName(String positionId,String smallmaterialName);

	public List<Material> getMaterialByPositionId(String positionId);

	public boolean relatedRegionByPositionId(final String positionId);
	
	public boolean relatedModel(final List<Material> materials);
	
}
