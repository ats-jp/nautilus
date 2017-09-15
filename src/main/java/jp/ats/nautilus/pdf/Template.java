package jp.ats.nautilus.pdf;

public class Template {

	private final byte[] templateDocument;

	private final int pageIndex;

	public Template(byte[] templateDocument, int pageIndex) {
		this.templateDocument = templateDocument;
		this.pageIndex = pageIndex;
	}

	byte[] getTemplateDocument() {
		return templateDocument;
	}

	int getPageIndex() {
		return pageIndex;
	}

	@Override
	public final boolean equals(Object another) {
		return super.equals(another);
	}

	@Override
	public final int hashCode() {
		return super.hashCode();
	}
}
