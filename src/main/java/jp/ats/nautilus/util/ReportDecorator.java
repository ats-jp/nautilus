package jp.ats.nautilus.util;

import jp.ats.nautilus.pdf.Report;

@FunctionalInterface
public interface ReportDecorator {

	void decorate(int row, int column, String value, Report report);
}
