package jp.ats.nautilus.util;

import java.util.List;

import jp.ats.nautilus.common.U;

class Page {

	private final List<String> lines;

	Page(List<String> lines) {
		this.lines = lines;
	}

	String get(int index) {
		return lines.get(index);
	}

	List<String> getLines() {
		return lines;
	}

	@Override
	public String toString() {
		return String.join(U.LINE_SEPARATOR, lines);
	}
}
