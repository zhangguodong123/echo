package cn.com.cig.adsense.vo.fix;

import com.google.common.base.Objects;

/**   
 * @File: Size.java 
 * @Package cn.com.cig.adsense.vo 
 * @Description: TODO
 * @author zhangguodong   
 * @date 2015年5月20日 下午2:39:31 
 * @version V1.0   
 */
public class Size {
	private int width;
	private int height;
	
	public Size(){}
	
	public Size(int width, int height) {
		this.width=width;
		this.height=height;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(width, height);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Size) {
			final Size other = (Size) obj;
			return Objects.equal(width, other.width)
					&& Objects.equal(height, other.height);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("width", width).add("height", height).toString();
	}

	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
}
