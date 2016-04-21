package cn.com.cig.adsense.service;

import java.text.ParseException;
import java.util.List;

import cn.com.cig.adsense.service.MaterialLibraryService;
import cn.com.cig.adsense.service.impl.CigMaterialLibraryServiceImpl;
import cn.com.cig.adsense.utils.job.ScheduledJob;
import cn.com.cig.adsense.vo.fix.Material;
import junit.framework.TestCase;

public class MaterialLibraryServiceTest extends TestCase {

	private MaterialLibraryService mls;
	
	protected void setUp() throws Exception {
		mls = CigMaterialLibraryServiceImpl.getInstance();
	}

	@Override
	protected void tearDown() throws Exception {
		ScheduledJob.getInstance().close();
	}

	public void testDao() throws ParseException {
//		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//		List<Material> list = mls.getMaterialByPositionIdRegionAndDate("2",
//				df.parse("2014-4-15"), 440000, 451100);
		
		List<Material> list = mls.getDefaultMaterial("3");
		assertTrue(list != null);
		assertTrue(list.size() > 0);
		for(Material m : list){
			System.out.println(m.getPositionId() + ":" + m.getId());
		}
	}
}
