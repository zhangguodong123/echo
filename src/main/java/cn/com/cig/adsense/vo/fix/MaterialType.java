package cn.com.cig.adsense.vo.fix;

public enum MaterialType {

	// 0图片, 1文章, 2flash,3视频,图文
	IMAGE(0), TEXT(1), FLASH(2), VIDEO(3),TELETEXT(4);

	private int index;

	private MaterialType(int i) {
		this.index = i;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

}
