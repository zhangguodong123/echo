package cn.com.cig.adsense.vo.fix;

import java.util.List;

import cn.com.cig.adsense.vo.PlatefromEnum;

import com.google.common.base.Objects;

public class Position {
	// place
	private String id;
	private String name;
	private PositionTypeEnum type;  // 0 图片, 1 文章, 2 flash, 3 视频, 4图片\flash, 5图文
	private int titleMinNum;
	private int titleMaxNum;
	private int attribute;          //属性字段 0 信息流，默认信息流

	// media
	private String mediaId;
	private String mediaName;
	private PlatefromEnum mediaType;// 0 PC网站、1 移动网站、2 APP
	private OsTypeEnum osType;      // 0 IOS、1 Android
	private List<String> middle;   // 1 手机、2 平板。多选用逗号相隔如:1,2
	private DockingEnum docking;    // 0 API、1 SDK
	private String guid;            // app guid全球唯一标示，pc/wap 默认0

	// channel
	private String channelId;
	private String channelName;

	@Override
	public int hashCode() {
		return Objects.hashCode(id, name, type, titleMinNum, titleMaxNum,
				attribute, mediaId, mediaName, mediaType, osType,
				middle, docking, guid, channelId, channelName);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Position) {
			final Position other = (Position) obj;
			return Objects.equal(id, other.id)
					&& Objects.equal(name, other.name)
					&& Objects.equal(type, other.type)
					&& Objects.equal(titleMinNum, other.titleMinNum)
					&& Objects.equal(titleMaxNum, other.titleMaxNum)
					&& Objects.equal(attribute, other.attribute)
					&& Objects.equal(mediaId, other.mediaId)
					&& Objects.equal(mediaName, other.mediaName)
					&& Objects.equal(mediaType, other.mediaType)
					&& Objects.equal(osType, other.osType)
					&& Objects.equal(middle, other.middle)
					&& Objects.equal(docking, other.docking)
					&& Objects.equal(guid, other.guid)
					&& Objects.equal(channelId, other.channelId)
					&& Objects.equal(channelName, other.channelName);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("id", id)
				.add("name", name)
				.add("type", type)
				.add("titleMinNum", titleMinNum)
				.add("titleMaxNum", titleMaxNum)
				.add("attribute", attribute)
				.add("mediaId", mediaId)
				.add("mediaName", mediaName)
				.add("mediaType", mediaType)
				.add("osType", osType)
				.add("middle", middle)
				.add("docking", docking)
				.add("guid", guid)
				.add("channelId", channelId)
				.add("channelName", channelName).toString();
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

	public PositionTypeEnum getType() {
		return type;
	}

	public void setType(PositionTypeEnum type) {
		this.type = type;
	}

	public int getTitleMinNum() {
		return titleMinNum;
	}

	public void setTitleMinNum(int titleMinNum) {
		this.titleMinNum = titleMinNum;
	}

	public int getTitleMaxNum() {
		return titleMaxNum;
	}

	public void setTitleMaxNum(int titleMaxNum) {
		this.titleMaxNum = titleMaxNum;
	}

	public int getAttribute() {
		return attribute;
	}

	public void setAttribute(int attribute) {
		this.attribute = attribute;
	}

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public String getMediaName() {
		return mediaName;
	}

	public void setMediaName(String mediaName) {
		this.mediaName = mediaName;
	}

	public PlatefromEnum getMediaType() {
		return mediaType;
	}

	public void setMediaType(PlatefromEnum mediaType) {
		this.mediaType = mediaType;
	}

	public OsTypeEnum getOsType() {
		return osType;
	}

	public void setOsType(OsTypeEnum osType) {
		this.osType = osType;
	}

	public List<String> getMiddle() {
		return middle;
	}

	public void setMiddle(List<String> middle) {
		this.middle = middle;
	}
	
	public DockingEnum getDocking() {
		return docking;
	}

	public void setDocking(DockingEnum docking) {
		this.docking = docking;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getChannelName() {
		return channelName;
	}

	public void setChannelName(String channelName) {
		this.channelName = channelName;
	}
}
