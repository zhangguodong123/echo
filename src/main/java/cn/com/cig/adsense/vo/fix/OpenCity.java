package cn.com.cig.adsense.vo.fix;

import com.google.common.base.Objects;

/**   
 * @File: OpenCity.java 
 * @Package cn.com.cig.adsense.vo 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年5月11日 下午7:59:42 
 * @version V1.0   
 */
public class OpenCity {
	private Integer cityId;
	private Integer provinceId;
	private String cityName;
	private String cityNamePy;
	private Integer isCharge;
	private String cityUrl;
	
	
	
	@Override
	public int hashCode() {
		return Objects.hashCode(cityId,provinceId,cityName,cityNamePy,isCharge,cityUrl);
	}
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof OpenCity){
			final OpenCity other = (OpenCity) obj;
			return Objects.equal(cityId, other.cityId)
					&& Objects.equal(provinceId, other.provinceId)
					&& Objects.equal(cityName, other.cityName)
					&& Objects.equal(cityNamePy, other.cityNamePy)
					&& Objects.equal(isCharge, other.isCharge)
					&& Objects.equal(cityUrl, other.cityUrl);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("cityId", cityId).add("provinceId", provinceId)
				.add("cityName", cityName)
				.add("cityNamePy", cityNamePy)
				.add("isCharge", isCharge)
				.add("cityUrl", cityUrl)
				.toString();
	}
	
	public Integer getCityId() {
		return cityId;
	}
	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}
	public Integer getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(Integer provinceId) {
		this.provinceId = provinceId;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getCityNamePy() {
		return cityNamePy;
	}
	public void setCityNamePy(String cityNamePy) {
		this.cityNamePy = cityNamePy;
	}
	public Integer getIsCharge() {
		return isCharge;
	}
	public void setIsCharge(Integer isCharge) {
		this.isCharge = isCharge;
	}
	public String getCityUrl() {
		return cityUrl;
	}
	public void setCityUrl(String cityUrl) {
		this.cityUrl = cityUrl;
	}
}
