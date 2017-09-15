package jp.ats.nautilus.dds;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.ats.nautilus.pdf.Font;
import jp.ats.nautilus.pdf.FontManager;

public class ConcreteFontManager implements FontManager {

	private static String fontDirectory = "";

	private final Font font;

	private final Font externalFont;

	public ConcreteFontManager() throws IOException {
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

		font = new Font() {

			@Override
			protected InputStream load() throws IOException {
				return new FileInputStream(fontString);
			}

			@Override
			protected String name() {
				return "msmincho";
			}
		};

		externalFont = new Font() {

			@Override
			protected InputStream load() throws IOException {
				return new FileInputStream(fontDirectory + "EUDC.TTF");
			}

			@Override
			protected String name() {
				return "EUDC";
			}
		};
	}

	@Override
	public Font createFont() {
		return font;
	}

	@Override
	public Font createExternalFont() {
		return externalFont;
	}

	public static synchronized void setFontDirectory(String fontPath) {
		ConcreteFontManager.fontDirectory = fontPath;
	}

	private static synchronized String getFontDirectory() {
		return fontDirectory;
	}
}
