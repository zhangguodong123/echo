package cn.com.cig.adsense.vo.fix;

import java.util.List;

import com.google.common.base.Objects;

/**   
 * @File: Feed.java 
 * @Package cn.com.cig.adsense.vo.fix 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月11日 上午11:41:23 
 * @version V1.0   
 */
public class Feed {
	
	private String dvid;
	private List<Poster> result;
	private int status;
	public String getDvid() {
		return dvid;
	}
	public void setDvid(String dvid) {
		this.dvid = dvid;
	}
	public List<Poster> getResult() {
		return result;
	}
	public void setResult(List<Poster> result) {
		this.result = result;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("dvid", dvid)
		.add("result", result)
		.toString();
	}
}
