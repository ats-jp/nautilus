package jp.ats.nautilus.dds;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ats.nautilus.common.U;

public class CHRSIZ extends ConditionHolder {

	public static final CHRSIZ defaultCHRSIZ = new CHRSIZ(1, 1);

	private static final Pattern pattern = Pattern
		.compile("^CHRSIZ\\(([^ ]+) ([^ ]+)\\)$");

	final int horizontalSize;

	final int verticalSize;

	static CHRSIZ parse(String body) {
		Matcher matcher = pattern.matcher(body);
		if (!matcher.matches()) return null;

		return new CHRSIZ(
			new BigDecimal(matcher.group(1)).intValue(),
			new BigDecimal(matcher.group(2)).intValue());
	}

	private CHRSIZ(int horizontalSize, int verticalSize) {
		this.horizontalSize = horizontalSize;
		this.verticalSize = verticalSize;
	}

	public int getHorizontalSize() {
		return horizontalSize;
	}

	public int getVerticalSize() {
		return verticalSize;
	}

	@Override
	public String toString() {
		return horizontalSize + "x" + verticalSize;
	}

	@Override
	public boolean equals(Object o) {
		CHRSIZ another = (CHRSIZ) o;
		return horizontalSize == another.horizontalSize
			&& verticalSize == another.verticalSize;
	}

	@Override
	public int hashCode() {
		return U.sumHashCodes(new int[] { horizontalSize, verticalSize });
	}
}
