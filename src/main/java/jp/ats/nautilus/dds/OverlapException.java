package jp.ats.nautilus.dds;

@SuppressWarnings("serial")
public class OverlapException extends RuntimeException {

	private final DDSField field1;

	private final DDSField field2;

	OverlapException(DDSField field1, DDSField field2) {
		super(
			"DDS上で、2つのフィールドが重なっています。: ["
				+ buildMessagePart(field1)
				+ "] ["
				+ buildMessagePart(field2)
				+ "]");
		this.field1 = field1;
		this.field2 = field2;
	}

	public DDSField getField1() {
		return field1;
	}

	public DDSField getField2() {
		return field2;
	}

	private static String buildMessagePart(DDSField field) {
		return field.getConvenientName()
			+ " ("
			+ field.getColumnIndex()
			+ ", "
			+ field.getLength()
			+ ")"
			+ " "
			+ field.getCHRSIZ();
	}
}
