package cn.com.cig.adsense.vo.fix;

import com.google.common.base.Objects;

/*
 * 车型素材(动态广告投放系统使用)
 */
public class YipaiModelMaterial {
	// model id
	private Integer cityId;
	// model name
	private Integer modelId;
	// 图片
	// specification
	// 1
	private String slogan;
	// 2
	private String landingPage;
	// 3
	private String maxPromotionPrice;
	// 5
	private String gift;
	// 6
	private String minPrice;
	// 7
	private String maxPrice;

	@Override
	public int hashCode() {
		return Objects.hashCode(cityId, modelId, slogan, landingPage, maxPromotionPrice,
				gift, minPrice, maxPrice);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof YipaiModelMaterial) {
			final YipaiModelMaterial other = (YipaiModelMaterial) obj;
			return Objects.equal(cityId, other.cityId)
					&& Objects.equal(modelId, other.modelId)
					&& Objects.equal(slogan, other.slogan)
					&& Objects.equal(landingPage, other.landingPage)
					&& Objects.equal(maxPromotionPrice, other.maxPromotionPrice)
					&& Objects.equal(gift, other.gift)
					&& Objects.equal(minPrice, other.minPrice)
					&& Objects.equal(maxPrice, other.maxPrice);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("cityId", cityId).add("modelId", modelId)
				.add("slogan", slogan).add("landingPage", landingPage)
				.add("maxPromotionPrice", maxPromotionPrice).add("gift", gift)
				.add("minPrice", minPrice).add("maxPrice", maxPrice)
				.toString();
	}

	public Integer getCityId() {
		return cityId;
	}

	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}

	public Integer getModelId() {
		return modelId;
	}

	public void setModelId(Integer modelId) {
		this.modelId = modelId;
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

}
