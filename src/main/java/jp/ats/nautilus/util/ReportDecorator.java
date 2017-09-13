package jp.ats.nautilus.util;

import jp.ats.nautilus.pdf.Report;

@FunctionalInterface
public interface ReportDecorator {

	void decorate(int lineIndex, int from, String value, Report report);
}
