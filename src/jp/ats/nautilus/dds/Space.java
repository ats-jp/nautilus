package jp.ats.nautilus.dds;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Space extends ConditionHolder {

	private static final Pattern pattern = Pattern
		.compile("^SPACE(A|B)\\(([\\d]+)\\)$");

	boolean hasSpaceBefore = false;

	int spaceBefore;

	boolean hasSpaceAfter = false;

	int spaceAfter;

	boolean prepare(String body) {
		Matcher matcher = pattern.matcher(body);
		if (!matcher.matches()) return false;

		if (matcher.group(1).equals("A")) {
			hasSpaceAfter = true;
			spaceAfter = Integer.parseInt(matcher.group(2));
		} else {
			hasSpaceBefore = true;
			spaceBefore = Integer.parseInt(matcher.group(2));
		}

		return true;
	}
}
