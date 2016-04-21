package cn.com.cig.adsense.vo.dyn;

import java.util.List;

import com.google.common.base.Objects;

/**   
 * @File: Feed.java 
 * @Package org.bitauto.striker.product_lib 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年1月25日 上午11:38:31 
 * @version V1.0   
 */
public class Feed {
	private Integer feedid;
	private List<Block> data;
	public Integer getFeedid() {
		return feedid;
	}
	public void setFeedid(Integer feedid) {
		this.feedid = feedid;
	}
	public List<Block> getData() {
		return data;
	}
	public void setData(List<Block> data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("feedid", feedid)
				.add("data", data)
				.toString();
	}
}
