package cn.com.cig.adsense.vo.dyn;

import java.util.List;

import com.google.common.base.Objects;

/**   
 * @File: Data.java 
 * @Package org.bitauto.striker.vo 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年12月23日 下午3:56:55 
 * @version V1.0   
 */
public class Product {
	private String msg;
	private String accid;
	private List<Feed> feeds;
	private boolean success;
	
	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}


	public String getAccid() {
		return accid;
	}


	public void setAccid(String accid) {
		this.accid = accid;
	}


	public List<Feed> getFeeds() {
		return feeds;
	}


	public void setFeeds(List<Feed> feeds) {
		this.feeds = feeds;
	}


	public boolean isSuccess() {
		return success;
	}


	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("msg", msg)
				.add("accid", accid)
				.add("feeds", feeds)
				.add("success", success)
				.toString();
	}
}
