package cn.com.cig.adsense.vo.fix;

public enum PositionTypeEnum {

	// 0 图片, 1 文章, 2flash, 3 视频, 4图片\flash, 5图文
	IMAGE(0), TEXT(1), FLASH(2), VIDEO(3), IMAGEORFLASH(4),ELETEXT(5);

	private int index;

	private PositionTypeEnum(int i) {
		this.index = i;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
