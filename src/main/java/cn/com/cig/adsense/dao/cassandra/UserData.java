package cn.com.cig.adsense.dao.cassandra;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;

import java.util.Map;
/**
 * 用户数据表
 * @author zgd
 *
 */
public class UserData implements Serializable {

	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public Integer getUa() {
		return ua;
	}
	public void setUa(Integer ua) {
		this.ua = ua;
	}
	public Map<Integer, ByteBuffer> getModels() {
		return models;
	}
	public void setModels(Map<Integer, ByteBuffer> models) {
		this.models = models;
	}
	public Map<Integer, ByteBuffer> getSearchs() {
		return searchs;
	}
	public void setSearchs(Map<Integer, ByteBuffer> searchs) {
		this.searchs = searchs;
	}
	public String getEcheId() {
		return echeId;
	}
	public void setEcheId(String echeId) {
		this.echeId = echeId;
	}
	public Date getCreated_time() {
		return created_time;
	}
	public void setCreated_time(Date created_time) {
		this.created_time = created_time;
	}
	public Date getLast_visited() {
		return last_visited;
	}
	public void setLast_visited(Date last_visited) {
		this.last_visited = last_visited;
	}
	public Integer getLast_model() {
		return last_model;
	}
	public void setLast_model(Integer last_model) {
		this.last_model = last_model;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Integer getProvince() {
		return province;
	}
	public void setProvince(Integer province) {
		this.province = province;
	}
	public Integer getCity() {
		return city;
	}
	public void setCity(Integer city) {
		this.city = city;
	}
	public Map<Integer, Integer> getMsc() {
		return msc;
	}
	public void setMsc(Map<Integer, Integer> msc) {
		this.msc = msc;
	}
	public Map<Integer, Integer> getOsc() {
		return osc;
	}
	public void setOsc(Map<Integer, Integer> osc) {
		this.osc = osc;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -8857816254202576942L;
	private String user_id;
	
	private Integer ua;
	
	
	private Map<Integer, ByteBuffer> models ;
	
	private Map<Integer, ByteBuffer> searchs;
	
	private String echeId;
	
	private Date created_time;//用户id创建时间
	
	private Date last_visited;//最近一次访问时间
	
	private Integer last_model;//最后一次访问的车型标签id
	
	private Integer status;
	
	private Integer province;//省编码-国家标准6位数字
	
	private Integer city;//市编码-国家标准6位数字
	
	private Map<Integer, Integer> msc; //车型标签得分表
	
	private Map<Integer, Integer> osc ;//其他标签得分表
	public static void setnoargConstructor(UserData data){
		if(data!=null){
			if(data.getMsc()!=null){
				data.setMsc(new HashMap<Integer, Integer>(data.getMsc()));
			}
			if(data.getOsc()!=null){
				data.setOsc(new HashMap<Integer, Integer>(data.getOsc()));
			}
		}
	}
}
