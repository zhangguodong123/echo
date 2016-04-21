package cn.com.cig.adsense.vo.fix;

import java.util.List;
import java.util.Map;

import cn.com.cig.adsense.vo.fix.UserBehavior.MaterialBehavior;

import com.google.common.base.Objects;

/*
 * 固定位广告投放
 */
public class FixtureDeliveryAttribute {

	private List<Material> materials;
	private UserStruct userStruct;
	private Map<String, MaterialBehavior> userBehaviorMap;

	@Override
	public int hashCode() {
		return Objects.hashCode(materials, userStruct, userBehaviorMap);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FixtureDeliveryAttribute) {
			final FixtureDeliveryAttribute other = (FixtureDeliveryAttribute) obj;
			return Objects.equal(materials, other.materials)
					&& Objects.equal(userStruct, other.userStruct)
					&& Objects.equal(userBehaviorMap, other.userBehaviorMap);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("materials", materials)
				.add("userStruct", userStruct)
				.add("userBehaviorMap", userBehaviorMap).toString();
	}

	public List<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(List<Material> materials) {
		this.materials = materials;
	}

	public UserStruct getUserStruct() {
		return userStruct;
	}

	public void setUserStruct(UserStruct userStruct) {
		this.userStruct = userStruct;
	}

	public Map<String, MaterialBehavior> getUserBehaviorMap() {
		return userBehaviorMap;
	}

	public void setUserBehaviorMap(Map<String, MaterialBehavior> userBehaviorMap) {
		this.userBehaviorMap = userBehaviorMap;
	}

}
