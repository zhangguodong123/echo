package cn.com.cig.adsense.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.dao.MaterialLibraryDao;
import cn.com.cig.adsense.dao.impl.MaterialLibraryDaoImpl;
import cn.com.cig.adsense.service.BitautoMaterialLibraryService;
import cn.com.cig.adsense.service.ModelMaterialService;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.job.ScheduledJob;
import cn.com.cig.adsense.vo.fix.Advertiser;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.Campaign;
import cn.com.cig.adsense.vo.fix.MaterialType;
import cn.com.cig.adsense.vo.fix.Position;
import cn.com.cig.adsense.vo.fix.Size;

import com.google.common.collect.Maps;

public class BitautoMaterialLibraryServiceImpl implements BitautoMaterialLibraryService {
	private static Logger logger = LoggerFactory.getLogger(BitautoMaterialLibraryServiceImpl.class);
	private final MaterialLibraryDao dao = new MaterialLibraryDaoImpl();
	private final ModelMaterialService modelMaterialService = ModelMaterialServiceImpl.getInstance();
	// 不需要使用volatile，使用旧的也没什么问题，下次使用新的就可以了。
	// 图片素材
	private Map<String, List<BitautoMaterial>> sizeAndMaterials = new HashMap<>();
	private Map<String, List<BitautoMaterial>> sizeAndDefaultMaterias = new HashMap<>();
	private Map<String, List<BitautoMaterial>> sizeAllMaterials = new HashMap<>();// 一定注意，这个变量在
	// 所有文本素材
	private List<BitautoMaterial> allTextMaterial = new ArrayList<BitautoMaterial>();
	private List<BitautoMaterial> allDefaultTextMaterial = new ArrayList<BitautoMaterial>();
	// 所有图文素材
	private List<BitautoMaterial> teleTextSizeMaterials = new ArrayList<BitautoMaterial>();
	private List<BitautoMaterial> teleTextSizeDefaultMaterias = new ArrayList<BitautoMaterial>();
	
	// positionId-size
	private Map<String, String> pidAndSize = new HashMap<>();
	private Map<String, Size> pidSizes = new HashMap<>();
	// 所有的广告位
	private List<Position> allPositions = new ArrayList<Position>();
	private Map<String, Position> allPositionMap = new HashMap<String, Position>();
	// PMP广告位对应广告主
	private Map<Integer, Integer> pmpAdvisters = new HashMap<>();
	private Map<Integer, Advertiser> pmpAdvistersCampains = new HashMap<>();

	private BitautoMaterialLibraryServiceImpl() {
		ScheduledJob.getInstance().scheduleAtFixedRate(autoMatchTask, 1, 120,TimeUnit.SECONDS);
		ScheduledJob.getInstance().scheduleAtFixedRate(positionTask, 3, 100,TimeUnit.SECONDS);
		ScheduledJob.getInstance().scheduleAtFixedRate(task,6,100,TimeUnit.SECONDS);
		ScheduledJob.getInstance().scheduleAtFixedRate(pmp, 1, 60,TimeUnit.SECONDS);
		ScheduledJob.getInstance().scheduleAtFixedRate(pmpAdCampains, 2, 60,TimeUnit.SECONDS);
	}

	private Runnable pmp = new Runnable() {
		@Override
		public void run() {
			// TODO 获取PMP广告主列表
			try {
				Map<Integer, Integer> pmp = dao.getPMPAdvisters();
				if (pmp == null || pmp.size() == 0) {
					logger.error("The pids  comparison relation is null of pmp job......");
					return;
				}
				pmpAdvisters = pmp;
			} catch (Exception e) {
				logger.error("The BitautoMaterialLibraryService scheduled pmp job is error xxxxxxx",e);
			}
		}
	};

	private Runnable pmpAdCampains = new Runnable() {
		@Override
		public void run() {
			// TODO 获取PMP广告主物料
			try {
				Map<Integer, Advertiser> pmpAdvertisers = dao.getPMPAdvertisers();
				if (pmpAdvertisers == null || pmpAdvertisers.size() == 0) {
					logger.error("The advisters  comparison relation is null of pmpAdCampains job......");
					return;
				}
				pmpAdvistersCampains = pmpAdvertisers;
			} catch (Exception e) {
				logger.error("The BitautoMaterialLibraryService scheduled pmpAdCampains job is error xxxxxxx",e);
			}
		}
	};

	private Runnable task = new Runnable() {
		@Override
		public void run() {
			try {
				pidAndSize = dao.getPositionSize();
				pidSizes = dao.getPositionWidthAndHeight();
				// 按尺寸获取素材
				List<BitautoMaterial> tempAllMaterial = dao.getBitautoAllMaterial();
				if ((tempAllMaterial == null) || (tempAllMaterial.size() == 0)) {
					// 只打印日志，仍然使用上一次成功拿到的全部素材。
					logger.error("getAllMaterial is empty!");
					return;
				}
				//文本
				List<BitautoMaterial> tempAllTextMaterial = new ArrayList<BitautoMaterial>();
				List<BitautoMaterial> tempAllDefaultTextMaterial = new ArrayList<BitautoMaterial>();
				
				//图片
				Map<String, List<BitautoMaterial>> tempSizeDefaultMaterias = new HashMap<>();
				Map<String, List<BitautoMaterial>> tempSizeMaterias = new HashMap<>();
				Map<String, List<BitautoMaterial>> tempSizeAllMaterias = new HashMap<>();
				
				//图文
				List<BitautoMaterial> teleTextSizeTempMaterias = new ArrayList<BitautoMaterial>();
				List<BitautoMaterial> teleTextSizeDefaultTempMaterias = new ArrayList<BitautoMaterial>();
				
				Iterator<BitautoMaterial> iter = tempAllMaterial.iterator();
				while (iter.hasNext()) {
					BitautoMaterial m = iter.next();
					if (m.getMatType() == MaterialType.TEXT || m.getMatType() == MaterialType.TELETEXT) {
						// 文本物料不再使用老莫生成的创意，直接用title。解决：title截断以后，创意不同但是title相同的问题。
						m.setCreative(m.getTitle());
						// 1)非精准text素材 不受地域限制
						if (((m.getRegions() == null) || (m.getRegions().size() == 0) || (m.getRegions().size()==1 && m.getRegions().contains(0)))
								&& ((m.getModels() == null) || (m.getModels().size() == 0))) {
							tempAllDefaultTextMaterial.add(m);
						}else{
						// 2)精准text素材
							tempAllTextMaterial.add(m);
						}
					}
					if(m.getMatType() == MaterialType.TELETEXT){
						// 默认/非精准物料放到一起，实现定投.
						m.setCreative(m.getText());
						if (((m.getRegions() == null) || (m.getRegions().size() == 0) || (m.getRegions().size()==1 && m.getRegions().contains(0)))
								&& ((m.getModels() == null) || (m.getModels()
										.size() == 0))) {
							// 1)非精准图文素材
							teleTextSizeDefaultTempMaterias.add(m);
						}else{
							// 2)精准图文素材
							teleTextSizeTempMaterias.add(m);
						}
					}
					if(m.getMatType() == MaterialType.IMAGE || m.getMatType() == MaterialType.FLASH || m.getMatType() == MaterialType.VIDEO){
						String size = m.getWidth()
								+ Constant.BITAUTO_MATERIAL_SIZE_SEPARATOR
								+ m.getHeight();
						// 默认/非精准物料放到一起，实现定投.
						if (((m.getRegions() == null) || (m.getRegions().size() == 0) || (m.getRegions().size()==1 && m.getRegions().contains(0)))
								&& ((m.getModels() == null) || (m.getModels()
										.size() == 0))) {
							// 1)非精准图片素材
							if (tempSizeDefaultMaterias.containsKey(size)) {
								tempSizeDefaultMaterias.get(size).add(m);
							} else {
								tempSizeDefaultMaterias.put(
										size,
										new ArrayList<BitautoMaterial>(Arrays
												.asList(m)));
							}
						}else{
							// 2)精准图片素材
							if (tempSizeMaterias.containsKey(size)) {
								tempSizeMaterias.get(size).add(m);
							} else {
								tempSizeMaterias.put(size, new ArrayList<BitautoMaterial>(Arrays.asList(m)));
							}
						}
						// 3)将精准和非精准全部放在一起，图片PMP和大众投放returnMaterial 里要用到。
						if (tempSizeAllMaterias.containsKey(size)) {
							tempSizeAllMaterias.get(size).add(m);
						} else {
							tempSizeAllMaterias.put(size,new ArrayList<BitautoMaterial>(Arrays.asList(m)));
						}
					}
				}
				//文本素材
				if (tempAllTextMaterial.size() == 0) {
					logger.info("All Bitauto text materials is empty.");
				}
				allTextMaterial = tempAllTextMaterial;
				
				if (tempAllDefaultTextMaterial.size() == 0) {
					logger.info("All Bitauto default text materials is empty.");
				}
				allDefaultTextMaterial = tempAllDefaultTextMaterial;
				
				//图文素材
				if (teleTextSizeTempMaterias.size() == 0) {
					logger.info("All teleTextSizeMaterials materials is empty_<<warn>><<warn>><<warn>><<warn>><<warn>>!!!!");
				}
				teleTextSizeMaterials = teleTextSizeTempMaterias;
				if (teleTextSizeDefaultTempMaterias.size() == 0) {
					logger.info("All teleTextSizeDefaultMaterias materials is empty_<<warn>><<warn>><<warn>><<warn>><<warn>>!!!!");
				}
				teleTextSizeDefaultMaterias = teleTextSizeDefaultTempMaterias;
				
				//图片素材 →素材是否相同，相同就不需要替换了。
				if (Maps.difference(tempSizeMaterias, sizeAndMaterials).areEqual()) {
					logger.info("Materials do not need to replace.");
				} else {
					sizeAndMaterials = tempSizeMaterias;
					logger.info("All Bitauto materials have been replaced.");
				}
				if (Maps.difference(sizeAndDefaultMaterias,tempSizeDefaultMaterias).areEqual()) {
					logger.info("Size and default materias do not need to replace.");
				} else {
					sizeAndDefaultMaterias = tempSizeDefaultMaterias;
					logger.info("Size and default materias have been replaced.");
				}
				if(Maps.difference(sizeAllMaterials,tempSizeAllMaterias).areEqual()){
					logger.info("Materials do not need to replace.");
				}else{
					sizeAllMaterials = tempSizeAllMaterias;
					logger.info("Size and default materias have been replaced.");
				}
			} catch (Exception e) {
				logger.error("BitautoMaterialLibraryService scheduled job error!", e);
			}
		}
	};

	private Runnable autoMatchTask = new Runnable() {
		@Override
		public void run() {
			try {
				Map<Integer, List<Integer>> needAutoMatchModels = dao
						.needAutoMatchModels();
				if ((needAutoMatchModels != null)
						&& (needAutoMatchModels.size() > 0)) {
					Map<Integer, List<Integer>> competitiveModels = modelMaterialService
							.getCompetitiveModels();
					if ((competitiveModels == null)
							|| (competitiveModels.size() == 0)) {
						logger.error("competitiveModels is empty! autoMatch Task exit!");
						return;
					}
					Iterator<Entry<Integer, List<Integer>>> iter = needAutoMatchModels
							.entrySet().iterator();
					Map<Integer, List<Integer>> result = new HashMap<>();
					while (iter.hasNext()) {
						Entry<Integer, List<Integer>> entry = iter.next();
						int materialAudienceId = entry.getKey();
						List<Integer> models = entry.getValue();
						if ((models != null) && (models.size() > 0)) {
							// 竞品
							List<Integer> temp = new ArrayList<>();
							for (int modelId : models) {
								List<Integer> cm = competitiveModels
										.get(modelId);
								if ((cm != null) && (cm.size() > 0)) {
									temp.addAll(cm);
								}
							}
							if (temp.size() > 0) {
								result.put(materialAudienceId, temp);
							}
						}

					}
					dao.saveAutoMatchCompetitiveModels(result);
					logger.info("AutoMatch task succeed.");
				}
			} catch (Exception e) {
				logger.error(
						"BitautoMaterialLibraryService scheduled job autoMatchTask error!",
						e);
			}
		}
	};

	private Runnable positionTask = new Runnable() {

		@Override
		public void run() {
			try {
				allPositions = dao.getAllPositions();
				if (allPositions != null && allPositions.size() > 0) {
					for (Position position : allPositions) {
						allPositionMap.put(position.getId(), position);
					}
				}
			} catch (Exception e) {
				logger.error("BitautoMaterialLibraryService scheduled job positionTask error!",e);
			}
		}
	};

	@Override
	public boolean relatedRegionBySize(final String size) {
		if ((sizeAndMaterials == null) || (sizeAndMaterials.size() == 0)) {
			logger.warn("Bitauto sizeAndMaterials is empty!");
			return false;
		}
		List<BitautoMaterial> ml = sizeAndMaterials.get(size);
		if ((ml == null) || (ml.size() == 0)) {
			return false;
		}
		return true;
	}
	
	@Override
	public boolean relatedRegionByMaterials(
			final Collection<BitautoMaterial> materials) {
		if ((materials == null) || (materials.size() == 0)) {
			logger.warn("Bitauto materials is empty!");
			return false;
		}
		Stream<BitautoMaterial> stream = materials.stream();
		List<BitautoMaterial> filtered = stream.filter(
				(material) -> {
					return (material.getRegions() != null)&& (material.getRegions().size() > 0);
				}).collect(Collectors.toList());
		stream.close();
		if ((filtered == null) || (filtered.size() == 0)) {
			// 未关联地域
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean relatedModel(final Collection<BitautoMaterial> materials) {
		if ((sizeAndMaterials == null) || (sizeAndMaterials.size() == 0)) {
			logger.warn("Bitauto sizeAndMaterials is empty!");
			return false;
		}
		if ((materials == null) || (materials.size() == 0)) {
			logger.warn("materials is empty! materials:" + materials);
			return false;
		}
		Stream<BitautoMaterial> stream = materials.stream();
		List<BitautoMaterial> filtered = stream.filter(
				(material) -> {
					return (material.getModels() != null)
							&& (material.getModels().size() > 0);
				}).collect(Collectors.toList());
		stream.close();
		if ((filtered == null) || (filtered.size() == 0)) {
			// 未关联车型
			return false;
		} else {
			return true;
		}
	}

	@Override
	public List<BitautoMaterial> getMaterialBySize(final String size) {
		if ((sizeAndMaterials == null) || (sizeAndMaterials.size() == 0)) {
			logger.warn("Bitauto sizeAndMaterials is empty!");
			return null;
		}
		List<BitautoMaterial> ms = sizeAndMaterials.get(size);
		return ms;
	}

	@Override
	public List<BitautoMaterial> getMaterialBySizeAndRegion(final String size,
			final int province, final int city) {
		if ((sizeAndMaterials == null) || (sizeAndMaterials.size() == 0)) {
			logger.warn("Bitauto sizeAndMaterials is empty!");
			return null;
		}
		List<BitautoMaterial> ml = sizeAndMaterials.get(size);
		if ((ml == null) || (ml.size() == 0)) {
			return null;
		}
		return getMaterialByRegion(ml, province, city);
	}
	

	@Override
	public List<BitautoMaterial> getMaterialByRegion(
			Collection<BitautoMaterial> materials, int province, int city) {
		if(materials == null){
			logger.warn("The AllEletextMaterial is empty | null >>>>>>>>>>>>>【warn】>【warn】【warn】>【warn】>>>>>>>>>>>>");
			return null;
		}
		Stream<BitautoMaterial> stream = materials.stream();
		List<BitautoMaterial> filtered = stream
				.filter((material) -> {
					if (((material.getRegions() == null)//地域不限，车型不为空的物料
							|| (material.getRegions().size() == 0) || ((material
							.getRegions().size() == 1) && material.getRegions()
							.contains(0)))
							&& ((material.getModels() != null) && material
									.getModels().size() > 0)) {
						return true;
					}
					boolean result = ((material.getRegions().contains(province))
							|| (material.getRegions().contains(city)) || (city == 0 && material
							.getRegions().contains(province * 10)))
							|| (province == 0 && city == 0);
					return result;
				}).collect(Collectors.toList());
		stream.close();
		return filtered;
	}

	@Override
	public List<BitautoMaterial> getDefaultMaterialBySize(final String size) {
		if ((sizeAndDefaultMaterias == null)
				|| (sizeAndDefaultMaterias.size() == 0)) {
			logger.warn("sizeAndDefaultMaterias is empty!");
			return null;
		}
		List<BitautoMaterial> ms = sizeAndDefaultMaterias.get(size);
		return ms;
	}
	
	@Override
	public BitautoMaterial getMaterialByName(final String size,
			final String materialName) {
		if ((sizeAllMaterials == null) || (sizeAllMaterials.size() == 0)) {
			String errorStr = "Bitauto sizeAndMaterials is empty!";
			logger.error(errorStr);
			throw new RuntimeException(errorStr);
		}
		List<BitautoMaterial> ml = sizeAllMaterials.get(size);
		if ((ml == null) || (ml.size() == 0)) {
			return null;
		}

		Stream<BitautoMaterial> stream = ml.stream();
		List<BitautoMaterial> filtered = stream.filter((material) -> {
			return material.getName().equals(materialName);
		}).collect(Collectors.toList());
		stream.close();

		if ((filtered == null) || (filtered.size() == 0)) {
			String errorStr = "Filtered is empty! size:" + size
					+ " smallmaterialName:" + materialName;
			logger.error(errorStr);
			throw new RuntimeException(errorStr);
		} else if (filtered.size() > 1) {
			String errorStr = "filtered illegal! filtered size > 1! size:"
					+ size + " smallmaterialName:" + materialName;
			logger.error(errorStr);
			throw new RuntimeException(errorStr);
		}

		return filtered.get(0);
	}

	@Override
	public String getPositionSizeById(String positionId) {

		if ((positionId == null) || (positionId.length() == 0)) {
			logger.error("positionId is empty! positionId:" + positionId);
		}
		return pidAndSize.get(positionId);
	}

	@Override
	public List<Position> getAllPositions() {
		return allPositions;
	}

	@Override
	public Position getPositionById(String positionId) {
		return allPositionMap.get(positionId);
	}

	@Override
	public List<BitautoMaterial> getAllTextMaterial() {
		return allTextMaterial;
	}

	@Override
	public List<BitautoMaterial> getAllDefaultTextMaterial() {
		return allDefaultTextMaterial;
	}
	
	@Override
	public List<BitautoMaterial> getAllEletextMaterial() {
		return teleTextSizeMaterials;
	}

	@Override
	public List<BitautoMaterial> getAllDefaultEletextMaterial() {
		return teleTextSizeDefaultMaterias;
	}

	// 单例
	private static class MlServiceHolder {
		private static BitautoMaterialLibraryService mlService = new BitautoMaterialLibraryServiceImpl();
	}

	public static BitautoMaterialLibraryService getInstance() {
		return MlServiceHolder.mlService;
	}

	@Override
	public Size getPosIdSizeById(String positionId) {
		if ((positionId == null) || (positionId.length() == 0)) {
			logger.error("positionId is empty! positionId:" + positionId);
		}
		return pidSizes.get(positionId);
	}

	@Override
	public Collection<BitautoMaterial> sortedByCTR(
			Collection<BitautoMaterial> bitautoMaterials, String pid,
			Integer provId, Integer cityId, String urlReffererTag) {
		// TODO update code
		if (bitautoMaterials == null || bitautoMaterials.size() == 0
				|| pid == null) {
			logger.error("bitautoMaterials is:" + bitautoMaterials
					+ " or pid is:" + pid);
			return null;
		}
		StringBuilder materials = new StringBuilder();
		Map<Integer, BitautoMaterial> bitautoMap = new HashMap<Integer, BitautoMaterial>();
		Iterator<BitautoMaterial> it = bitautoMaterials.iterator();
		while (it.hasNext()) {
			BitautoMaterial bitauto = it.next();
			String materIds = String.format("%s_%s_%s_%s", bitauto.getId(),
					bitauto.getMatType().getIndex(), bitauto.getWidth(),
					bitauto.getHeight());
			materials.append(materIds);
			materials.append(",");
			bitautoMap.put(bitauto.getId(), bitauto);
		}
		materials.deleteCharAt(materials.length() - 1);
		String urlFormat = "place=%s&province=%s&city=%s&materials=%s&modelid=%s";
		String params = String.format(urlFormat, pid, provId, cityId,
				materials, urlReffererTag);

		String url = String.format(Constant.CTR_URL_FORMAT, Constant.CTR_URL,
				Constant.CTR_URL_PORT, Constant.CTR_URL_PRIFIX, params);
		// logger.info(url);
		String sortByCtrMaterials = dao.getCTRMaterials(url);
		if (sortByCtrMaterials == null || "".equals(sortByCtrMaterials)
				|| sortByCtrMaterials.length() > 3000000) {
			logger.error("invoke url response is error:>>" + url
					+ " reponseBody_size:" + sortByCtrMaterials.length());
			return null;
		}
		Collection<BitautoMaterial> result = new ArrayList<>();
		JSONArray data = new JSONArray(sortByCtrMaterials);
		for (int i = 0; i < data.length(); i++) {
			JSONObject obj = data.getJSONObject(i);
			int mid = obj.getInt("materialId");
			BitautoMaterial bitautoMaterial = bitautoMap.get(mid);
			result.add(bitautoMaterial);
		}
		return result;
	}

	@Override
	public Integer getPMPAdvisterID(Integer pid) {
		if (pmpAdvisters == null || pmpAdvisters.size() == 0) {
			logger.error("the pmpAdvisters is empty!");
			return null;
		}
		if (pid != null) {
			return pmpAdvisters.get(pid);
		}
		return null;
	}

	@Override
	public Advertiser getPMPAdvister(Integer advisterId) {
		if (pmpAdvistersCampains == null || pmpAdvistersCampains.size() == 0) {
			logger.error("the pmpAdvistersCampains is empty!");
			return null;
		}
		if (advisterId != null) {
			return pmpAdvistersCampains.get(advisterId);
		}
		return null;
	}

	@Override
	public Campaign getDirectPMPCampain(Integer aid, Integer pid) {
		// TODO 查询定投广告计划根据广告主ID和广告位ID
		if (pmpAdvistersCampains == null || pmpAdvistersCampains.size() == 0) {
			logger.error("the pmpAdvistersCampains is empty!");
			return null;
		}
		Advertiser advertiser = pmpAdvistersCampains.get(aid);
		if (advertiser == null) {
			logger.warn("The " + pid + " of Advertiser is null");
			return null;
		}
		List<Campaign> campaigns = advertiser.getDirectional();// 先从定投广告计划中找是否是PMP定投
		if (campaigns == null || campaigns.size() == 0) {
			logger.warn("The " + pid + " Belong CampaignList is null");
			return null;
		}
		Campaign fixedCampaign = null;
		for (int i = 0; i < campaigns.size(); i++) {
			Campaign campaign = campaigns.get(i);
			if (campaign == null) {
				continue;
			}
			if ((campaign.getPlaces().contains(pid))
					&& campaign.getIsPmp() == 1) {
				fixedCampaign = campaign;
				break;
			}
		}
		return fixedCampaign;
	}

	@Override
	public List<BitautoMaterial> getDirectPMPMaterialsBySizeAndRegion(
			Campaign campaign, String size, int province, int city) {
		// 获取PMP地域定投精准相关物料
		if (campaign == null) {
			logger.warn("The directional of campaign:" + campaign + " is null");
			return null;
		}
		Map<String, List<BitautoMaterial>> fixedMap = campaign
				.getBitautoMaterial();
		if (fixedMap == null) {
			logger.error("The campaign:" + campaign + " size:" + size
					+ " materials is null..");
			return null;
		}
		List<BitautoMaterial> bitautolist = fixedMap.get(size);
		if (bitautolist == null || bitautolist.size() == 0) {
			logger.warn("The campaign Belong Campaign materials of size:"
					+ size + " is null");
			return null;
		}
		return getMaterialByRegion(bitautolist, province, city);
	}

	@Override
	public List<BitautoMaterial> getDirectPMPDefaultMaterialsBySizeAndRegion(
			Campaign campaign, String size) {
		// 获取PMP地域定投默认素材相关物料
		if (campaign == null) {
			logger.warn("The directional campaign:" + campaign + " is null");
			return null;
		}
		Map<String, List<BitautoMaterial>> fixedDefaultMap = campaign
				.getDefaultBitautoMaterial();
		if (fixedDefaultMap == null) {
			logger.error("The campaign:" + campaign + " size:" + size
					+ " materials is null..");
			return null;
		}
		List<BitautoMaterial> bitautolist = fixedDefaultMap.get(size);
		if (bitautolist == null || bitautolist.size() == 0) {
			logger.warn("The campaign belong materials of size:" + size
					+ " is null");
			return null;
		}
		return bitautolist;
	}

	@Override
	public Collection<BitautoMaterial> getUnDirectPMPMaterialsBySizeAndRegion(
			List<Campaign> unDirectional, String size, int province, int city) {
		// TODO 获取非定投PMP精准素材，整合所有素材问题
		if (unDirectional == null || unDirectional.size() == 0) {
			logger.error("UnDirectionalList is empty...");
			return null;
		}
		List<BitautoMaterial> data = new ArrayList<>();
		for (int i = 0; i < unDirectional.size(); i++) {
			Campaign campaign = unDirectional.get(i);
			if (campaign == null) {
				continue;
			}
			Map<String, List<BitautoMaterial>> bitautoMaterial = campaign
					.getBitautoMaterial();
			if (bitautoMaterial == null || bitautoMaterial.size() == 0) {
				logger.error("The undirectional of list:" + bitautoMaterial
						+ " is null");
				continue;
			}
			List<BitautoMaterial> list = bitautoMaterial.get(size);
			if (list == null || list.size() == 0) {
				continue;
			}
			List<BitautoMaterial> materialByRegion = getMaterialByRegion(list,
					province, city);
			if (materialByRegion == null || materialByRegion.size() == 0) {
				continue;
			}
			data.addAll(materialByRegion);
		}
		return data;
	}

	@Override
	public Collection<BitautoMaterial> getUnDirectPMPDefaultMaterialsBySize(
			List<Campaign> unDirectional, String size) {
		// TODO get materials of campain'list
		if (unDirectional == null || unDirectional.size() == 0) {
			logger.error("UnDirectionalList is empty...");
			return null;
		}
		List<BitautoMaterial> data = new ArrayList<>();
		for (int i = 0; i < unDirectional.size(); i++) {
			Campaign campaign = unDirectional.get(i);
			if (campaign == null) {
				continue;
			}
			Map<String, List<BitautoMaterial>> bitautoMaterial = campaign
					.getDefaultBitautoMaterial();
			if (bitautoMaterial == null || bitautoMaterial.size() == 0) {
				continue;
			}
			List<BitautoMaterial> list = bitautoMaterial.get(size);
			if (list == null || list.size() == 0) {
				continue;
			}
			data.addAll(list);
		}
		return data;
	}

	@Override
	public Collection<BitautoMaterial> getGeneralPMPMaterialsBySizeAndRegion(
			List<Campaign> general, String size, int province, int city) {
		// TODO 获取非定投PMP,和非PMP广告计划列表精准素材，整合所有素材问题
		List<BitautoMaterial> data = new ArrayList<>();
		if (general == null || general.size() == 0) {
			logger.error("general is empty...");
			return null;
		}
		for (int i = 0; i < general.size(); i++) {
			Campaign campaign = general.get(i);
			if (campaign == null) {
				continue;
			}
			Map<String, List<BitautoMaterial>> bitautoMaterial = campaign
					.getBitautoMaterial();
			if (bitautoMaterial == null || bitautoMaterial.size() == 0) {
				continue;
			}
			List<BitautoMaterial> list = bitautoMaterial.get(size);
			if (list == null || list.size() == 0) {
				continue;
			}
			List<BitautoMaterial> materialByRegion = getMaterialByRegion(list,
					province, city);
			if (materialByRegion == null || materialByRegion.size() == 0) {
				continue;
			}
			data.addAll(materialByRegion);
		}
		return data;
	}

	@Override
	public Collection<BitautoMaterial> getGeneralPMPDefaultMaterialsBySize(
			List<Campaign> general, String size) {
		if (general == null) {
			logger.error("The GeneralPMP campaign is null...");
			return null;
		}
		List<BitautoMaterial> data = new ArrayList<>();
		for (int i = 0; i < general.size(); i++) {
			Campaign campaign = general.get(i);
			if (campaign == null) {
				continue;
			}
			Map<String, List<BitautoMaterial>> bitautoMaterial = campaign
					.getDefaultBitautoMaterial();
			if (bitautoMaterial == null || bitautoMaterial.size() == 0) {
				continue;
			}
			List<BitautoMaterial> list = bitautoMaterial.get(size);
			if (list == null || list.size() == 0) {
				continue;
			}
			data.addAll(list);
		}
		return data;
	}

	@Override
	public Collection<BitautoMaterial> getTextDirectPMPMaterialsBySizeAndRegion(
			Campaign directOfCamp, int province, int city) {
		if (directOfCamp == null) {
			logger.warn("The directOfCamp of TextCampaign:" + directOfCamp
					+ " is null");
			return null;
		}
		List<BitautoMaterial> bitautoMaterialText = directOfCamp
				.getBitautoMaterialText();
		if (bitautoMaterialText == null || bitautoMaterialText.size() == 0) {
			return null;
		}
		return getMaterialByRegion(bitautoMaterialText, province, city);
	}

	@Override
	public Collection<BitautoMaterial> getTextDirectPMPDefaultMaterialsBySizeAndRegion(
			Campaign directOfCamp) {
		if (directOfCamp == null) {
			logger.warn("The directOfCamp of TextCampaign:" + directOfCamp
					+ " is null");
			return null;
		}
		List<BitautoMaterial> bitautoMaterialText = directOfCamp
				.getDefaultBitautoMaterialText();
		if (bitautoMaterialText == null || bitautoMaterialText.size() == 0) {
			return null;
		}
		return bitautoMaterialText;
	}

	@Override
	public Collection<BitautoMaterial> getTextUnDirectPMPMaterialsBySizeAndRegion(
			List<Campaign> unDirectional, int province, int city) {
		List<BitautoMaterial> data = new ArrayList<>();
		if (unDirectional == null || unDirectional.size() == 0) {
			logger.error("TextUnDirectPMPMaterials unDirectional is empty...");
			return null;
		}
		for (int i = 0; i < unDirectional.size(); i++) {
			Campaign campaign = unDirectional.get(i);
			if (campaign == null) {
				continue;
			}
			List<BitautoMaterial> list = campaign.getBitautoMaterialText();
			if (list == null || list.size() == 0) {
				continue;
			}
			List<BitautoMaterial> materialByRegion = getMaterialByRegion(list,
					province, city);
			if (materialByRegion == null || materialByRegion.size() == 0) {
				continue;
			}
			data.addAll(materialByRegion);
		}
		return data;
	}

	@Override
	public Collection<BitautoMaterial> getTextUnDirectPMPDefaultMaterialsBySize(
			List<Campaign> unDirectional) {
		// TODO 获取非定投PMP,和非PMP广告计划列表精准素材，整合所有素材问题
		List<BitautoMaterial> data = new ArrayList<>();
		if (unDirectional == null || unDirectional.size() == 0) {
			logger.error("TextUnDirectPMPDefaultMaterials unDirectional is empty...");
			return null;
		}
		for (int i = 0; i < unDirectional.size(); i++) {
			Campaign campaign = unDirectional.get(i);
			if (campaign == null) {
				continue;
			}
			List<BitautoMaterial> list = campaign
					.getDefaultBitautoMaterialText();
			if (list == null || list.size() == 0) {
				continue;
			}
			data.addAll(list);
		}
		return data;
	}

	@Override
	public Collection<BitautoMaterial> getTextGeneralPMPMaterialsBySizeAndRegion(
			List<Campaign> general, int province, int city) {
		// TODO 获取非定投PMP,和非PMP广告计划列表精准素材，整合所有素材问题
		List<BitautoMaterial> data = new ArrayList<>();
		if (general == null || general.size() == 0) {
			logger.error("TextGeneralPMPMaterials general is empty...");
			return null;
		}
		for (int i = 0; i < general.size(); i++) {
			Campaign campaign = general.get(i);
			if (campaign == null) {
				continue;
			}
			List<BitautoMaterial> list = campaign.getBitautoMaterialText();
			if (list == null || list.size() == 0) {
				continue;
			}
			List<BitautoMaterial> materialByRegion = getMaterialByRegion(list,
					province, city);
			if (materialByRegion == null || materialByRegion.size() == 0) {
				continue;
			}
			data.addAll(materialByRegion);
		}
		return data;
	}

	@Override
	public Collection<BitautoMaterial> getTextGeneralPMPDefaultMaterialsBySize(
			List<Campaign> general) {
		List<BitautoMaterial> data = new ArrayList<>();
		if (general == null || general.size() == 0) {
			logger.error("unDirectional is empty...");
			return null;
		}
		for (int i = 0; i < general.size(); i++) {
			Campaign campaign = general.get(i);
			if (campaign == null) {
				continue;
			}
			List<BitautoMaterial> list = campaign
					.getDefaultBitautoMaterialText();
			if (list == null || list.size() == 0) {
				continue;
			}
			data.addAll(list);
		}
		return data;
	}

	@Override
	public List<BitautoMaterial> getDirectPMPEletextMaterialsBySizeAndRegion(Campaign campaign, int province, int city) {
		// 获取PMP地域定投精准相关物料
		if (campaign == null) {
			logger.warn("The directOfCamp of EletextCampaign:" + campaign+ " is null");
			return null;
		}
		List<BitautoMaterial> bitautoMaterialText = campaign.getBitautoEletextMaterial();
		if (bitautoMaterialText == null || bitautoMaterialText.size() == 0) {
			return null;
		}
		return getMaterialByRegion(bitautoMaterialText, province, city);
	}

	@Override
	public List<BitautoMaterial> getDirectPMPEletextDefaultMaterialsBySizeAndRegion(Campaign campaign) {
		// 获取PMP地域定投默认素材相关物料
		if (campaign == null) {
			logger.warn("The directOfCamp of EletextCampaign:" + campaign+ " is null");
			return null;
		}
		List<BitautoMaterial> bitautoMaterialEletext = campaign.getDefaultEletextMaterial();
		if (bitautoMaterialEletext == null || bitautoMaterialEletext.size() == 0) {
			return null;
		}
		return bitautoMaterialEletext;
	}

	@Override
	public Collection<BitautoMaterial> getGeneralPMPEletextMaterialsBySizeAndRegion(List<Campaign> general,int province, int city) {
		// TODO 获取非定投PMP,和非PMP广告计划列表精准素材，整合所有素材问题
		List<BitautoMaterial> data = new ArrayList<>();
		if (general == null || general.size() == 0) {
			logger.error("TextGeneralPMPEletextMaterials general is empty...");
			return null;
		}
		for (int i = 0; i < general.size(); i++) {
			Campaign campaign = general.get(i);
			if (campaign == null) {
				continue;
			}
			List<BitautoMaterial> list = campaign.getBitautoEletextMaterial();
			if (list == null || list.size() == 0) {
				continue;
			}
			List<BitautoMaterial> materialByRegion = getMaterialByRegion(list,province, city);
			if (materialByRegion == null || materialByRegion.size() == 0) {
				continue;
			}
			data.addAll(materialByRegion);
		}
		return data;
	}

	@Override
	public Collection<BitautoMaterial> getGeneralPMPEletextDefaultMaterialsBySize(List<Campaign> general) {
		List<BitautoMaterial> data = new ArrayList<>();
		if (general == null || general.size() == 0) {
			logger.error("unDirectional is empty...");
			return null;
		}
		for (int i = 0; i < general.size(); i++) {
			Campaign campaign = general.get(i);
			if (campaign == null) {
				continue;
			}
			List<BitautoMaterial> list = campaign.getDefaultEletextMaterial();
			if (list == null || list.size() == 0) {
				continue;
			}
			data.addAll(list);
		}
		return data;
	}

	@Override
	public Collection<BitautoMaterial> getUnDirectPMPEletextMaterialsBySizeAndRegion(List<Campaign> unDirectional, int province, int city) {
		// TODO 获取非定投PMP精准素材，整合所有素材问题
		List<BitautoMaterial> data = new ArrayList<>();
		if (unDirectional == null || unDirectional.size() == 0) {
			logger.error("EletextUnDirectPMPMaterials unDirectional is empty...");
			return null;
		}
		for (int i = 0; i < unDirectional.size(); i++) {
			Campaign campaign = unDirectional.get(i);
			if (campaign == null) {
				continue;
			}
			List<BitautoMaterial> list = campaign.getBitautoEletextMaterial();
			if (list == null || list.size() == 0) {
				continue;
			}
			List<BitautoMaterial> materialByRegion = getMaterialByRegion(list,
					province, city);
			if (materialByRegion == null || materialByRegion.size() == 0) {
				continue;
			}
			data.addAll(materialByRegion);
		}
		return data;
	}

	@Override
	public Collection<BitautoMaterial> getUnDirectPMPEletextDefaultMaterialsBySize(List<Campaign> unDirectional) {
		// TODO 获取非定投PMP,和非PMP广告计划列表精准素材，整合所有素材问题
		List<BitautoMaterial> data = new ArrayList<>();
		if (unDirectional == null || unDirectional.size() == 0) {
			logger.error("EletextUnDirectPMPDefaultMaterials unDirectional is empty...");
			return null;
		}
		for (int i = 0; i < unDirectional.size(); i++) {
			Campaign campaign = unDirectional.get(i);
			if (campaign == null) {
				continue;
			}
			List<BitautoMaterial> list = campaign.getDefaultEletextMaterial();
			if (list == null || list.size() == 0) {
				continue;
			}
			data.addAll(list);
		}
		return data;
	}
}
