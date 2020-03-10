package jp.ats.nautilus.dds;

import java.io.IOException;
import java.util.List;

import jp.ats.nautilus.common.U;

public class DDSFile {

	private static final ThreadLocal<String> expansionArea = new ThreadLocal<String>();

	private final String name;

	private final DDSRecord[] records;

	public static DDSFile getInstance(String name, String[] lines)
		throws IOException {
		ReportContext.startFile(name);

		List<DDSRecord> records = U.newLinkedList();

		DDSRecord currentRecord = null;
		DDSField currentField = null;

		Condition condition = null;
		int lineCount = 0;
		for (String line : lines) {
			lineCount++;

			try {
				//コメント行
				if (line.charAt(6) == '*') continue;

				expansionArea.set(line.substring(0, 5));

				//レコード定義
				if (line.charAt(16) == 'R') {
					ReportContext.endRecord();
					currentRecord = new DDSRecord(line);

					ReportContext.startRecord(currentRecord.getName());

					records.add(currentRecord);
					currentField = null;
				} else {
					if (line.charAt(28) == 'R')
						throw new IllegalStateException("参照はサポートされていません");

					String conditionString = line.substring(6, 16);
					if (U.presents(conditionString.trim())) {
						if (condition == null) condition = new Condition();
						condition.append(conditionString);
					}

					String rowIndex = line.substring(38, 41).trim();
					String columnIndex = line.substring(41, 44).trim();

					//フィールド定義
					if (U.presents(rowIndex) || U.presents(columnIndex)) {
						ReportContext.endField();

						currentField = new DDSField(
							lineCount,
							line,
							currentRecord.getDefaultCHRSIZ(),
							rowIndex,
							columnIndex);

						ReportContext
							.startField(currentField.getConvenientName());

						currentField.setCondition(condition);
						condition = null;

						currentRecord.add(currentField);
						currentField.setParent(currentRecord);
					}
				}

				String body = line.substring(44).trim();
				if (!U.presents(body)) continue;

				ConditionHolder conditionable;
				if (currentField != null) {
					conditionable = currentField.parseBody(body);
				} else if (currentRecord != null) {
					conditionable = currentRecord.parseBody(body);
				} else {
					// 先頭行のREF()の場合はここに
					continue;
				}

				if (conditionable != null) {
					conditionable.setCondition(condition);
					condition = null;
				}
			} finally {
				expansionArea.set(null);
			}
		}

		DDSFile file = new DDSFile(
			name,
			records.toArray(new DDSRecord[records.size()]));

		ReportContext.endField();
		ReportContext.endRecord();
		ReportContext.endFile();

		return file;
	}

	static String getCurrentExpansionArea() {
		String area = expansionArea.get();
		if (area == null)
			throw new IllegalStateException("解析プロセス外からの拡張領域取得はできません。");
		return area;
	}

	private DDSFile(String name, DDSRecord[] records) {
		this.name = name;
		this.records = records.clone();

		for (DDSRecord record : this.records) {
			record.setParent(this);
		}
	}

	public String getName() {
		return name;
	}

	public DDSRecord[] getRecords() {
		return records.clone();
	}

	@Override
	public String toString() {
		return U.toString(this);
	}
}
