package jp.ats.nautilus.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.itextpdf.text.pdf.PdfReader;

public class TemplateManager {

	private static final Map<File, PdfReader> map =  new HashMap<>();

	public static Template createTemplate(File pdfFile, int page)
		throws IOException {
		synchronized (map) {
			PdfReader reader = map.get(pdfFile);
			if (reader == null) {
				reader = new PdfReader(new FileInputStream(pdfFile));
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

		private final File pdfFile;

		private final PdfReader reader;

		private final int page;

		private Template(File pdfFile, PdfReader reader, int page) {
			this.pdfFile = pdfFile;
			this.reader = reader;
			this.page = page;
		}

		public File getPDFFile() {
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
