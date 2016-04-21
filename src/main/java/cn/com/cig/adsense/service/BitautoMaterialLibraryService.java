package cn.com.cig.adsense.service;

import java.util.Collection;
import java.util.List;

import cn.com.cig.adsense.vo.fix.Advertiser;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.Campaign;
import cn.com.cig.adsense.vo.fix.Position;
import cn.com.cig.adsense.vo.fix.Size;


public interface BitautoMaterialLibraryService {
	
	public List<BitautoMaterial> getMaterialBySizeAndRegion(final String size, final int province, final int city);

	public List<BitautoMaterial> getMaterialByRegion(Collection<BitautoMaterial> materials, final int province, final int city);

	public List<BitautoMaterial> getDefaultMaterialBySize(final String size);
	
	public BitautoMaterial getMaterialByName(final String size,final String materialName);

	public List<BitautoMaterial> getMaterialBySize(final String size);

	public boolean relatedRegionBySize(final String size);
	
	public boolean relatedRegionByMaterials(Collection<BitautoMaterial> materials);
	
	public boolean relatedModel(final Collection<BitautoMaterial> materials);
	
	public String getPositionSizeById(final String positionId);
	
	public Size getPosIdSizeById(final String positionId);
	
	public List<Position> getAllPositions();

	public Position getPositionById(String positionId);
	
	public List<BitautoMaterial> getAllTextMaterial();
	
	public List<BitautoMaterial> getAllDefaultTextMaterial();
	
	public List<BitautoMaterial> getAllEletextMaterial();
	
	public List<BitautoMaterial> getAllDefaultEletextMaterial();
	
	public Collection<BitautoMaterial> sortedByCTR(Collection<BitautoMaterial> bitautoMaterials,String pid,Integer provId,Integer cityId,String urlReffererTag);
	/**
	 * description:根据广告位ID获取PMP广告主ID
	 * @param pid
	 * @return
	 */
	public Integer getPMPAdvisterID(Integer pid);
	/**
	 * description:根据广告主ID，获取广告主实体对象
	 * @param aid
	 * @return
	 */
	public Advertiser getPMPAdvister(Integer aid);
	/**
	 * description:根据广告主ID，和广告位ID，获取PMP定投广告计划实体
	 * @param aid
	 * @param pid
	 * @return
	 */
	public Campaign getDirectPMPCampain(Integer aid,Integer pid);
	/**
	 * description:根据广告计划，广告位尺寸省份ID，和城市ID获取PMP定投广告物料
	 * @param campaign
	 * @param size
	 * @param province
	 * @param city
	 * @return
	 */
	public List<BitautoMaterial> getDirectPMPMaterialsBySizeAndRegion(Campaign campaign,String size,int province,int city);//获取PMP定投广告位的广告计划物料
	/**
	 * description:根据广告计划，广告位尺寸PMP定投默认广告物料
	 * @param campaign
	 * @param size
	 * @param province
	 * @param city
	 * @return
	 */
	public List<BitautoMaterial> getDirectPMPDefaultMaterialsBySizeAndRegion(Campaign campaign,String size);//获取PMP定投广告位的广告计划物料
	
	/**
	 * description:根据广告计划，广告位尺寸PMP定投默认广告图文物料
	 * @param campaign
	 * @param size
	 * @return
	 */
	public List<BitautoMaterial> getDirectPMPEletextMaterialsBySizeAndRegion(Campaign campaign,int province,int city); 		//获取PMP文本定投广告位的广告计划物料
	
	/**
	 * description:根据广告计划，广告位尺寸PMP定投默认广告图文物料
	 * @param campaign
	 * @param size
	 * @return
	 */
	public List<BitautoMaterial> getDirectPMPEletextDefaultMaterialsBySizeAndRegion(Campaign campaign); //获取PMP文本定投广告位的广告计划默认物料
	
	/**
	 * description:根据PMP非定投广告计划列表，城市ID，尺寸获取精准物料
	 * @param unDirectional
	 * @param size
	 * @param province
	 * @param city
	 * @return
	 */
	public Collection<BitautoMaterial> getUnDirectPMPMaterialsBySizeAndRegion(List<Campaign> unDirectional, String size, int province, int city);
	
	/**
	 * description:根据PMP广告计划，获取PMP广告计划列表中的默认物料
	 * @param unDirectional
	 * @param size
	 * @return
	 */
	public Collection<BitautoMaterial> getUnDirectPMPDefaultMaterialsBySize(List<Campaign> unDirectional, String size);
	/**
	 * description:根据PMP非定投广告计划列表，城市ID，尺寸获取精准物料(图文)
	 * @param unDirectional
	 * @param size
	 * @param province
	 * @param city
	 * @return
	 */
	public Collection<BitautoMaterial> getUnDirectPMPEletextMaterialsBySizeAndRegion(List<Campaign> unDirectional,int province, int city);
	/**
	 * description:根据PMP广告计划，获取PMP广告计划列表中的默认物料(图文)
	 * @param unDirectional
	 * @param size
	 * @return
	 */
	public Collection<BitautoMaterial> getUnDirectPMPEletextDefaultMaterialsBySize(List<Campaign> unDirectional);
	/**
	 * description:根据计划列表，尺寸，省份，城市获取广告主下普通广告计划下的精准物料
	 * @param general
	 * @param size
	 * @param province
	 * @param city
	 * @return
	 */
	public Collection<BitautoMaterial> getGeneralPMPMaterialsBySizeAndRegion(
			List<Campaign> general, String size, int province, int city);
	/**
	 * description:根据普通计划列表尺寸获取物料
	 * @param general
	 * @param size
	 * @return
	 */
	public Collection<BitautoMaterial> getGeneralPMPDefaultMaterialsBySize(List<Campaign> general, String size);
	/**
	 * description:根据普通计划列表尺寸获取物料(图文)
	 * @param general
	 * @param size
	 * @param province
	 * @param city
	 * @return
	 */
	public Collection<BitautoMaterial> getGeneralPMPEletextMaterialsBySizeAndRegion(List<Campaign> general, int province, int city);
	/**
	 * description:根据普通计划列表尺寸获取物料(图文)
	 * @param general
	 * @param size
	 * @return
	 */
	public Collection<BitautoMaterial> getGeneralPMPEletextDefaultMaterialsBySize(List<Campaign> general);
	/**
	 * description:文本投放：根据给定的广告计划，省份ID，城市ID获取物料
	 * @param directOfCamp
	 * @param province
	 * @param city
	 * @return
	 */
	public Collection<BitautoMaterial> getTextDirectPMPMaterialsBySizeAndRegion(
			Campaign directOfCamp, int province, int city);
	/**
	 * description:文本投放：根据定投给的广告计划获取默认物料
	 * @param directOfCamp
	 * @return
	 */
	public Collection<BitautoMaterial> getTextDirectPMPDefaultMaterialsBySizeAndRegion(
			Campaign directOfCamp);
	/**
	 * description:根据PMP广告计划列表，获取默认物料
	 * @param unDirectional
	 * @return
	 */
	public Collection<BitautoMaterial> getTextUnDirectPMPDefaultMaterialsBySize(
			List<Campaign> unDirectional);
	/**
	 * description:根据PMP广告计划列表，省份ID，城市ID获取广告物料
	 * @param unDirectional
	 * @param province
	 * @param city
	 * @return
	 */
	public Collection<BitautoMaterial> getTextUnDirectPMPMaterialsBySizeAndRegion(
			List<Campaign> unDirectional, int province, int city);
	/**
	 * description:根据普通广告计划列表，省份ID，城市ID获取物料
	 * @param general
	 * @param province
	 * @param city
	 * @return
	 */
	public Collection<BitautoMaterial> getTextGeneralPMPMaterialsBySizeAndRegion(
			List<Campaign> general, int province, int city);
	/**
	 * description:根据普通广告计划列表，获取默认物料
	 * @param general
	 * @return
	 */
	public Collection<BitautoMaterial> getTextGeneralPMPDefaultMaterialsBySize(List<Campaign> general);
}
