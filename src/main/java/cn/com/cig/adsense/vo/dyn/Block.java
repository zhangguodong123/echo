package cn.com.cig.adsense.vo.dyn;

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
public class Block {
	private String pId;
	private String sysPId;
	private Integer csId;
	private String brandName;
	private String csName;
	private String adLink;
	private String mLink;
	private String img;
	private String slogan;
	private Double weight;
	private double price;
	private double referPrice;
	private List<Integer> regionId;
	private List<String> regionName;
	private Map<String,String> custom;
	
	public String getpId() {
		return pId;
	}


	public void setpId(String pId) {
		this.pId = pId;
	}


	public String getSysPId() {
		return sysPId;
	}


	public void setSysPId(String sysPId) {
		this.sysPId = sysPId;
	}


	public Integer getCsId() {
		return csId;
	}


	public void setCsId(Integer csId) {
		this.csId = csId;
	}


	public String getBrandName() {
		return brandName;
	}


	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}


	public String getCsName() {
		return csName;
	}


	public void setCsName(String csName) {
		this.csName = csName;
	}


	public String getAdLink() {
		return adLink;
	}


	public void setAdLink(String adLink) {
		this.adLink = adLink;
	}


	public String getmLink() {
		return mLink;
	}


	public void setmLink(String mLink) {
		this.mLink = mLink;
	}


	public String getImg() {
		return img;
	}


	public void setImg(String img) {
		this.img = img;
	}


	public String getSlogan() {
		return slogan;
	}


	public void setSlogan(String slogan) {
		this.slogan = slogan;
	}

	public Double getWeight() {
		return weight;
	}


	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public double getPrice() {
		return price;
	}


	public void setPrice(double price) {
		this.price = price;
	}


	public double getReferPrice() {
		return referPrice;
	}


	public void setReferPrice(double referPrice) {
		this.referPrice = referPrice;
	}

	public List<Integer> getRegionId() {
		return regionId;
	}


	public void setRegionId(List<Integer> regionId) {
		this.regionId = regionId;
	}


	public List<String> getRegionName() {
		return regionName;
	}


	public void setRegionName(List<String> regionName) {
		this.regionName = regionName;
	}

	public Map<String, String> getCustom() {
		return custom;
	}


	public void setCustom(Map<String, String> custom) {
		this.custom = custom;
	}
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("pId", pId)
				.add("sysPId", sysPId)
				.add("csId", csId)
				.add("brandName", brandName)
				.add("csName", csName)
				.add("adLink", adLink)
				.add("mLink", mLink)
				.add("img", img)
				.add("slogan", slogan)
				.add("weight", weight)
				.add("price", price)
				.add("referPrice", referPrice)
				.add("regionId", regionId)
				.add("regionName", regionName)
				.add("custom", custom)
				.toString();
	}
}
