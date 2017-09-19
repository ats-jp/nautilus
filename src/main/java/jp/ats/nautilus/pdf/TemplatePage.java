package jp.ats.nautilus.pdf;

public class TemplatePage {

	private final Template template;

	private final int pageIndex;

	public TemplatePage(Template template, int pageIndex) {
		this.template = template;
		this.pageIndex = pageIndex;
	}

	public Template getTemplate() {
		return template;
	}

	public int getPageIndex() {
		return pageIndex;
	}
}
