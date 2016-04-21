package cn.com.cig.adsense.vo.fix;
/**   
 * @File: ADEnum.java 
 * @Package cn.com.cig.adsense.vo.fix 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月7日 上午10:43:49 
 * @version V1.0   
 */
public enum HttpArgEnum {
	pubid("pubid"),
	pid("pid"),
	dvid("dvid"),
	os("os"),
	dvtype("dvtype"),
	nettype("nettype"),
	ip("ip"),
	ua("ua"),
	osvs("osvs"),
	ts("ts"),
	geo("geo"),
	tags("tags"),
	cityid("cityid"),
	key("key");
	
	private String value;
	HttpArgEnum(String arg){
		this.value=arg;
	}
	public String value() {
		return value;
	}
}
