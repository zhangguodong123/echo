package cn.com.cig.adsense.vo.fix;
import com.google.common.base.Objects;
/**   
 * @File: CityMapping.java 
 * @Package cn.com.cig.adsense.vo 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年5月11日 下午8:47:24 
 * @version V1.0   
 */
public class CityMapping {
	private Integer cityId;
	private Integer privinceId;
	private Integer rewriteCityId;
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return Objects.hashCode(cityId,privinceId,rewriteCityId);
	}
	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		if(obj instanceof CityMapping){
			final CityMapping other = (CityMapping) obj;
			return Objects.equal(cityId, other.cityId)
					&& Objects.equal(privinceId, other.privinceId)
					&& Objects.equal(rewriteCityId, other.rewriteCityId);
		}
		return false;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return Objects.toStringHelper(this)
				.add("cityId", cityId)
				.add("privinceId", privinceId)
				.add("rewriteCityId", rewriteCityId)
				.toString();
	}
	public Integer getCityId() {
		return cityId;
	}
	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}
	public Integer getPrivinceId() {
		return privinceId;
	}
	public void setPrivinceId(Integer privinceId) {
		this.privinceId = privinceId;
	}
	public Integer getRewriteCityId() {
		return rewriteCityId;
	}
	public void setRewriteCityId(Integer rewriteCityId) {
		this.rewriteCityId = rewriteCityId;
	}
}
