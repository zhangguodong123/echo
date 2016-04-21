package cn.com.cig.adsense.vo.fix;

import java.util.List;

import com.google.common.base.Objects;

/**   
 * @File: Advertiser.java 
 * @Package cn.com.cig.adsense.vo 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年9月23日 上午11:29:47 
 * @version V1.0   
 */
public class Advertiser {
	private Integer id;
	private String name;
	private long limitVisitorDisplayDay;
	private List<Campaign> directional;//定投广告计划
	private List<Campaign> unDirectional;//定投广告计划
	private List<Campaign> general;//定投广告计划
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
	public long getLimitVisitorDisplayDay() {
		return limitVisitorDisplayDay;
	}
	public void setLimitVisitorDisplayDay(long limitVisitorDisplayDay) {
		this.limitVisitorDisplayDay = limitVisitorDisplayDay;
	}
	public List<Campaign> getDirectional() {
		return directional;
	}
	public void setDirectional(List<Campaign> directional) {
		this.directional = directional;
	}
	public List<Campaign> getUnDirectional() {
		return unDirectional;
	}
	public void setUnDirectional(List<Campaign> unDirectional) {
		this.unDirectional = unDirectional;
	}
	public List<Campaign> getGeneral() {
		return general;
	}
	public void setGeneral(List<Campaign> general) {
		this.general = general;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("id", id)
		.add("name", name)
		.add("directional", directional)
		.add("limitVisitorDisplayDay", limitVisitorDisplayDay)
		.add("unDirectional", unDirectional)
		.add("general", general).toString();
	}
}
