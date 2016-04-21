package cn.com.cig.adsense.vo.dyn;
/**   
 * @File: PlatFormEnum.java 
 * 投放平台1pc端 2移动端
 * @Package cn.com.cig.adsense.vo.dyn 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月7日 上午10:15:56 
 * @version V1.0   
 */
public enum PlatFormEnum {
	PC(1),APP(2);
	private int value;
	private PlatFormEnum(int id){
		this.value=id;
	}
	public int getValue() {
		return value;
	}
}
