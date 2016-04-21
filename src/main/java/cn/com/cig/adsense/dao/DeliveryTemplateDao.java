package cn.com.cig.adsense.dao;

public interface DeliveryTemplateDao {

	public String getTemplateByPositionIdAndSize(String positionId,
			int width, int height);
	
}
