package jp.ats.nautilus.util;

import java.nio.charset.Charset;

import jp.ats.nautilus.common.Constants;
import jp.ats.nautilus.pdf.Report;

public class Range implements Comparable<Range> {

	private static final Charset charset = Constants.MEASURE_CHARSET;

	private static final ReportDecorator defaultDecorator = new ReportDecorator() {

		@Override
		public void decorate(int row, int column, String value, Report report) {
			report.drawText(row, column, 1, 1, value);
		}

		@Override
		public void drawBeforeRange(
			int startLine,
			int startColumn,
			int horizontalSize,
			int verticalSize,
			String text,
			Report report) {
			report.drawText(
				startLine,
				startColumn,
				horizontalSize,
				verticalSize,
				text);
		}
	};

	private final int lineIndex;

	private final int from;

	private final int to;

	private final ReportDecorator decorator;

	public Range(int lineIndex, int from, int to, ReportDecorator decorator) {
		this.lineIndex = lineIndex;
		this.from = from;
		this.to = to;
		this.decorator = decorator;
	}

	public Range(int lineIndex, int from, int to) {
		this.lineIndex = lineIndex;
		this.from = from;
		this.to = to;
		this.decorator = defaultDecorator;
	}

	public String extractFrom(RestoredPage page) {
		String line = page.get(lineIndex);
		if (line.length() == 0) return "";
		return substring(line, from, to);
	}

	public int executeIfMatches(
		int lineIndex,
		int alreadyExecutedPosition,
		String line,
		Report report) {
		if (this.lineIndex != lineIndex) return alreadyExecutedPosition;

		decorator.drawBeforeRange(
			lineIndex + 1,
			alreadyExecutedPosition + 1,
			1,
			1,
			substring(line, alreadyExecutedPosition, from),
			report);

		String value = substring(line, from, to);
		decorator.decorate(lineIndex + 1, from + 1, value, report);

		return to;
	}

	public static String substring(String source, int from, int to) {
		return substringInternal(source.getBytes(charset), from, to);
	}

	public static String substring(String source, int from) {
		byte[] bytes = source.getBytes(charset);
		return substringInternal(bytes, from, bytes.length);
	}

	private static String substringInternal(byte[] bytes, int from, int to) {
		byte[] dest = new byte[to - from];
		System.arraycopy(bytes, from, dest, 0, dest.length);
		return new String(dest, charset);
	}

	@Override
	public int compareTo(Range another) {
		int subtract = lineIndex - another.lineIndex;
		return subtract == 0 ? from - another.from : subtract;
	}
}
