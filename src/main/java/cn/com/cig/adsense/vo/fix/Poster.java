package cn.com.cig.adsense.vo.fix;

import com.google.common.base.Objects;
import com.google.gson.annotations.SerializedName;

/**   
 * @File: Poster.java 
 * @Package cn.com.cig.adsense.vo.fix 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月11日 上午11:50:17 
 * @version V1.0   
 */
public class Poster {
	@SerializedName("Newsid")
	private String newSid;
	
	@SerializedName("Type")
	private String type;
	
	@SerializedName("PicUrl")
	private String picUrl;
	
	@SerializedName("Title")
	private String title;
	
	@SerializedName("URL")
	private String url;
	
	public String getNewSid() {
		return newSid;
	}
	public void setNewSid(String newSid) {
		this.newSid = newSid;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("newSid", newSid)
		.add("type", type)
		.add("picUrl", picUrl)
		.add("title", title)
		.add("url", url)
		.toString();
	}
}
