package cn.com.cig.adsense.vo.fix;

import java.util.List;

import com.google.common.base.Objects;

/*
 * 素材(固定位广告投放系统使用)
 */
public class Material {

	private String id;
	private String name;
	// 1:默认物料
	private Integer type;
	// 关联广告位id
	private String positionId;
	private Integer audienceId;
	// 排期
//	private Date startDate;
//	private Date endDate;
	// CDN URL
	private String url;
	private String landingPage;
	// 关联地域
	private List<Integer> regions;
//	private List<Integer> citys;
	// 关联车型
	private List<Integer> models;
	// 备注
	private String text;
	private Integer met_type;
	private Integer width;
	private Integer height;
	
	private List<Integer> matchedTags;

	@Override
	public int hashCode() {
		return Objects.hashCode(id, name, type, positionId, url, landingPage,
				regions, models, text, met_type, width, height, matchedTags);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Material) {
			final Material other = (Material) obj;
			return Objects.equal(id, other.id)
					&& Objects.equal(name, other.name)
					&& Objects.equal(type, other.type)
					&& Objects.equal(positionId, other.positionId)
					&& Objects.equal(audienceId, other.audienceId)
					&& Objects.equal(url, other.url)
					&& Objects.equal(landingPage, other.landingPage)
					&& Objects.equal(regions, other.regions)
					&& Objects.equal(models, other.models)
					&& Objects.equal(text, other.text)
					&& Objects.equal(met_type, other.met_type)
					&& Objects.equal(width, other.width)
					&& Objects.equal(height, other.height)
					&& Objects.equal(matchedTags, other.matchedTags);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("id", id).add("name", name)
				.add("type", type).add("positionId", positionId)
				.add("audienceId", audienceId).add("url", url)
				.add("landingPage", landingPage).add("regions", regions)
				.add("models", models).add("text", text)
				.add("met_type", met_type).add("width", width)
				.add("height", height).add("matchedTags", matchedTags).toString();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getPositionId() {
		return positionId;
	}

	public void setPositionId(String positionId) {
		this.positionId = positionId;
	}
	
	public Integer getAudienceId() {
		return audienceId;
	}

	public void setAudienceId(Integer audienceId) {
		this.audienceId = audienceId;
	}

//	public Date getStartDate() {
//		return startDate;
//	}
//
//	public void setStartDate(Date startDate) {
//		this.startDate = startDate;
//	}
//
//	public Date getEndDate() {
//		return endDate;
//	}
//
//	public void setEndDate(Date endDate) {
//		this.endDate = endDate;
//	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLandingPage() {
		return landingPage;
	}

	public void setLandingPage(String landingPage) {
		this.landingPage = landingPage;
	}

	public List<Integer> getRegions() {
		return regions;
	}

	public void setRegions(List<Integer> regions) {
		this.regions = regions;
	}

//	public List<Integer> getCitys() {
//		return citys;
//	}
//
//	public void setCitys(List<Integer> citys) {
//		this.citys = citys;
//	}

	public List<Integer> getModels() {
		return models;
	}

	public void setModels(List<Integer> models) {
		this.models = models;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Integer getMet_type() {
		return met_type;
	}

	public void setMet_type(Integer met_type) {
		this.met_type = met_type;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public List<Integer> getMatchedTags() {
		return matchedTags;
	}

	public void setMatchedTags(List<Integer> matchedTags) {
		this.matchedTags = matchedTags;
	}

}
