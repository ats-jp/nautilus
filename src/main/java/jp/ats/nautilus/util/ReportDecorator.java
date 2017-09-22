package jp.ats.nautilus.util;

import jp.ats.nautilus.pdf.Report;

public interface ReportDecorator {

	void decorate(int row, int column, String value, Report report);

	void drawBeforeRange(
		int startLine,
		int startColumn,
		int horizontalSize,
		int verticalSize,
		String text,
		Report report);
}
