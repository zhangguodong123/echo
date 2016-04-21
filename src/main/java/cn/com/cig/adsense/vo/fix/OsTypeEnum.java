package cn.com.cig.adsense.vo.fix;
/**   
 * @File: SystemType.java 
 * @Package cn.com.cig.adsense.vo.fix 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月14日 下午1:51:01 
 * @version V1.0   
 */
public enum OsTypeEnum {
	ANDROID(0),IOS(1);
	private int value;
	private OsTypeEnum(int n){
		this.value=n;
	}
	
	public int getValue() {
		return value;
	}
}
