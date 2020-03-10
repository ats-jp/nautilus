package jp.ats.nautilus.pdf;

import java.io.IOException;
import java.io.InputStream;

import org.apache.fontbox.ttf.CmapLookup;
import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeCollection;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import jp.ats.nautilus.common.U;

public abstract class Font {

	private final TrueTypeFont font;

	private final CmapLookup cmap;

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

	protected CmapLookup cmap() {
		try {
			return font.getUnicodeCmapLookup();
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	protected abstract TrueTypeFont trueTypeFont();

	protected abstract InputStream load() throws IOException;

	protected abstract String name();

	protected TrueTypeFont parseFontInCollection() {
		try (TrueTypeCollection collection = new TrueTypeCollection(
			U.wrap(load()))) {
			return collection.getFontByName(name());
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	protected TrueTypeFont parseFont() {
		try (InputStream input = U.wrap(load())) {
			return new TTFParser().parse(input);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}
}
