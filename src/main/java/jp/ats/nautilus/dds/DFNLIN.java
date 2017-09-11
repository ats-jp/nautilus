package jp.ats.nautilus.dds;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ats.nautilus.pdf.Page;
import jp.ats.nautilus.pdf.Page.LineDirection;
import jp.ats.nautilus.pdf.Report.LineWidth;

public class DFNLIN extends ConditionHolder {

	private static final Pattern pattern = Pattern
		.compile("^DFNLIN\\(\\*(HRZ|VRT) +(\\d+) +(\\d+) +(\\d+)\\)$");

	private final LineDirection direction;

	private final LineWidth width;

	private final int startLine;

	private final int startColumn;

	private final int length;

	static DFNLIN parse(String body) {
		Matcher matcher = pattern.matcher(body);
		if (!matcher.matches()) return null;

		LineDirection direction;
		String directionString = matcher.group(1);
		if (directionString.equals("HRZ")) {
			direction = LineDirection.HORIZONTAL;
		} else {
			direction = LineDirection.VERTICAL;
		}

		LineWidth width;
		String expansionArea = DDSFile.getCurrentExpansionArea();
		if (expansionArea.trim().equals("@@1@")) {
			width = LineWidth.BOLD;
		} else {
			width = LineWidth.NORMAL;
		}

		return new DFNLIN(
			direction,
			width,
			Integer.parseInt(matcher.group(2)),
			Integer.parseInt(matcher.group(3)),
			Integer.parseInt(matcher.group(4)));
	}

	private DFNLIN(
		LineDirection direction,
		LineWidth width,
		int startLine,
		int startColumn,
		int length) {
		this.direction = direction;
		this.width = width;
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.length = length;
	}

	void add(Page page) {
		page.addLine(direction, width, startLine, startColumn, length);
	}
}
