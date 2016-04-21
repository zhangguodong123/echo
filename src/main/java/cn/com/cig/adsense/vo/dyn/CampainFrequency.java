package cn.com.cig.adsense.vo.dyn;

import com.google.common.base.Objects;

/**   
 * @File: CampainFrequency.java 
 * @Package cn.com.cig.adsense.vo.frequency 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年10月21日 下午2:47:40 
 * @version V1.0   
 */
public class CampainFrequency {

	private int id;
	private Long exposeVisitDayLimit;
	private Long exposeVisitHourLimit;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Long getExposeVisitDayLimit() {
		return exposeVisitDayLimit;
	}
	public void setExposeVisitDayLimit(Long exposeVisitDayLimit) {
		this.exposeVisitDayLimit = exposeVisitDayLimit;
	}
	public Long getExposeVisitHourLimit() {
		return exposeVisitHourLimit;
	}
	public void setExposeVisitHourLimit(Long exposeVisitHourLimit) {
		this.exposeVisitHourLimit = exposeVisitHourLimit;
	}
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("id", id).add("exposeVisitDayLimit", exposeVisitDayLimit).add("exposeVisitHourLimit", exposeVisitHourLimit).toString();
	}
}
