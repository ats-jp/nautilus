package jp.ats.nautilus.dds;

import java.util.List;

import jp.ats.nautilus.common.U;

public class DDSRecord {

	private final String line;

	private final String name;

	private final List<DDSField> fields = U.newLinkedList();

	private final List<DFNLIN> dfnlins = U.newLinkedList();

	private final Skip skip = new Skip();

	private final Space space = new Space();

	private DDSFile parent;

	private CHRSIZ defaultCHRSIZ = CHRSIZ.defaultCHRSIZ;

	DDSRecord(String line) {
		this.line = line;
		name = line.substring(18, 28).trim();
	}

	public String getName() {
		return name;
	}

	public DDSFile getParent() {
		return parent;
	}

	void setParent(DDSFile parent) {
		this.parent = parent;
	}

	DFNLIN[] getDFNLINs() {
		return dfnlins.toArray(new DFNLIN[dfnlins.size()]);
	}

	String getLine() {
		return line;
	}

	void add(DDSField field) {
		fields.add(field);
	}

	ConditionHolder parseBody(String body) {
		if (skip.prepare(body)) return skip;

		if (space.prepare(body)) return space;

		DFNLIN dfnlin = DFNLIN.parse(body);
		if (dfnlin != null) {
			dfnlins.add(dfnlin);
			return dfnlin;
		}

		CHRSIZ myDefaultCHRSIZ = CHRSIZ.parse(body);

		if (myDefaultCHRSIZ != null) {
			defaultCHRSIZ = myDefaultCHRSIZ;
			return defaultCHRSIZ;
		}

		return null;
	}

	CHRSIZ getDefaultCHRSIZ() {
		return defaultCHRSIZ;
	}

	int adjustPositionForFields(
		List<DDSField> matchingFields,
		Indicators indicators,
		int rowIndex) {
		int currentRowIndex = skip.hasSkipBefore ? skip.skipBefore : rowIndex;

		currentRowIndex = space.hasSpaceBefore
			? currentRowIndex + space.spaceBefore
			: currentRowIndex;

		int currentColumnIndex = 0, currentStartColumn = 0;

		for (DDSField field : fields) {
			currentRowIndex = field.computeRowIndex(currentRowIndex);
			currentColumnIndex = field.computeColumnIndex(currentColumnIndex);
			currentStartColumn = field.computeStartColumn(currentStartColumn);

			//オプション標識が指定されない場合、全て使用
			if (indicators == null || field.judgeIndicators(indicators))
				matchingFields.add(field);
		}

		if (skip.hasSkipAfter) return skip.skipAfter;

		return space.hasSpaceAfter
			? currentRowIndex + space.spaceAfter
			: currentRowIndex;
	}

	@Override
	public String toString() {
		return U.toString(this);
	}
}
