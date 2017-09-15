package jp.ats.nautilus.pdf;

public abstract class FontManager {

	private Font font;

	private Font externalFont;

	public synchronized Font getFont() {
		if (font == null) font = createFont();
		return font;
	}

	public synchronized Font getExternalFont() {
		if (externalFont == null) externalFont = createExternalFont();
		return externalFont;
	}

	protected abstract Font createFont();

	protected abstract Font createExternalFont();
}
