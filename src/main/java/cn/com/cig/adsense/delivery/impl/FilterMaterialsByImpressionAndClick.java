package cn.com.cig.adsense.delivery.impl;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.delivery.FixtureAbstractHandler;
import cn.com.cig.adsense.delivery.FixtureHandler;
import cn.com.cig.adsense.utils.Utils;
import cn.com.cig.adsense.utils.date.SystemTimer;
import cn.com.cig.adsense.vo.fix.FixtureDeliveryAttribute;
import cn.com.cig.adsense.vo.fix.Material;
import cn.com.cig.adsense.vo.fix.UserBehavior.MaterialBehavior;

public class FilterMaterialsByImpressionAndClick extends FixtureAbstractHandler
		implements FixtureHandler {

	private static Logger logger = LoggerFactory
			.getLogger(FilterMaterialsByImpressionAndClick.class);
	
	@Override
	public void operator(FixtureDeliveryAttribute da) {
		
		if ((da != null) && (da.getMaterials() != null)
				&& (da.getUserBehaviorMap() != null)) {
			// 根据投放策略筛选素材.
			// 没有impression和click或者userBehaviorStr非法,不需要根据pv,click过滤.
			// 然后根据pv和click确定最终的素材(selected).
			Iterator<Material> i = da.getMaterials().iterator();
			while (i.hasNext()) {
				Material m = i.next();
				// 没有浏览过当前素材(m),保留。
				if (!da.getUserBehaviorMap().containsKey(m.getId())) {
					continue;
				}
				MaterialBehavior mb = da.getUserBehaviorMap().get(m.getId());
				int pv = mb.getImpressionPeriod();
				int click = mb.getClickPeriod();
				long lastClick = mb.getLastClick();
				// lastClick默认为0
				if (lastClick > 0) {
					int daysBetween = Utils.daysBetween(
							SystemTimer.currentTimeMillis(), lastClick);
					// 投放策略2.
					// 以天为单位，Cookie当天点击过1次当前素材后，下次对该cookie投放次权重素材，依次同理，直至默认素材；次日轮回。
					//TODO 次数和天数可配置.
					if ((click >= 1) && (daysBetween <= 0)) {
						i.remove();
						continue;
					}
				}
				long laseImpression = mb.getLastImpression();
				// laseImpression默认为0
				if (laseImpression > 0) {
					int daysBetween = Utils.daysBetween(
							SystemTimer.currentTimeMillis(), laseImpression);
					// 投放策略3.以天为单位，
					// Cookie浏览3次仍未点击当前素材，3次之后则当天对该cookie投放次权重素材，依次同理，直至默认素材；次日轮回。
					//TODO 次数和天数可配置.
					if ((pv >= 3) && (daysBetween <= 0)) {
						i.remove();
					}
				}
			}
		} else {
			logger.warn("DeliveryAttribute is null! DeliveryAttribute:" + da);
		}
		if (getHandler() != null) {
			getHandler().operator(da);
		}

	}

}
