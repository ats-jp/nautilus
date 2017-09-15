package jp.ats.nautilus.pdf;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import jp.ats.nautilus.common.U;

public class TemplateManager {

	private static final Object lock = new Object();

	private static final Map<Path, byte[]> contentCache = new HashMap<>();

	private static final Map<Template, Path> pathCache = new HashMap<>();

	public static Template createTemplate(Path pdfFile, int page)
		throws IOException {
		synchronized (lock) {
			byte[] content = contentCache.get(pdfFile);
			if (content == null) {
				content = U.readBytes(
					new BufferedInputStream(Files.newInputStream(pdfFile)));
				contentCache.put(pdfFile, content);
			}

			Template template = new Template(content, page);

			pathCache.put(template, pdfFile);

			return template;
		}
	}

	public static Path getTemplatePath(Template template) {
		return pathCache.get(template);
	}
}
