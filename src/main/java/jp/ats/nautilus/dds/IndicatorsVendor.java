package jp.ats.nautilus.dds;

import java.io.File;
import java.io.IOException;

import jp.ats.nautilus.common.MapMap;
import jp.ats.nautilus.common.TextReader;

public class IndicatorsVendor {

	private final MapMap<String, String, Indicators> map = MapMap.newInstance();

	public IndicatorsVendor() {}

	public IndicatorsVendor(File input) throws IOException {
		String[] lines = new TextReader(input.toURI().toURL())
			.readLinesAsArray();

		for (String line : lines) {
			String[] columns = line.trim().split("\\s+");
			Indicators indicators = map.get(columns[0]).get(columns[1]);
			if (indicators == null) {
				indicators = new Indicators();
				map.get(columns[0]).put(columns[1], indicators);
			}

			for (int i = 2; i < columns.length; i++) {
				indicators.setFlag(Integer.parseInt(columns[i]), true);
			}
		}
	}

	Indicators getIndicators(String fileName, String recordName) {
		recordName = SpoolFileReportLister.treatYen(recordName);
		return map.get(fileName).get(recordName);
	}
}
