package jp.ats.nautilus.pdf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.pdf.PdfReader;

public class TemplateManager {

	private static final Map<Path, PdfReader> map = new HashMap<>();

	public static Template createTemplate(Path pdfFile, int page)
		throws IOException {
		synchronized (map) {
			PdfReader reader = map.get(pdfFile);
			if (reader == null) {
				reader = new PdfReader(Files.newInputStream(pdfFile));
				map.put(pdfFile, reader);
			}

			return new Template(pdfFile, reader, page);
		}
	}

	public static void close() {
		synchronized (map) {
			for (PdfReader reader : map.values()) {
				reader.close();
			}

			map.clear();
		}
	}

	public static class Template {

		private final Path pdfFile;

		private final PdfReader reader;

		private final int page;

		private Template(Path pdfFile, PdfReader reader, int page) {
			this.pdfFile = pdfFile;
			this.reader = reader;
			this.page = page;
		}

		public Path getPDFFile() {
			return pdfFile;
		}

		public int getPage() {
			return page;
		}

		void draw(Canvas canvas) {
			canvas.addTemplate(reader, page);
		}
	}
}
