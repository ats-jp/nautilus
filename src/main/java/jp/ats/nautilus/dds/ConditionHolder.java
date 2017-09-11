package jp.ats.nautilus.dds;

import jp.ats.nautilus.common.U;

abstract class ConditionHolder {

	private Condition condition;

	void setCondition(Condition condition) {
		this.condition = condition;
	}

	boolean judgeIndicators(Indicators indicators) {
		if (condition == null) return true;
		return condition.judge(indicators);
	}

	boolean isReverseCondition(ConditionHolder another) {
		if (condition == null || another.condition == null) return false;
		return condition.isReverseCondition(another.condition);
	}

	@Override
	public String toString() {
		return U.toString(this);
	}
}
