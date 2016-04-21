package cn.com.cig.adsense.vo;

import com.google.common.base.Objects;

/**   
 * @File: Region.java 
 * @Package cn.com.cig.adsense.vo.dync 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年1月27日 下午3:00:14 
 * @version V1.0   
 */
public class Region {
	
	private int province;//省
	private int city;//市
	private int district;//区
	
	public Region(){
		
	}
	public Region(int city){
		this.city=city;
	}
	public Region(int province,int city,int district){
		this.province=province;
		this.city=city;
		this.district=district;
	}
	
	public int getProvince() {
		return province;
	}
	public void setProvince(int province) {
		this.province = province;
	}
	public int getCity() {
		return city;
	}
	public void setCity(int city) {
		this.city = city;
	}
	public int getDistrict() {
		return district;
	}
	public void setDistrict(int district) {
		this.district = district;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("province", province)
				.add("city", city)
				.add("district", district)
				.toString();
	}
}
