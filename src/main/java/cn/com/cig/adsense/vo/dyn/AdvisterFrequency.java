package cn.com.cig.adsense.vo.dyn;

import java.util.List;

import com.google.common.base.Objects;

/**   
 * @File: AdvisterFrequency.java 
 * @Package cn.com.cig.adsense.vo.frequency 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年10月21日 下午2:47:27 
 * @version V1.0   
 */
public class AdvisterFrequency {
	private int id;
	private Long limitVisitorDisplayDay;
	private List<CampainFrequency> campains;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Long getLimitVisitorDisplayDay() {
		return limitVisitorDisplayDay;
	}
	public void setLimitVisitorDisplayDay(Long limitVisitorDisplayDay) {
		this.limitVisitorDisplayDay = limitVisitorDisplayDay;
	}
	public List<CampainFrequency> getCampains() {
		return campains;
	}
	public void setCampains(List<CampainFrequency> campains) {
		this.campains = campains;
	}
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("id", id).add("limitVisitorDisplayDay", limitVisitorDisplayDay).add("campains", campains.toString()).toString();
	}
}
