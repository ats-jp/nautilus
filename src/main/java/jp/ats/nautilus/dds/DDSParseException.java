package jp.ats.nautilus.dds;

public class DDSParseException extends RuntimeException {

	private static final long serialVersionUID = 711782536567346447L;

	public DDSParseException(int lineCount, String message) {
		super("DDS解析中のエラー " + lineCount + " 行目 - " + message);
	}
}
