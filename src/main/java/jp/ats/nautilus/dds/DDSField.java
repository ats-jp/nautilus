package jp.ats.nautilus.dds;

import static jp.ats.nautilus.dds.AS400Utilities.addsSpacesAsShiftChars;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ats.nautilus.common.U;

public class DDSField extends ConditionHolder {

	private static final Pattern indexPattern = Pattern
		.compile("^(\\+?)(\\d+)$");

	private static final Pattern staticStringPattern = Pattern
		.compile("^'([^']+)(['-])$");

	private static final Pattern EDTWRDPattern = Pattern
		.compile("^EDTWRD\\('([^']+)'\\)$");

	private final String name;

	private final Skip skip = new Skip();

	private final Space space = new Space();

	private final int decimalLength;

	private final String rowIndexString;

	private final String columnIndexString;

	private DDSRecord parent;

	private int length;

	private int rowIndex;

	private int columnIndex;

	private int startColumn;

	private CHRSIZ chrsiz;

	private boolean underlined = false;

	private boolean continuing = false;

	private String staticBody;

	private String continuingBody;

	DDSField(
		int lineCount,
		String line,
		CHRSIZ defaultCHRSIZ,
		String rowIndexString,
		String columnIndexString) {
		name = line.substring(18, 28).trim();

		String lengthString = line.substring(29, 34).trim();
		if (U.presents(lengthString)) {
			if (!isInteger(lengthString)) throw new DDSParseException(
				lineCount,
				"フィールドの桁数が数値ではありません [" + lengthString + "]");

			length = Integer.parseInt(lengthString);
		}

		String decimalLengthString = line.substring(35, 37).trim();
		if (U.presents(decimalLengthString)) {
			if (!isInteger(decimalLengthString)) throw new DDSParseException(
				lineCount,
				"フィールドの少数点桁数が数値ではありません [" + decimalLengthString + "]");

			decimalLength = Integer.parseInt(decimalLengthString);
		} else {
			decimalLength = 0;
		}

		chrsiz = defaultCHRSIZ;

		this.rowIndexString = rowIndexString;
		this.columnIndexString = columnIndexString;
	}

	/**
	 * 定義された名称
	 */
	public String getName() {
		return name;
	}

	/**
	 * 無名項目も含めた便宜上の名称
	 * メッセージ等に使用する
	 */
	public String getConvenientName() {
		if (U.presents(name)) return name;
		if (U.presents(staticBody)) return "'" + staticBody + "'";
		return "*";
	}

	public int getRowIndex() {
		return rowIndex;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public int getStartColumn() {
		return startColumn;
	}

	public CHRSIZ getCHRSIZ() {
		return chrsiz;
	}

	public int getLength() {
		return length;
	}

	public boolean isUnderlined() {
		return underlined;
	}

	public DDSRecord getParent() {
		return parent;
	}

	int computeRowIndex(int currentRowIndex) {
		if (U.presents(rowIndexString)) {
			rowIndex = computeIndex(currentRowIndex, rowIndexString);
			return rowIndex;
		}

		if (skip.hasSkipBefore) {
			rowIndex = skip.skipBefore;
			return rowIndex;
		}

		rowIndex = currentRowIndex;

		if (space.hasSpaceBefore) {
			rowIndex += space.spaceBefore;
			return rowIndex;
		}

		if (skip.hasSkipAfter) return skip.skipAfter;

		return space.hasSpaceAfter ? rowIndex + space.spaceAfter : rowIndex;
	}

	int computeColumnIndex(int currentColumnIndex) {
		columnIndex = computeIndex(currentColumnIndex, columnIndexString);
		return columnIndex + length;
	}

	int computeStartColumn(int currentStartColumn) {
		startColumn = computeIndex(currentStartColumn, columnIndexString);
		return startColumn + length * chrsiz.horizontalSize;
	}

	void setParent(DDSRecord parent) {
		this.parent = parent;
	}

	ConditionHolder parseBody(String body) {
		if (continuing) {
			continuingBody += body.substring(0, body.length() - 1);
			if (body.endsWith("'")) {
				//次行に継続する固定文字列の場合、接続部分のshift-out shift-inの
				//2バイトは無視され連結される。
				//そのため、予めshift文字をスペースに変換してしまうとこのshift文字
				//無視が判断できなくなってしまうので、一旦スペース化無しでDDSを
				//ASCII化し、継続固定文字を連結後に改めてshift文字のスペース変換を行う
				staticBody = addsSpacesAsShiftChars(continuingBody);
				length = staticBody
					.getBytes(ReportLister.MEASURE_CHARSET).length;
				continuing = false;
			}

			return null;
		}

		if (skip.prepare(body)) return skip;

		if (space.prepare(body)) return space;

		CHRSIZ myCHRSIZ = CHRSIZ.parse(body);
		if (myCHRSIZ != null) {
			chrsiz = myCHRSIZ;
			return chrsiz;
		}

		{
			Matcher matcher = staticStringPattern.matcher(body);
			if (matcher.matches()) {
				if (matcher.group(2).equals("-")) {
					continuingBody = matcher.group(1);
					continuing = true;
				} else {
					staticBody = addsSpacesAsShiftChars(matcher.group(1));
					length = staticBody
						.getBytes(ReportLister.MEASURE_CHARSET).length;
				}

				return null;
			}
		}

		EDTCDE edtcde = EDTCDE.parse(body);
		if (edtcde != null) {
			length = edtcde.computeLength(length, decimalLength);
			//オプション標識は無効
			return null;
		}

		{
			Matcher matcher = EDTWRDPattern.matcher(body);
			if (matcher.matches()) {
				length = addsSpacesAsShiftChars(matcher.group(1))
					.getBytes(ReportLister.MEASURE_CHARSET).length;
				return null;
			}
		}

		if (body.equals("TIME")) {
			length = 8;
			return null;
		}

		if (body.equals("PAGNBR")) {
			length = 4;
			return null;
		}

		if (body.equals("UNDERLINE")) {
			underlined = true;
		}

		return null;
	}

	private static int computeIndex(int currentIndex, String myIndexString) {
		Matcher matcher = indexPattern.matcher(myIndexString);
		if (!matcher.matches()) throw new IllegalStateException();

		int myIndex = Integer.parseInt(matcher.group(2));

		if (U.presents(matcher.group(1))) return currentIndex + myIndex;

		return myIndex;
	}

	private static boolean isInteger(String target) {
		return target.matches("^\\d+$");
	}
}
