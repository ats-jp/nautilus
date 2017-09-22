package jp.ats.nautilus.pdf;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import jp.ats.nautilus.common.U;

public abstract class Font {

	private final byte[] source;

	private final CmapSubtable cmap;

	public Font() {
		try {
			try (InputStream input = new BufferedInputStream(load())) {
				source = U.readBytes(input);
			}
		} catch (IOException e) {
			throw new DocumentException(e);
		}

		cmap = createCmap();
	}

	public boolean hasGryph(int codePoint) {
		if (cmap == null) throw new UnsupportedOperationException();

		return cmap.getGlyphId(codePoint) != 0;
	}

	protected byte[] source() {
		return source;
	}

	protected PDType0Font createPDFont(PDDocument document) {
		try (TrueTypeCollection collection = new TrueTypeCollection(
			new ByteArrayInputStream(source))) {
			return PDType0Font
				.load(document, collection.getFontByName(name()), true);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	protected CmapSubtable createCmap() {
		try {
			return new TTFParser().parse(new ByteArrayInputStream(source))
				.getUnicodeCmap(false);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	protected abstract InputStream load() throws IOException;

	protected abstract String name();
}
