package cn.com.cig.adsense.vo.fix;

import java.util.List;
import java.util.Map;

import cn.com.cig.adsense.dao.cassandra.UserData;
import cn.com.cig.adsense.vo.fix.UserBehavior.MaterialBehavior;

import com.google.common.base.Objects;

public class DynamicDeliveryAttribute {

	private List<Material> materials;
	private UserData userData;
	private Map<String, MaterialBehavior> userBehaviorMap;

	@Override
	public int hashCode() {
		return Objects.hashCode(materials, userData, userBehaviorMap);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DynamicDeliveryAttribute) {
			final DynamicDeliveryAttribute other = (DynamicDeliveryAttribute) obj;
			return Objects.equal(materials, other.materials)
					&& Objects.equal(userData, other.userData)
					&& Objects.equal(userBehaviorMap, other.userBehaviorMap);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("materials", materials)
				.add("userData", userData)
				.add("userBehaviorMap", userBehaviorMap).toString();
	}

	public List<Material> getMaterials() {
		return materials;
	}

	public void setMaterials(List<Material> materials) {
		this.materials = materials;
	}

	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}

	public Map<String, MaterialBehavior> getUserBehaviorMap() {
		return userBehaviorMap;
	}

	public void setUserBehaviorMap(Map<String, MaterialBehavior> userBehaviorMap) {
		this.userBehaviorMap = userBehaviorMap;
	}

}
