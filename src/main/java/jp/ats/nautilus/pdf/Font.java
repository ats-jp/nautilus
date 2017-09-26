package jp.ats.nautilus.pdf;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.fontbox.ttf.CmapSubtable;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

public abstract class Font {

	private final TrueTypeFont font;

	private final CmapSubtable cmap;

	public Font() {
		font = trueTypeFont();
		cmap = cmap();
	}

	public boolean hasGryph(int codePoint) {
		if (cmap == null) throw new UnsupportedOperationException();
		return cmap.getGlyphId(codePoint) != 0;
	}

	protected PDType0Font createPDFont(PDDocument document) {
		try {
			return PDType0Font.load(document, font, true);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	protected CmapSubtable cmap() {
		try {
			return font.getUnicodeCmap();
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	protected abstract TrueTypeFont trueTypeFont();

	protected abstract InputStream load() throws IOException;

	protected abstract String name();

	protected TrueTypeFont parseFontInCollection() {
		try (TrueTypeCollection collection = new TrueTypeCollection(
			new BufferedInputStream(load()))) {
			return collection.getFontByName(name());
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	protected TrueTypeFont parseFont() {
		try (InputStream input = new BufferedInputStream(load())) {
			return new TTFParser().parse(input);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}
}
