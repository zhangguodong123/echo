package cn.com.cig.adsense.vo.fix;
/**   
 * @File: TdlForAdStatus.java 
 * @Package cn.com.cig.adsense.utils 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年3月24日 上午11:42:40 
 * @version V1.0   
 */
public enum Status{
	has_no_default(0),//正常投放流程
	no_region_identity_materials(1),//无效的地域标识,无法识别的地域或者IP
	no_region_materials(2),//关联尺寸物料不符合地域匹配。
	no_match_materials(3),//找不到匹配物料,返回默认物料
	no_cookie_no_match_size_materials(4),//既没cookie又没有关联地域的物料
	no_cookie_materials(5),//没有cookie,但有匹配size物料。
	no_relatedModel_no_relatedRegion_materials(6),//没指定车型集没关联尺寸车型
	final_default(7);//最终全部不符合的默认投放
	
	private int index;
	
	private Status(int i){
		this.index=i;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
