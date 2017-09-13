package jp.ats.nautilus.pdf;

import com.lowagie.text.pdf.BaseFont;

public interface FontManager {

	public BaseFont createFont();

	public BaseFont createExternalFont();
}
