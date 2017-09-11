package jp.ats.nautilus.dds;

import jp.ats.nautilus.common.U;

public class Indicators {

	private final boolean[] indicators = new boolean[99];

	public void setFlag(int indicator, boolean flag) {
		indicators[indicator - 1] = flag;
	}

	public boolean getFlag(int indicator) {
		return indicators[indicator - 1];
	}

	@Override
	public String toString() {
		return U.toString(this);
	}
}
