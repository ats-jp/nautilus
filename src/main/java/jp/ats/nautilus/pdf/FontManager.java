package jp.ats.nautilus.pdf;

import com.itextpdf.text.pdf.BaseFont;

public interface FontManager {

	public BaseFont createFont();

	public BaseFont createExternalFont();
}
