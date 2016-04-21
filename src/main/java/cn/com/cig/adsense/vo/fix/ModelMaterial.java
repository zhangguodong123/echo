package cn.com.cig.adsense.vo.fix;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

/*
 * 车型素材(动态广告投放系统使用)
 */
public class ModelMaterial {

	private Integer modelId;
	private String name;
	private Integer carYear;
	private String pic210X140;
	private String pic90x60;
	private String slogan;
	private String landingPage;
	// 关联地域
	private Integer region;
	private String maxPromotionPrice;
	private String gift;
	private String minPrice;
	private String maxPrice;
	private String impressionMonitorCode;
	private String clickMonitorCode;
	private Collection<Integer> matchedTags;
	private Collection<Integer> matchedCompetitiveModels;
	//价格
	private String price;
	private String referPrice;
	private Integer groupoNum;//团购新增属性
	private Date groupEndTime;//团购新增属性
	
	private Integer endDay;
	
	private String prefer_info;//活动的优惠信息
	private Integer signupNumber;//活动参与人数
	
	private String showcarid;//2015-03-13 新增车款字段
	private String showyear; //2015-03-13 新增车型年份字段
	
	private String brandName;//2015-04-22 新增活动车系字段
	
	private String mUrl; //2015-05-11新增手机地址字段
	
	// 复制之后才可以修改.
	private boolean copied = false;
	
	private Integer isWarranty;
	
	//weight applyCount monthPay
	private double weight;    //权重
	private int applyCount;//申请人数
	private String monthPay;
	
	private Integer echoRegion;
	
	private List<Integer> cityIds;
	private List<String> origCityIds;
	private boolean cityFlag=true;
	
	private boolean isPromotion;
	private Map<String,String> custom;
	
	@Override
	public int hashCode() {
		return Objects.hashCode(modelId, name,carYear,pic210X140,pic90x60, slogan,
				landingPage, region, maxPromotionPrice, gift, minPrice, 
				maxPrice, matchedTags, matchedCompetitiveModels,
				price,referPrice,groupoNum,groupEndTime,endDay,prefer_info,
				signupNumber,showcarid,showyear,brandName,mUrl,
				copied,isWarranty,weight,applyCount,monthPay,
				echoRegion,cityIds,cityFlag,origCityIds,isPromotion,custom);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ModelMaterial) {
			final ModelMaterial other = (ModelMaterial) obj;
			return Objects.equal(modelId, other.modelId)
					&& Objects.equal(name, other.name)
					&& Objects.equal(carYear, other.carYear)
					&& Objects.equal(pic210X140, other.pic210X140)
					&& Objects.equal(pic90x60, other.pic90x60)
					&& Objects.equal(slogan, other.slogan)
					&& Objects.equal(landingPage, other.landingPage)
					&& Objects.equal(region, other.region)
					&& Objects.equal(maxPromotionPrice, other.maxPromotionPrice)
					&& Objects.equal(gift, other.gift)
					&& Objects.equal(minPrice, other.minPrice)
					&& Objects.equal(maxPrice, other.maxPrice)
					&& Objects.equal(price, other.price)
					&& Objects.equal(referPrice, other.referPrice)
					&& Objects.equal(groupoNum, other.groupoNum)
					&& Objects.equal(groupEndTime, other.groupEndTime)
					&& Objects.equal(endDay, other.endDay)
					&& Objects.equal(prefer_info, other.prefer_info)
					&& Objects.equal(signupNumber, other.signupNumber)
					&& Objects.equal(showcarid, other.showcarid)
					&& Objects.equal(brandName, other.brandName)
					&& Objects.equal(showyear, other.showyear)
					&& Objects.equal(mUrl, other.mUrl)
					&& Objects.equal(isWarranty, other.isWarranty)
					&& Objects.equal(copied, other.copied)
					&& Objects.equal(weight, other.weight)
					&& Objects.equal(applyCount, other.applyCount)
					&& Objects.equal(echoRegion, other.echoRegion)
					&& Objects.equal(monthPay, other.monthPay)
					&& Objects.equal(cityIds, other.cityIds)
					&& Objects.equal(cityFlag, other.cityFlag)
					&& Objects.equal(origCityIds, other.origCityIds)
					&& Objects.equal(isPromotion, other.isPromotion)
					&& Objects.equal(custom, other.custom)
					;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return Objects
				.toStringHelper(this)
				.add("modelId", modelId)
				.add("name", name)
				.add("carYear", carYear)
				.add("pic210X140", pic210X140)
				.add("pic90x60", pic90x60)
				.add("slogan", slogan).add("landingPage", landingPage)
				.add("region", region)
				.add("maxPromotionPrice", maxPromotionPrice).add("gift", gift)
				.add("minPrice", minPrice).add("maxPrice", maxPrice)
				.add("price", price)
				.add("referPrice", referPrice)
				.add("groupoNum", groupoNum)
				.add("groupEndTime", groupEndTime)
				.add("endDay", endDay)
				.add("prefer_info", prefer_info)
				.add("signupNumber", signupNumber)
				.add("showcarid", showcarid)
				.add("showyear", showyear)
				.add("brandName", brandName)
				.add("mUrl", mUrl)
				.add("copied", copied)
				.add("isWarranty", isWarranty)
				.add("weight", weight)
				.add("applyCount", applyCount)
				.add("monthPay", monthPay)
				.add("echoRegion", echoRegion)
				.add("cityIds", cityIds)
				.add("cityFlag", cityFlag)
				.add("origCityIds", origCityIds)
				.add("isPromotion", isPromotion)
				.add("custom", custom)
				.toString();
	}
	
	public Integer getModelId() {
		return modelId;
	}

	public void setModelId(Integer modelId) {
		this.modelId = modelId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getCarYear() {
		return carYear;
	}

	public void setCarYear(Integer carYear) {
		this.carYear = carYear;
	}

	public String getPic90x60() {
		return pic90x60;
	}

	public void setPic90x60(String pic90x60) {
		this.pic90x60 = pic90x60;
	}

	public String getSlogan() {
		return slogan;
	}

	public void setSlogan(String slogan) {
		this.slogan = slogan;
	}

	public String getLandingPage() {
		return landingPage;
	}

	public void setLandingPage(String landingPage) {
		this.landingPage = landingPage;
	}

	public Integer getRegion() {
		return region;
	}

	public void setRegion(Integer region) {
		this.region = region;
	}

	public String getMaxPromotionPrice() {
		return maxPromotionPrice;
	}

	public void setMaxPromotionPrice(String maxPromotionPrice) {
		this.maxPromotionPrice = maxPromotionPrice;
	}

	public String getGift() {
		return gift;
	}

	public void setGift(String gift) {
		this.gift = gift;
	}

	public String getMinPrice() {
		return minPrice;
	}

	public void setMinPrice(String minPrice) {
		this.minPrice = minPrice;
	}

	public String getMaxPrice() {
		return maxPrice;
	}

	public void setMaxPrice(String maxPrice) {
		this.maxPrice = maxPrice;
	}

	public String getImpressionMonitorCode() {
		return impressionMonitorCode;
	}

	public void setImpressionMonitorCode(String impressionMonitorCode) {
		this.impressionMonitorCode = impressionMonitorCode;
	}

	public String getClickMonitorCode() {
		return clickMonitorCode;
	}

	public void setClickMonitorCode(String clickMonitorCode) {
		this.clickMonitorCode = clickMonitorCode;
	}

	public Collection<Integer> getMatchedTags() {
		return matchedTags;
	}

	public void setMatchedTags(Collection<Integer> matchedTags) {
		this.matchedTags = matchedTags;
	}

	public String getPic210X140() {
		return pic210X140;
	}

	public void setPic210X140(String pic210x140) {
		pic210X140 = pic210x140;
	}

	public Collection<Integer> getMatchedCompetitiveModels() {
		return matchedCompetitiveModels;
	}

	public void setMatchedCompetitiveModels(Collection<Integer> matchedCompetitiveModels) {
		this.matchedCompetitiveModels = matchedCompetitiveModels;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}
	
	public String getReferPrice() {
		return referPrice;
	}

	public void setReferPrice(String referPrice) {
		this.referPrice = referPrice;
	}

	public Integer getGroupoNum() {
		return groupoNum;
	}

	public void setGroupoNum(Integer groupoNum) {
		this.groupoNum = groupoNum;
	}

	public Date getGroupEndTime() {
		return groupEndTime;
	}

	public void setGroupEndTime(Date groupEndTime) {
		this.groupEndTime = groupEndTime;
	}

	public Integer getEndDay() {
		return endDay;
	}

	public void setEndDay(Integer endDay) {
		this.endDay = endDay;
	}

	public String getShowcarid() {
		return showcarid;
	}

	public void setShowcarid(String showcarid) {
		this.showcarid = showcarid;
	}

	public String getShowyear() {
		return showyear;
	}

	public void setShowyear(String showyear) {
		this.showyear = showyear;
	}

	public String getPrefer_info() {
		return prefer_info;
	}

	public void setPrefer_info(String prefer_info) {
		this.prefer_info = prefer_info;
	}

	public Integer getSignupNumber() {
		return signupNumber;
	}

	public void setSignupNumber(Integer signupNumber) {
		this.signupNumber = signupNumber;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public String getmUrl() {
		return mUrl;
	}

	public void setmUrl(String mUrl) {
		this.mUrl = mUrl;
	}

	public boolean isCopied() {
		return copied;
	}

	public void setCopied(boolean copied) {
		this.copied = copied;
	}

	public Integer getIsWarranty() {
		return isWarranty;
	}

	public void setIsWarranty(Integer isWarranty) {
		this.isWarranty = isWarranty;
	}
	
	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public int getApplyCount() {
		return applyCount;
	}

	public void setApplyCount(int applyCount) {
		this.applyCount = applyCount;
	}

	public String getMonthPay() {
		return monthPay;
	}

	public void setMonthPay(String monthPay) {
		this.monthPay = monthPay;
	}

	public Integer getEchoRegion() {
		return echoRegion;
	}

	public void setEchoRegion(Integer echoRegion) {
		this.echoRegion = echoRegion;
	}
	public boolean isCityFlag() {
		return cityFlag;
	}
	
	public void setCityFlag(boolean cityFlag) {
		this.cityFlag = cityFlag;
	}
	

	public List<Integer> getCityIds() {
		return cityIds;
	}

	public void setCityIds(List<Integer> cityIds) {
		this.cityIds = cityIds;
	}
	public List<String> getOrigCityIds() {
		return origCityIds;
	}

	public void setOrigCityIds(List<String> origCityIds) {
		this.origCityIds = origCityIds;
	}
	
	public boolean isPromotion() {
		return isPromotion;
	}

	public void setPromotion(boolean isPromotion) {
		this.isPromotion = isPromotion;
	}
	public Map<String, String> getCustom() {
		return custom;
	}


	public void setCustom(Map<String, String> custom) {
		this.custom = custom;
	}
}
