package jp.ats.nautilus.dds;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import jp.ats.nautilus.common.CollectionMap;
import jp.ats.nautilus.pdf.TemplateManager;
import jp.ats.nautilus.pdf.TemplatePage;

public class TemplateVendor {

	private static final TemplatePage[] emptyArray = {};

	private final CollectionMap<String, TemplatePage> map = CollectionMap
		.newInstance();

	public TemplateVendor() {}

	public TemplateVendor(Path input, Path templateDirectory)
		throws IOException {
		for (String line : Files.readAllLines(input)) {
			if (line.length() == 0) continue;
			String[] columns = line.trim().split("\\s+");
			map.put(
				columns[0],
				TemplateManager.createTemplatePage(
					templateDirectory.resolve(columns[1]),
					Integer.parseInt(columns[2])));
		}
	}

	public TemplatePage[] getTemplates(String ddsName) {
		Collection<TemplatePage> templates = map.get(ddsName);
		if (templates.size() == 0) return emptyArray;

		return templates.toArray(new TemplatePage[templates.size()]);
	}
}
