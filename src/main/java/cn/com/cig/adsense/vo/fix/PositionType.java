package cn.com.cig.adsense.vo.fix;

public enum PositionType {

	// 0 图片, 1 文章, 2flash, 3 视频, 4图片\flash
	IMAGE(0), TEXT(1), FLASH(2), VIDEO(3), IMAGEORFLASH(4);

	private int index;

	private PositionType(int i) {
		this.index = i;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
