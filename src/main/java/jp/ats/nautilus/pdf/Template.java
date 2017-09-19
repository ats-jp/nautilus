package jp.ats.nautilus.pdf;

public class Template {

	private final byte[] templateDocument;

	public Template(byte[] templateDocument) {
		this.templateDocument = templateDocument;
	}

	byte[] getTemplateDocument() {
		return templateDocument;
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
