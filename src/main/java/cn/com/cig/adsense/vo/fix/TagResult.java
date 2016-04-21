package cn.com.cig.adsense.vo.fix;

import java.util.List;


public class TagResult {
	public TagParseResult getModel() {
		return model;
	}
	public void setModel(TagParseResult model) {
		this.model = model;
	}
	public List<TagParseResult> getOtherResult() {
		return otherResult;
	}
	public void setOtherResult(List<TagParseResult> otherResult) {
		this.otherResult = otherResult;
	}
	private TagParseResult model;
	private List<TagParseResult> otherResult;
}
