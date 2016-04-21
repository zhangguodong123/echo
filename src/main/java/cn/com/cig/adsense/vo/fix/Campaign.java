package cn.com.cig.adsense.vo.fix;

import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

/**   
 * @File: Campaign.java 
 * @Package cn.com.cig.adsense.vo 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年9月23日 上午11:31:02 
 * @version V1.0   
 */
public class Campaign {
	private Integer id;
	private Integer isPmp;
	private List<Integer> places;
	private long exposeVisitDayLimit;
	private long exposeVisitHourLimit;
	private Map<String,List<BitautoMaterial>> bitautoMaterial;//定投图片物料，精准和非精准全部物料，大集合
	private Map<String,List<BitautoMaterial>> defaultBitautoMaterial;//默认图片物料
	
	private List<BitautoMaterial> bitautoEletextMaterial;//定投图文物料，精准和非精准全部物料，大集合
	private List<BitautoMaterial> defaultEletextMaterial;//默认图文物料
	
	private List<BitautoMaterial> bitautoMaterialText;//定投物料，精准和非精准全部物料，大集合
	private List<BitautoMaterial> defaultBitautoMaterialText;//默认物料
	//文本尺寸物料
	//默认文本尺寸物料
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getIsPmp() {
		return isPmp;
	}
	public void setIsPmp(Integer isPmp) {
		this.isPmp = isPmp;
	}
	public List<Integer> getPlaces() {
		return places;
	}
	public void setPlaces(List<Integer> places) {
		this.places = places;
	}
	public long getExposeVisitDayLimit() {
		return exposeVisitDayLimit;
	}
	public void setExposeVisitDayLimit(long exposeVisitDayLimit) {
		this.exposeVisitDayLimit = exposeVisitDayLimit;
	}
	public long getExposeVisitHourLimit() {
		return exposeVisitHourLimit;
	}
	public void setExposeVisitHourLimit(long exposeVisitHourLimit) {
		this.exposeVisitHourLimit = exposeVisitHourLimit;
	}
	public Map<String, List<BitautoMaterial>> getBitautoMaterial() {
		return bitautoMaterial;
	}
	public void setBitautoMaterial(
			Map<String, List<BitautoMaterial>> bitautoMaterial) {
		this.bitautoMaterial = bitautoMaterial;
	}
	public Map<String, List<BitautoMaterial>> getDefaultBitautoMaterial() {
		return defaultBitautoMaterial;
	}
	public void setDefaultBitautoMaterial(
			Map<String, List<BitautoMaterial>> defaultBitautoMaterial) {
		this.defaultBitautoMaterial = defaultBitautoMaterial;
	}
	public List<BitautoMaterial> getBitautoMaterialText() {
		return bitautoMaterialText;
	}
	public void setBitautoMaterialText(List<BitautoMaterial> bitautoMaterialText) {
		this.bitautoMaterialText = bitautoMaterialText;
	}
	public List<BitautoMaterial> getDefaultBitautoMaterialText() {
		return defaultBitautoMaterialText;
	}
	public void setDefaultBitautoMaterialText(
			List<BitautoMaterial> defaultBitautoMaterialText) {
		this.defaultBitautoMaterialText = defaultBitautoMaterialText;
	}
	
	public List<BitautoMaterial> getBitautoEletextMaterial() {
		return bitautoEletextMaterial;
	}
	public void setBitautoEletextMaterial(
			List<BitautoMaterial> bitautoEletextMaterial) {
		this.bitautoEletextMaterial = bitautoEletextMaterial;
	}
	public List<BitautoMaterial> getDefaultEletextMaterial() {
		return defaultEletextMaterial;
	}
	public void setDefaultEletextMaterial(
			List<BitautoMaterial> defaultEletextMaterial) {
		this.defaultEletextMaterial = defaultEletextMaterial;
	}
	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("id", id)
				.add("isPmp", isPmp)
				.add("places", places)
				.add("exposeVisitDayLimit", exposeVisitDayLimit)
				.add("exposeVisitHourLimit", exposeVisitHourLimit)
				.add("bitautoMaterial", bitautoMaterial)
				.add("defaultBitautoMaterial", defaultBitautoMaterial)
				.add("bitautoEletextMaterial", bitautoEletextMaterial)
				.add("defaultEletextMaterial", defaultEletextMaterial)
				.add("bitautoMaterialText", bitautoMaterialText)
				.add("defaultBitautoMaterialText", defaultBitautoMaterialText)
				.toString();
	}
}
