package cn.com.cig.adsense.vo.fix;

import java.util.List;
import java.util.Map;
import com.google.common.base.Objects;

/**   
 * @File: Structer.java 
 * @Package org.bita.standard.Structer 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年12月10日 下午4:50:03 
 * @version V1.0   
 */
public class Structer {
	private String PId;
	private String AdLink;
	private String Slogan;
	private String Img;
	private Integer CsId;
	private String CsName;
	private String BrandName;
	private List<String> RegionId;
	private String RegionName;
	private double Price;
	private double ReferPrice;
	private Integer SignupNumber;
	private String StartTime;
	private String EndTime;
	private String MLink;
	private Integer IsHot;
	private Integer Weight;
	private Map<String,String> Custom;
	
	public String getPId() {
		return PId;
	}

	public void setPId(String pId) {
		PId = pId;
	}

	public String getAdLink() {
		return AdLink;
	}

	public void setAdLink(String adLink) {
		AdLink = adLink;
	}

	public String getSlogan() {
		return Slogan;
	}

	public void setSlogan(String slogan) {
		Slogan = slogan;
	}

	public String getImg() {
		return Img;
	}

	public void setImg(String img) {
		Img = img;
	}

	public Integer getCsId() {
		return CsId;
	}

	public void setCsId(Integer csId) {
		CsId = csId;
	}

	public String getCsName() {
		return CsName;
	}

	public void setCsName(String csName) {
		CsName = csName;
	}

	public String getBrandName() {
		return BrandName;
	}

	public void setBrandName(String brandName) {
		BrandName = brandName;
	}
	public List<String> getRegionId() {
		return RegionId;
	}

	public void setRegionId(List<String> regionId) {
		RegionId = regionId;
	}
	public String getRegionName() {
		return RegionName;
	}

	public void setRegionName(String regionName) {
		RegionName = regionName;
	}

	public double getPrice() {
		return Price;
	}

	public void setPrice(double price) {
		Price = price;
	}

	public double getReferPrice() {
		return ReferPrice;
	}

	public void setReferPrice(double referPrice) {
		ReferPrice = referPrice;
	}

	public Integer getSignupNumber() {
		return SignupNumber;
	}

	public void setSignupNumber(Integer signupNumber) {
		SignupNumber = signupNumber;
	}

	public String getStartTime() {
		return StartTime;
	}

	public void setStartTime(String startTime) {
		StartTime = startTime;
	}

	public String getEndTime() {
		return EndTime;
	}

	public void setEndTime(String endTime) {
		EndTime = endTime;
	}

	public String getMLink() {
		return MLink;
	}

	public void setMLink(String mLink) {
		MLink = mLink;
	}

	public Integer getIsHot() {
		return IsHot;
	}

	public void setIsHot(Integer isHot) {
		IsHot = isHot;
	}

	public Integer getWeight() {
		return Weight;
	}

	public void setWeight(Integer weight) {
		Weight = weight;
	}

	public Map<String, String> getCustom() {
		return Custom;
	}

	public void setCustom(Map<String, String> custom) {
		Custom = custom;
	}
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("PId", PId)
				.add("AdLink", AdLink)
				.add("Slogan", Slogan)
				.add("Img", Img)
				.add("CsId", CsId)
				.add("CsName", CsName)
				.add("BrandName", BrandName)
				.add("RegionId", RegionId)
				.add("RegionName", RegionName)
				.add("Price", Price)
				.add("ReferPrice", ReferPrice)
				.add("SignupNumber", SignupNumber)
				.add("StartTime", StartTime)
				.add("EndTime", EndTime)
				.add("MLink", MLink)
				.add("IsHot", IsHot)
				.add("Weight", Weight)
				.add("Custom", Custom)
				.toString();
	}
}
