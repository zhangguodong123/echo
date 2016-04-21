package cn.com.cig.adsense.vo.fix;

import java.util.Collection;

import com.google.common.base.Objects;

public class BitautoMaterial {

	private Integer id;
	
	private String name;
	
	private MaterialType matType;
	
	private int width;
	
	private int height;
	
	private String fileName;
	
	private String creative;
	
	private String url;
	
	private String text;
	
	private String linkUrl;
	
	private int isSync;
	
	private int isDel;
	
	private String  creator;
	
	private int createtime;
	
	private int campaignId;
	private int advisterId;
	
	private Collection<Integer> regions;
	
	private Collection<Integer> models;
	
	private Collection<Integer> places;
	
	private Collection<Integer> matchedTags;
	private Collection<Integer> matchedCompetitiveModels;
	
	private boolean isContentTarget = false;
	private Collection<Integer> competitiveModels;
	private boolean isRegionTarget = false;
	
	private Integer audienceId;//新增广告组ID
	
	// 文本投放
	private String wapLinkUrl;
	private String title;
	private String description;
	
	//频次限定
	private Long advLimitVisitorDisplayDay;
	private Long campExposeVisitDayLimit;
	private Long campExposeVisitHourLimit;
	
	// 复制之后才可以修改.
	private boolean copied = false;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MaterialType getMatType() {
		return matType;
	}

	public void setMatType(MaterialType matType) {
		this.matType = matType;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getCreative() {
		return creative;
	}

	public void setCreative(String creative) {
		this.creative = creative;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getLinkUrl() {
		return linkUrl;
	}

	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}

	public int getIsSync() {
		return isSync;
	}

	public void setIsSync(int isSync) {
		this.isSync = isSync;
	}

	public int getIsDel() {
		return isDel;
	}

	public void setIsDel(int isDel) {
		this.isDel = isDel;
	}
	
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public int getCreatetime() {
		return createtime;
	}

	public void setCreatetime(int createtime) {
		this.createtime = createtime;
	}

	public int getCampaignId() {
		return campaignId;
	}

	public void setCampaignId(int campaignId) {
		this.campaignId = campaignId;
	}

	public Collection<Integer> getRegions() {
		return regions;
	}

	public void setRegions(Collection<Integer> regions) {
		this.regions = regions;
	}

	public Collection<Integer> getModels() {
		return models;
	}

	public void setModels(Collection<Integer> models) {
		this.models = models;
	}

	public Collection<Integer> getPlaces() {
		return places;
	}

	public void setPlaces(Collection<Integer> places) {
		this.places = places;
	}

	public Collection<Integer> getMatchedTags() {
		return matchedTags;
	}

	public void setMatchedTags(Collection<Integer> matchedTags) {
		this.matchedTags = matchedTags;
	}

	public Boolean isContentTarget() {
		return isContentTarget;
	}

	public void setContentTarget(boolean isContentTarget) {
		this.isContentTarget = isContentTarget;
	}

	public Collection<Integer> getCompetitiveModels() {
		return competitiveModels;
	}

	public void setCompetitiveModels(Collection<Integer> competitiveModels) {
		this.competitiveModels = competitiveModels;
	}

	public Collection<Integer> getMatchedCompetitiveModels() {
		return matchedCompetitiveModels;
	}

	public void setMatchedCompetitiveModels(Collection<Integer> matchedCompetitiveModels) {
		this.matchedCompetitiveModels = matchedCompetitiveModels;
	}

	public boolean isRegionTarget() {
		return isRegionTarget;
	}

	public void setRegionTarget(boolean isRegionTarget) {
		this.isRegionTarget = isRegionTarget;
	}
	
	public Integer getAudienceId() {
		return audienceId;
	}

	public void setAudienceId(Integer audienceId) {
		this.audienceId = audienceId;
	}

	public String getWapLinkUrl() {
		return wapLinkUrl;
	}

	public void setWapLinkUrl(String wapLinkUrl) {
		this.wapLinkUrl = wapLinkUrl;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCopied() {
		return copied;
	}

	public void setCopied(boolean copied) {
		this.copied = copied;
	}
	
	
	public int getAdvisterId() {
		return advisterId;
	}

	public void setAdvisterId(int advisterId) {
		this.advisterId = advisterId;
	}

	public Long getAdvLimitVisitorDisplayDay() {
		return advLimitVisitorDisplayDay;
	}

	public void setAdvLimitVisitorDisplayDay(Long advLimitVisitorDisplayDay) {
		this.advLimitVisitorDisplayDay = advLimitVisitorDisplayDay;
	}

	public Long getCampExposeVisitDayLimit() {
		return campExposeVisitDayLimit;
	}

	public void setCampExposeVisitDayLimit(Long campExposeVisitDayLimit) {
		this.campExposeVisitDayLimit = campExposeVisitDayLimit;
	}

	public Long getCampExposeVisitHourLimit() {
		return campExposeVisitHourLimit;
	}

	public void setCampExposeVisitHourLimit(Long campExposeVisitHourLimit) {
		this.campExposeVisitHourLimit = campExposeVisitHourLimit;
	}
	

	@Override
	public int hashCode() {
		return Objects.hashCode(id, name, matType, width, height, fileName, creative,
				url, text, linkUrl, isSync, isDel, creator, createtime,
				campaignId,advisterId,regions, models, places, audienceId, wapLinkUrl, 
				title, description,advLimitVisitorDisplayDay,campExposeVisitDayLimit,campExposeVisitHourLimit);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BitautoMaterial) {
			final BitautoMaterial other = (BitautoMaterial) obj;
			return Objects.equal(id, other.id)
					&& Objects.equal(name, other.name)
					&& Objects.equal(matType, other.matType)
					&& Objects.equal(width, other.width)
					&& Objects.equal(height, other.height)
					&& Objects.equal(fileName, other.fileName)
					&& Objects.equal(creative, other.creative)
					&& Objects.equal(url, other.url)
					&& Objects.equal(text, other.text)
					&& Objects.equal(linkUrl, other.linkUrl)
					&& Objects.equal(isSync, other.isSync)
					&& Objects.equal(isDel, other.isDel)
					&& Objects.equal(creator, other.creator)
					&& Objects.equal(createtime, other.createtime)
					&& Objects.equal(campaignId, other.campaignId)
					&& Objects.equal(advisterId, other.advisterId)
					&& Objects.equal(regions, other.regions)
					&& Objects.equal(models, other.models)
					&& Objects.equal(places, other.places)
					&& Objects.equal(audienceId, other.audienceId)
					&& Objects.equal(wapLinkUrl, other.wapLinkUrl)
					&& Objects.equal(title, other.title)
					&& Objects.equal(description, other.description)
					&& Objects.equal(copied, other.copied)
					&& Objects.equal(advLimitVisitorDisplayDay, other.advLimitVisitorDisplayDay)
					&& Objects.equal(campExposeVisitDayLimit, other.campExposeVisitDayLimit)
					&& Objects.equal(campExposeVisitHourLimit, other.campExposeVisitHourLimit);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return Objects
				.toStringHelper(this)
				.add("id", id)
				.add("name", name)
				.add("matType", matType)
				.add("width", width)
				.add("height", height)
				.add("fileName", fileName)
				.add("creative", creative)
				.add("url", url)
				.add("text", text)
				.add("linkUrl", linkUrl).add("isSync", isSync)
				.add("isDel", isDel).add("creator", creator)
				.add("createtime", createtime)
				.add("campaignId", campaignId)
				.add("advisterId", advisterId)
				.add("regions", regions)
				.add("models", models)
				.add("places", places)
				.add("matchedTags", matchedTags)
				.add("isContentTarget", isContentTarget)
				.add("competitiveModels", competitiveModels)
				.add("matchedCompetitiveModels", matchedCompetitiveModels)
				.add("isRegionTarget", isRegionTarget)
				.add("audienceId", audienceId)
				.add("wapLinkUrl", wapLinkUrl)
				.add("title", title)
				.add("description", description)
				.add("copied", copied)
				.add("advLimitVisitorDisplayDay", advLimitVisitorDisplayDay)
				.add("campExposeVisitDayLimit", campExposeVisitDayLimit)
				.add("campExposeVisitHourLimit", campExposeVisitHourLimit)
				.toString();
	}
	
	
}
