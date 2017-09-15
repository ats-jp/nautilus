package jp.ats.nautilus.pdf;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import jp.ats.nautilus.common.U;

public abstract class Font {

	private final byte[] source;

	public Font() {
		try {
			try (InputStream input = new BufferedInputStream(load())) {
				source = U.readBytes(input);
			}
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	PDType0Font createPDFont(PDDocument document) {
		try (TrueTypeCollection collection = new TrueTypeCollection(
			new ByteArrayInputStream(source))) {
			return PDType0Font
				.load(document, collection.getFontByName(name()), true);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	protected abstract InputStream load() throws IOException;

	protected abstract String name();
}
