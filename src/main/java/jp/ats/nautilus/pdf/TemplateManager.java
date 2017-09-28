package jp.ats.nautilus.pdf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import jp.ats.nautilus.common.U;

public class TemplateManager {

	private static final Object lock = new Object();

	private static final Map<Path, Template> templateCache = new HashMap<>();

	private static final Map<Template, Path> pathCache = new HashMap<>();

	public static TemplatePage createTemplatePage(Path pdfFile, int page)
		throws IOException {
		synchronized (lock) {
			Template template = templateCache.get(pdfFile);
			if (template == null) {
				template = new Template(
					U.readBytes(U.wrap(Files.newInputStream(pdfFile))));
				templateCache.put(pdfFile, template);
			}

			TemplatePage templatePage = new TemplatePage(template, page);

			pathCache.put(template, pdfFile);

			return templatePage;
		}
	}

	public static Path getTemplatePath(TemplatePage templatePage) {
		return pathCache.get(templatePage.getTemplate());
	}
}
