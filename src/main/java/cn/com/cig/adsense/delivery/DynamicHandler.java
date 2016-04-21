package cn.com.cig.adsense.delivery;

import cn.com.cig.adsense.vo.fix.DynamicDeliveryAttribute;

public interface DynamicHandler {
	public void operator(DynamicDeliveryAttribute da);
}
