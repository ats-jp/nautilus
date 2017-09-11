package jp.ats.nautilus.dds;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.ats.nautilus.common.U;

class Condition {

	private static final Pattern pattern = Pattern.compile(
		"^([ AO])([ N]\\d{2}| {3})([ N]\\d{2}| {3})([ N]\\d{2}| {3})$");

	// or でグループ化
	private final List<ConditionUnit> units = U.newLinkedList();

	private ConditionUnit currentUnit;

	static boolean matches(String conditionString) {
		Matcher matcher = pattern.matcher(conditionString);
		if (!matcher.matches()) return false;
		if (U.isAvailable(matcher.group(2).trim())
			|| U.isAvailable(matcher.group(3).trim())
			|| U.isAvailable(matcher.group(4).trim())) return true;

		return false;
	}

	void append(String conditionString) {
		Matcher matcher = pattern.matcher(conditionString);
		matcher.matches();

		if ("O".equals(matcher.group(1))) or();

		parseAndAppend(matcher.group(2));
		parseAndAppend(matcher.group(3));
		parseAndAppend(matcher.group(4));
	}

	boolean judge(Indicators indicators) {
		for (ConditionUnit unit : units) {
			if (unit.judge(indicators)) return true;
		}

		return false;
	}

	/**
	 * 条件同士が、完全に反転しているかどうかをチェックする
	 */
	boolean isReverseCondition(Condition another) {
		//OR を含んでいる場合、完全反転条件とはならない
		if (units.size() > 1 || another.units.size() > 1) return false;

		return units.get(0).isReverseCondition(another.units.get(0));
	}

	private void or() {
		currentUnit = null;
	}

	@Override
	public String toString() {
		return U.toString(this);
	}

	private void parseAndAppend(String unitString) {
		if (!U.isAvailable(unitString.trim())) return;

		ConditionUnit unit = new ConditionUnit(
			!unitString.startsWith("N"),
			Integer.parseInt(unitString.substring(1)));

		if (currentUnit == null) {
			currentUnit = unit;
			units.add(currentUnit);
		} else {
			currentUnit.append(unit);
		}
	}

	private static class ConditionUnit {

		private final List<ConditionUnit> allies = U.newLinkedList();

		private final boolean flag;

		private final int indicator;

		ConditionUnit(boolean flag, int indicator) {
			this.flag = flag;
			this.indicator = indicator;
		}

		void append(ConditionUnit ally) {
			allies.add(ally);
		}

		boolean judge(Indicators indicators) {
			for (ConditionUnit ally : allies) {
				if (!ally.judgeSelf(indicators)) return false;
			}

			return judgeSelf(indicators);
		}

		boolean judgeSelf(Indicators indicators) {
			return flag == indicators.getFlag(indicator);
		}

		/**
		 * AND条件内に、相反する条件が一つだけあり、その他は全く同じ条件であれば
		 * 完全に反転していると言える
		 * 完全に反転しているならば、印字に使用されるフィールドは常にどちらかなので
		 * 無条件で結合可能となる
		 */
		boolean isReverseCondition(ConditionUnit another) {
			//双方のindicator順の条件たちを用意
			List<ConditionUnit> selfs = getAllUnit();
			List<ConditionUnit> anothers = another.getAllUnit();

			int size = selfs.size();

			//数が違えば、当然反転にはならない
			if (size != anothers.size()) return false;

			boolean reverseFound = false;

			for (int i = 0; i < size; i++) {
				ConditionUnit selfTarget = selfs.get(i);
				ConditionUnit anotherTarget = anothers.get(i);

				if (selfTarget.indicator == anotherTarget.indicator) {
					//indicatorとflagが同じならば、条件を相殺
					if (selfTarget.flag == anotherTarget.flag) continue;

					//既に反転条件があるならば、完全反転にはならないのでfalse
					if (reverseFound) return false;

					//とりあえず反転条件発見
					reverseFound = true;
				} else {
					//indicatorが一つでも一致しなければfalse
					return false;
				}
			}

			//反転条件がひとつで、ほかは同一条件だったので完全反転とみなす
			if (reverseFound) return true;

			return false;
		}

		private List<ConditionUnit> getAllUnit() {
			List<ConditionUnit> list = U.newLinkedList();
			list.add(this);
			list.addAll(allies);
			Collections.sort(list, ConditionUnitComparator.singleton);
			return list;
		}

		@Override
		public String toString() {
			return U.toString(this);
		}
	}

	private static class ConditionUnitComparator
		implements Comparator<ConditionUnit> {

		private static final ConditionUnitComparator singleton = new ConditionUnitComparator();

		@Override
		public int compare(ConditionUnit o1, ConditionUnit o2) {
			//indicator順
			return o1.indicator - o2.indicator;
		}
	}
}
