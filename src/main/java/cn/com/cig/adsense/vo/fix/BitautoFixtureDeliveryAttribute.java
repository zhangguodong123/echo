package cn.com.cig.adsense.vo.fix;

import java.util.Collection;
import java.util.Map;

import cn.com.cig.adsense.vo.fix.UserBehavior.MaterialBehavior;

import com.google.common.base.Objects;

/*
 * 固定位广告投放
 */
public class BitautoFixtureDeliveryAttribute {

	private Collection<BitautoMaterial> materials;
	private Map<Integer, Integer> tags;
	private Map<String, MaterialBehavior> userBehaviorMap;

	@Override
	public int hashCode() {
		return Objects.hashCode(materials, tags, userBehaviorMap);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BitautoFixtureDeliveryAttribute) {
			final BitautoFixtureDeliveryAttribute other = (BitautoFixtureDeliveryAttribute) obj;
			return Objects.equal(materials, other.materials)
					&& Objects.equal(tags, other.tags)
					&& Objects.equal(userBehaviorMap, other.userBehaviorMap);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("materials", materials)
				.add("tags", tags)
				.add("userBehaviorMap", userBehaviorMap).toString();
	}

	public Collection<BitautoMaterial> getMaterials() {
		return materials;
	}

	public void setMaterials(Collection<BitautoMaterial> materials) {
		this.materials = materials;
	}

	public Map<Integer, Integer> getTags() {
		return tags;
	}

	public void setTags(Map<Integer, Integer> tags) {
		this.tags = tags;
	}

	public Map<String, MaterialBehavior> getUserBehaviorMap() {
		return userBehaviorMap;
	}

	public void setUserBehaviorMap(Map<String, MaterialBehavior> userBehaviorMap) {
		this.userBehaviorMap = userBehaviorMap;
	}

}
