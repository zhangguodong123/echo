package cn.com.cig.adsense.vo.fix;
/**   
 * @File: DockingEnum.java 
 * @Package cn.com.cig.adsense.vo.fix 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2016年3月14日 下午1:56:11 
 * @version V1.0   
 */
public enum DockingEnum {
	API(0),SDK(1);
	private int value;
	private DockingEnum(int v){
		this.value=v;
	}
	public int getValue() {
		return value;
	}
}
