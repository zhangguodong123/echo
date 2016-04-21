package cn.com.cig.adsense.vo;

public enum PlatefromEnum {
	// 1是pc端  2是移动wap端
	PC(1), WAP(2),APP(3);

	private int index;

	private PlatefromEnum(int i) {
		this.index = i;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
