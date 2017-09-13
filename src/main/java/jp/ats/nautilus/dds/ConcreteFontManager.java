package jp.ats.nautilus.dds;

import java.io.File;
import java.io.IOException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

import jp.ats.nautilus.pdf.FontManager;

public class ConcreteFontManager implements FontManager {

	private static String fontDirectory = "";

	private final BaseFont font;

	private final BaseFont externalFont;

	public ConcreteFontManager() throws IOException, DocumentException {
		String fontString;
		String fontName = "msmincho.ttc";
		String systemFontPath = "c:/windows/fonts/";
		String monospaceSelector = ",0";

		String fontDirectory = getFontDirectory();

		if (!new File(systemFontPath + fontName).exists()) {
			fontString = fontDirectory + fontName + monospaceSelector;
		} else {
			fontString = systemFontPath + fontName + monospaceSelector;
		}

		font = BaseFont
			.createFont(fontString, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

		externalFont = BaseFont.createFont(
			fontDirectory + "EUDC.TTF",
			BaseFont.IDENTITY_H,
			BaseFont.EMBEDDED);
	}

	@Override
	public BaseFont createFont() {
		return font;
	}

	@Override
	public BaseFont createExternalFont() {
		return externalFont;
	}

	public static synchronized void setFontDirectory(String fontPath) {
		ConcreteFontManager.fontDirectory = fontPath;
	}

	private static synchronized String getFontDirectory() {
		return fontDirectory;
	}
}
