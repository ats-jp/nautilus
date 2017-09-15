package jp.ats.nautilus.pdf;

public class DocumentException extends RuntimeException {

	private static final long serialVersionUID = -7326699324945101325L;

	public DocumentException(Exception e) {
		super(e);
	}
}
