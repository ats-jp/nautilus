package jp.ats.nautilus.dds;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum EDTCDE {

	/*
	 * EDTCDE(1)
	 */
	CODE_1 {

		@Override
		int computeLength(int definedLength, int decimalLength) {
			//3桁ごとに,が入る
			definedLength += (definedLength - 1) / 3;

			//小数点
			if (decimalLength > 0) definedLength++;

			return definedLength;
		}
	},

	/*
	 * EDTCDE(2)
	 */
	CODE_2 {

		@Override
		int computeLength(int definedLength, int decimalLength) {
			return CODE_1.computeLength(definedLength, decimalLength);
		}
	},

	/*
	 * EDTCDE(3)
	 */
	CODE_3 {

		@Override
		int computeLength(int definedLength, int decimalLength) {
			//小数点
			if (decimalLength > 0) definedLength++;

			return definedLength;
		}
	},

	/*
	 * EDTCDE(4)
	 */
	CODE_4 {

		@Override
		int computeLength(int definedLength, int decimalLength) {
			return CODE_3.computeLength(definedLength, decimalLength);
		}
	},

	/*
	 * EDTCDE(J)
	 */
	CODE_J {

		@Override
		int computeLength(int definedLength, int decimalLength) {
			return CODE_N.computeLength(definedLength, decimalLength);
		}
	},

	/*
	 * EDTCDE(L)
	 */
	CODE_L {

		@Override
		int computeLength(int definedLength, int decimalLength) {
			return CODE_3.computeLength(definedLength, decimalLength);
		}
	},

	/*
	 * EDTCDE(N)
	 */
	CODE_N {

		@Override
		int computeLength(int definedLength, int decimalLength) {
			return CODE_1.computeLength(definedLength, decimalLength) + 1;
		}
	},

	/*
	 * EDTCDE(O)
	 */
	CODE_O {

		@Override
		int computeLength(int definedLength, int decimalLength) {
			return CODE_N.computeLength(definedLength, decimalLength);
		}
	},

	/*
	 * EDTCDE(Y)
	 */
	CODE_Y {

		@Override
		int computeLength(int definedLength, int decimalLength) {
			if (definedLength <= 2) return definedLength;
			if (definedLength <= 4) return definedLength + 1;
			return definedLength + 2;
		}
	},

	/*
	 * EDTCDE(Z)
	 */
	CODE_Z {

		@Override
		int computeLength(int definedLength, int decimalLength) {
			return definedLength;
		}
	};

	private static final Pattern pattern = Pattern
		.compile("^EDTCDE\\((.)[^\\)]*\\)$");

	private static final Map<String, EDTCDE> map =  new HashMap<>();

	static {
		for (EDTCDE member : EDTCDE.values()) {
			map.put(member.name().substring(5), member);
		}
	}

	static EDTCDE parse(String body) {
		Matcher matcher = pattern.matcher(body);
		if (!matcher.matches()) return null;

		String code = matcher.group(1);
		EDTCDE selected = map.get(code);
		if (selected == null)
			throw new IllegalStateException("EDTCDE(" + code + ") は未定義です");

		return selected;
	}

	abstract int computeLength(int definedLength, int decimalLength);
}
