package jp.ats.nautilus.dds;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jp.ats.nautilus.common.MapMap;
import jp.ats.nautilus.common.TextReader;

public class BarcodeFieldVendor {

	private final MapMap<String, String, Map<String, String>> map = MapMap
		.newInstance();

	public BarcodeFieldVendor() {}

	public BarcodeFieldVendor(File input) throws IOException {
		String[] lines = new TextReader(input.toURI().toURL())
			.readLinesAsArray();

		for (String line : lines) {
			String[] columns = line.trim().split("\\s+");
			Map<String, String> fieldsAndClasses = map.get(columns[0])
				.get(columns[1]);
			if (fieldsAndClasses == null) {
				fieldsAndClasses = new HashMap<>();
				map.get(columns[0]).put(columns[1], fieldsAndClasses);
			}

			fieldsAndClasses.put(columns[2], columns[3]);
		}
	}

	String getBarcodeFactroyClass(
		String fileName,
		String recordName,
		String fieldName) {
		recordName = SpoolFileReportLister.treatYen(recordName);
		Map<String, String> fieldsAndClasses = map.get(fileName)
			.get(recordName);
		if (fieldsAndClasses == null) return null;
		return fieldsAndClasses.get(fieldName);
	}
}
