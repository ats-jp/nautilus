package jp.ats.nautilus.util;

import java.util.List;

import jp.ats.nautilus.common.U;

public class RestoredPage {

	private final List<String> lines;

	RestoredPage(List<String> lines) {
		this.lines = lines;
	}

	public String get(int index) {
		return lines.get(index);
	}

	public List<String> getLines() {
		return lines;
	}

	@Override
	public String toString() {
		return String.join(U.LINE_SEPARATOR, lines);
	}
}
