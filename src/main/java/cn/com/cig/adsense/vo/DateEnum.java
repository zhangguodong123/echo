package cn.com.cig.adsense.vo;
/**   
 * @File: DateEnum.java 
 * @Package cn.com.cig.adsense.utils.date 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月2日 下午4:06:24 
 * @version V1.0   
 */
public enum DateEnum {
	FORMAT_DATE_0("yyyy-MM-dd"),
	FORMAT_DATE_1("yyyy-MM-dd-HH-mm-ss"),
	FORMAT_DATE_2("yyyy-MM-dd HH:mm:ss"),
	FORMAT_DATE_3("yyyy-MM-dd HH:mm");
	
	private String value;
	
	DateEnum(String text){
		this.value=text;
	}
	public String value() {
		return value;
	}
}
