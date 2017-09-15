package jp.ats.nautilus.dds;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import jp.ats.nautilus.common.CollectionMap;
import jp.ats.nautilus.common.TextReader;
import jp.ats.nautilus.pdf.Template;
import jp.ats.nautilus.pdf.TemplateManager;

public class TemplateVendor {

	private static final Template[] enptyArray = {};

	private final CollectionMap<String, Template> map = CollectionMap
		.newInstance();

	public TemplateVendor() {}

	public TemplateVendor(Path input, Path templateDirectory)
		throws IOException {
		String[] lines = new TextReader(input.toUri().toURL())
			.readLinesAsArray();

		for (String line : lines) {
			if (line.length() == 0) continue;
			String[] columns = line.trim().split("\\s+");
			map.put(
				columns[0],
				TemplateManager.createTemplate(
					templateDirectory.resolve(columns[1]),
					Integer.parseInt(columns[2])));
		}
	}

	public Template[] getTemplates(String ddsName) {
		Collection<Template> templates = map.get(ddsName);
		if (templates.size() == 0) return enptyArray;

		return templates.toArray(new Template[templates.size()]);
	}
}
