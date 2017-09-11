package jp.ats.nautilus.dds;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ats.nautilus.common.U;

class SpoolPage {

	private static final Pattern pattern = Pattern.compile("^(\\d)$|^([A-Z])$");

	private static final int gap = 'A' - 10;

	private final int columns;

	private final Line[] lines;

	private final String filler;

	private int currentPosition = 0;

	SpoolPage(int rows, int columns) {
		this.columns = columns;
		lines = new Line[rows];
		filler = supply("", columns);
	}

	int getRows() {
		return lines.length;
	}

	String getLine(int index) {
		Line line = lines[index];
		if (line == null) return filler;
		return line.value;
	}

	int getLineCount(int index) {
		Line line = lines[index];
		if (line == null) return 1;
		return line.getLineCount();
	}

	String[] getLines(int index) {
		return lines[index].getValues();
	}

	DDSRecord[] orderRecords(
		DDSRecord[] originalRecords,
		int recordIndicatorPosition) {
		//recordIndicatorPositionが0の場合、指定されていないものとみなし、DDSRecordが単一であるとして動作する
		if (recordIndicatorPosition == 0) return originalRecords.clone();

		List<DDSRecord> result = U.newLinkedList();
		byte[] buffer = { 0 };
		for (Line line : lines) {
			if (line == null) continue;

			byte[] bytes = line.value.getBytes(ReportLister.MEASURE_CHARSET);

			//recordIndicatorPositionが-1の場合、最後尾が指定されたものとする
			if (recordIndicatorPosition == -1)
				//先頭5バイトとられた分を引く
				recordIndicatorPosition = bytes.length - 5;

			//重複行は重複元の行と同じレコードなので対象外
			buffer[0] = bytes[recordIndicatorPosition - 1];
			String recordIndicator = new String(
				buffer,
				ReportLister.MEASURE_CHARSET);

			if (" ".equals(recordIndicator)) continue;

			Matcher matcher = pattern.matcher(recordIndicator);

			if (!matcher.matches()) throw new IllegalStateException(
				"不正なレコード識別子:" + recordIndicator);

			/*
			 * ◎レコード識別子とは
			 * DDS仕様にはないNautilus独自仕様
			 *
			 * ◎何のために必要か
			 * スプール内のレコードが、RPGの制御によりDDSの定義順通り現れないことがあり、その場合DDSレコードの特定ができない
			 * DDSのレコードと、スプールのレコードの対応付けのために必要となる
			 *
			 * ◎前提
			 * レコードの先頭行のrecordIndicatorPosition桁目に1文字識別子を出力するように、RPGを改修する必要がある
			 * 識別子として、1 - 9, A - zが使用できる
			 * ただし、識別子の値は、DDSの定義順（一番最初のレコードが1、それ以降2, 3...）と対応している必要がある
			 * 識別子は全行に必要ではなく、レコードが開始した箇所にのみ出力されている必要がある
			 */
			int index;
			String match = matcher.group(1);
			if (match != null) {
				index = Integer.parseInt(match);
			} else {
				index = matcher.group(2).toCharArray()[0] - gap;
			}

			result.add(originalRecords[index - 1]);
		}

		return result.toArray(new DDSRecord[result.size()]);
	}

	boolean adoptLine(String line) {
		String header = line.substring(0, 5);
		int number = Integer.parseInt(line.substring(0, 5).trim());

		if (header.charAt(3) != ' ') {
			currentPosition += number;
		} else {
			//改ページ
			if (number < currentPosition + 1) {
				return false;
			}

			currentPosition = number - 1;
		}

		setLine(currentPosition, supply(line.substring(5), columns));

		return true;
	}

	private void setLine(int index, String lineString) {
		Line line = lines[index];
		if (line == null) {
			line = new Line();
			lines[index] = line;
		}

		line.setValue(lineString);
	}

	private static String supply(String original, int max) {
		byte[] bytes = original.getBytes(ReportLister.MEASURE_CHARSET);

		int remain = max - bytes.length;

		StringBuilder builder = new StringBuilder(original);

		while (remain-- > 0)
			builder.append(' ');

		return builder.toString();
	}

	private static class Line {

		private String value;

		private LinkedList<String> sibling;

		private void setValue(String value) {
			if (this.value == null) {
				this.value = value;
				return;
			}

			if (sibling == null) {
				sibling = U.newLinkedList();
				sibling.add(this.value);
			}

			sibling.add(value);
		}

		private int getLineCount() {
			if (sibling == null) return 1;
			return sibling.size();
		}

		private String[] getValues() {
			return sibling.toArray(new String[sibling.size()]);
		}
	}
}
