package cn.com.cig.adsense.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.dao.MaterialLibraryDao;
import cn.com.cig.adsense.dao.impl.MaterialLibraryDaoImpl;
import cn.com.cig.adsense.service.MaterialLibraryService;
import cn.com.cig.adsense.utils.job.ScheduledJob;
import cn.com.cig.adsense.vo.fix.Material;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class CigMaterialLibraryServiceImpl implements MaterialLibraryService {

	private static Logger logger = LoggerFactory
			.getLogger(CigMaterialLibraryServiceImpl.class);

	private final MaterialLibraryDao dao = new MaterialLibraryDaoImpl();
	// 不需要使用volatile，使用旧的positionIdAndMaterial也没什么问题，下次使用新的就可以了。
	// positionId-Material
	private Map<String, List<Material>> cigPositionIdAndMaterial = new HashMap<>();

	private CigMaterialLibraryServiceImpl() {
		ScheduledJob.getInstance().scheduleAtFixedRate(task, 1, 60,
				TimeUnit.SECONDS);
	}
	
	private Runnable task = new Runnable() {
		@Override
		public void run() {
			try{
				List<Material> allMaterial = dao.getCigAllMaterial();
				if ((allMaterial == null) || (allMaterial.size() == 0)) {
					// 只打印日志，仍然使用上一次成功拿到的全部素材。
					logger.error("getAllMaterial is empty!");
				} else {
					Iterator<Material> iter = allMaterial.iterator();
					Map<String, List<Material>> temp = new HashMap<>();
					while (iter.hasNext()) {
						Material m = iter.next();
						String positionId = m.getPositionId();
						if (temp.containsKey(positionId)) {
							temp.get(positionId).add(m);
						} else {
							temp.put(positionId,
									new ArrayList<Material>(Arrays.asList(m)));
						}
					}
					// 素材是否相同，相同就不需要替换了。
					boolean areEqual = Maps.difference(temp,
							cigPositionIdAndMaterial).areEqual();
					if (areEqual) {
						logger.info("Materials do not need to replace.");
					} else {
						cigPositionIdAndMaterial = ImmutableMap.copyOf(temp);
						logger.info("All Cig materials have been replaced.");
					}
				}
			} catch(Exception e) {
				logger.error("CigMaterialLibraryService scheduled job error!", e);
			}
		}
	};

	@Override
	public boolean relatedRegionByPositionId(final String positionId) {
		if ((cigPositionIdAndMaterial == null)
				|| (cigPositionIdAndMaterial.size() == 0)) {
			return false;
		}
		List<Material> ml = cigPositionIdAndMaterial.get(positionId);
		if ((ml == null) || (ml.size() == 0)) {
			return false;
		}
		List<Material> filtered = Lists.newArrayList(Iterables.filter(ml,
				new Predicate<Material>() {
					public boolean apply(Material m) {
						boolean result = (m.getPositionId().equals(positionId))
								&& ((m.getRegions() != null) && (m.getRegions()
										.size() > 0)) && (m.getType() != 1);
						return result;
					}
				}));
		if ((filtered == null) || (filtered.size() == 0)) {
			// 未关联地域
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean relatedModel(final List<Material> materials) {
		if ((cigPositionIdAndMaterial == null)
				|| (cigPositionIdAndMaterial.size() == 0)) {
			return false;
		}
		if ((materials == null) || (materials.size() == 0)) {
			logger.warn("materials is empty! materials:" + materials);
			return false;
		}
		final String positionId = materials.get(0).getPositionId();
		List<Material> filtered = Lists.newArrayList(Iterables.filter(materials,
				new Predicate<Material>() {
					public boolean apply(Material m) {
						boolean result = (m.getPositionId().equals(positionId))
								&& ((m.getModels() != null) && (m.getModels()
										.size() > 0)) && (m.getType() != 1);
						return result;
					}
				}));
		if ((filtered == null) || (filtered.size() == 0)) {
			// 未关联地域
			return false;
		} else {
			return true;
		}
	}

	@Override
	public List<Material> getMaterialByPositionId(final String positionId) {
		if ((cigPositionIdAndMaterial == null)
				|| (cigPositionIdAndMaterial.size() == 0)) {
			return null;
		}
		List<Material> ml = cigPositionIdAndMaterial.get(positionId);
		if ((ml == null) || (ml.size() == 0)) {
			return null;
		}
		List<Material> filtered = Lists.newArrayList(Iterables.filter(ml,
				new Predicate<Material>() {
					public boolean apply(Material m) {
						boolean result = (m.getPositionId().equals(positionId))
								&& (m.getType() != 1);
						return result;
					}
				}));
		return filtered;
	}

	@Override
	public List<Material> getMaterialByPositionIdRegion(
			final String positionId, final int province, final int city) {
		if ((cigPositionIdAndMaterial == null)
				|| (cigPositionIdAndMaterial.size() == 0)) {
			return null;
		}
		List<Material> ml = cigPositionIdAndMaterial.get(positionId);
		if ((ml == null) || (ml.size() == 0)) {
			return null;
		}
		List<Material> filtered = Lists.newArrayList(Iterables.filter(ml,
				new Predicate<Material>() {
					public boolean apply(Material m) {
						if((m.getRegions()==null) || (m.getRegions().size()==0)){
							return false;
						}
						boolean result = (m.getPositionId().equals(positionId))
								&& ((m.getRegions().contains(province)) || (m
										.getRegions().contains(city)))
								&& (m.getType() != 1);
						return result;
					}
				}));
		return filtered;
	}

	@Override
	public List<Material> getDefaultMaterial(final String positionId) {
		if ((cigPositionIdAndMaterial == null)
				|| (cigPositionIdAndMaterial.size() == 0)) {
			return null;
		}
		List<Material> ml = cigPositionIdAndMaterial.get(positionId);
		if ((ml == null) || (ml.size() == 0)) {
			return null;
		}
		List<Material> filtered = Lists.newArrayList(Iterables.filter(ml,
				new Predicate<Material>() {
					public boolean apply(Material m) {
						return (m.getType() == 1);
					}
				}));
		return filtered;
	}

	@Override
	public Material getMaterialByName(final String positionId,
			final String materialName) {
		if ((cigPositionIdAndMaterial == null)
				|| (cigPositionIdAndMaterial.size() == 0)) {
			String errorStr = "cigPositionIdAndMaterial is empty!";
			logger.error(errorStr);
			throw new RuntimeException(errorStr);
		}
		List<Material> ml = cigPositionIdAndMaterial.get(positionId);
		if ((ml == null) || (ml.size() == 0)) {
			return null;
		}
		List<Material> filtered = Lists.newArrayList(Iterables.filter(ml,
				new Predicate<Material>() {
					public boolean apply(Material m) {
						return (m.getName().equals(materialName));
					}
				}));

		if ((filtered == null) || (filtered.size() == 0)) {
			String errorStr = "Filtered is empty! positionId:" + positionId
					+ " smallmaterialName:" + materialName;
			logger.error(errorStr);
			throw new RuntimeException(errorStr);
		} else if (filtered.size() > 1) {
			String errorStr = "filtered illegal! filtered size > 1! positionId:"
					+ positionId + " smallmaterialName:" + materialName;
			logger.error(errorStr);
			throw new RuntimeException(errorStr);
		}

		return filtered.get(0);
	}

	// 单例
	private static class MlServiceHolder {
		private static MaterialLibraryService mlService = new CigMaterialLibraryServiceImpl();
	}

	public static MaterialLibraryService getInstance() {
		return MlServiceHolder.mlService;
	}

}
