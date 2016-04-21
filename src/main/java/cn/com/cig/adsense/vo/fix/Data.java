package cn.com.cig.adsense.vo.fix;

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
public class Data {
	private String msg;
	private boolean success;
	private List<Structer> data;
	
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public List<Structer> getData() {
		return data;
	}
	public void setData(List<Structer> data) {
		this.data = data;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("msg", msg)
				.add("success", success)
				.add("data", data)
				.toString();
	}
}
