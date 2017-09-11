package jp.ats.nautilus.dds;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import jp.ats.nautilus.common.MapMap;
import jp.ats.nautilus.common.TextReader;

public class UnderlineOnlyFieldVendor {

	private final MapMap<String, String, Set<String>> map = MapMap
		.newInstance();

	public UnderlineOnlyFieldVendor() {}

	public UnderlineOnlyFieldVendor(File input) throws IOException {
		String[] lines = new TextReader(input.toURI().toURL())
			.readLinesAsArray();

		for (String line : lines) {
			String[] columns = line.trim().split("\\s+");
			Set<String> set = map.get(columns[0]).get(columns[1]);
			if (set == null) {
				set = new HashSet<>();
				map.get(columns[0]).put(columns[1], set);
			}

			set.add(columns[2]);
		}
	}

	boolean isUnderlineOnly(
		String fileName,
		String recordName,
		String fieldName) {
		recordName = SpoolFileReportLister.treatYen(recordName);
		Set<String> set = map.get(fileName).get(recordName);
		if (set == null) return false;
		return set.contains(fieldName);
	}
}
