package jp.ats.nautilus.dds;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Skip extends ConditionHolder {

	private static final Pattern pattern = Pattern
		.compile("^SKIP(A|B)\\(([\\d]+)\\)$");

	boolean hasSkipBefore = false;

	int skipBefore;

	boolean hasSkipAfter = false;

	int skipAfter;

	boolean prepare(String body) {
		Matcher matcher = pattern.matcher(body);
		if (!matcher.matches()) return false;

		if (matcher.group(1).equals("A")) {
			hasSkipAfter = true;
			skipAfter = Integer.parseInt(matcher.group(2));
		} else {
			hasSkipBefore = true;
			skipBefore = Integer.parseInt(matcher.group(2));
		}

		return true;
	}
}
