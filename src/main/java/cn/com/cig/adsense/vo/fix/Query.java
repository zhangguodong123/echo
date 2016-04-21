package cn.com.cig.adsense.vo.fix;

import java.util.Map;

import com.google.common.base.Objects;

/**   
 * @File: Query.java 
 * @Package cn.com.cig.adsense.vo.fix 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月9日 下午6:52:57 
 * @version V1.0   
 */
public class Query {
	private String pubid;
	private Integer pid;
	private String dvid;
	private Integer os;
	private Integer dvtype;
	private Integer nettype;
	private String ip;
	private String ua;
	private String osvs;
	private long ts;
	private String geo;
	private Integer cityid;
	private Map<Integer,Integer> tags;
	
	public String getPubid() {
		return pubid;
	}
	public void setPubid(String pubid) {
		this.pubid = pubid;
	}
	public Integer getPid() {
		return pid;
	}
	public void setPid(Integer pid) {
		this.pid = pid;
	}
	public String getDvid() {
		return dvid;
	}
	public void setDvid(String dvid) {
		this.dvid = dvid;
	}
	public Integer getOs() {
		return os;
	}
	public void setOs(Integer os) {
		this.os = os;
	}
	public Integer getDvtype() {
		return dvtype;
	}
	public void setDvtype(Integer dvtype) {
		this.dvtype = dvtype;
	}
	public Integer getNettype() {
		return nettype;
	}
	public void setNettype(Integer nettype) {
		this.nettype = nettype;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getUa() {
		return ua;
	}
	public void setUa(String ua) {
		this.ua = ua;
	}
	public String getOsvs() {
		return osvs;
	}
	public void setOsvs(String osvs) {
		this.osvs = osvs;
	}
	public long getTs() {
		return ts;
	}
	public void setTs(long ts) {
		this.ts = ts;
	}
	public String getGeo() {
		return geo;
	}
	public void setGeo(String geo) {
		this.geo = geo;
	}
	public Integer getCityid() {
		return cityid;
	}
	public void setCityid(Integer cityid) {
		this.cityid = cityid;
	}
	public Map<Integer, Integer> getTags() {
		return tags;
	}
	public void setTags(Map<Integer, Integer> tags) {
		this.tags = tags;
	}
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("pubid", pubid)
		.add("pid", pid)
		.add("dvid", dvid)
		.add("os", os)
		.add("dvtype", dvtype)
		.add("nettype", nettype)
		.add("nettype", nettype)
		.add("ip", ip)
		.add("ua", ua)
		.add("osvs",osvs)
		.add("ts", ts)
		.add("geo", geo)
		.add("cityid", cityid)
		.add("tags", tags)
		.toString();
	}
}
