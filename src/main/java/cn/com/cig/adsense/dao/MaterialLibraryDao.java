package cn.com.cig.adsense.dao;

import java.util.List;
import java.util.Map;

import cn.com.cig.adsense.vo.fix.Advertiser;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.Material;
import cn.com.cig.adsense.vo.fix.Position;
import cn.com.cig.adsense.vo.fix.Size;

public interface MaterialLibraryDao {

	public List<Material> getCigAllMaterial();
	
	public List<BitautoMaterial> getBitautoAllMaterial();
	
	public Map<String, String> getPositionSize();

	public Map<Integer, List<Integer>> needAutoMatchModels();
	
	public void saveAutoMatchCompetitiveModels(Map<Integer, List<Integer>> competitiveModels);

	public List<Position> getAllPositions();
	
	public Map<String, Size> getPositionWidthAndHeight();
	
	public String getCTRMaterials(String url);
	
	public Map<Integer,Integer> getPMPAdvisters();
	
	public Map<Integer,Advertiser> getPMPAdvertisers();
	
}
