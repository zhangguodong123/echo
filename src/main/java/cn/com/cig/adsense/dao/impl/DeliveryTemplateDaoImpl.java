package cn.com.cig.adsense.dao.impl;

import cn.com.cig.adsense.dao.DeliveryTemplateDao;

public class DeliveryTemplateDaoImpl implements DeliveryTemplateDao {

	@Override
	public String getTemplateByPositionIdAndSize(String positionId,
			int width, int height) {
		
		//TODO 从mysql取投放模板
		return "1234|1|asdf";
	}

}
