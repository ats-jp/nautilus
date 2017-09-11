package jp.ats.nautilus.dds;

import jp.ats.nautilus.common.U;
import jp.ats.nautilus.pdf.Page;

public class TextFieldDrawer extends FieldDrawer {

	private final String value;

	private final int startLine, startColumn;

	private final CHRSIZ chrsiz;

	private final boolean underlined;

	private final int length;

	public TextFieldDrawer(
		String value,
		int startLine,
		int startColumn,
		CHRSIZ chrsiz,
		boolean underlined,
		int length) {
		this.value = value;
		this.startLine = startLine;
		this.startColumn = startColumn;
		this.chrsiz = chrsiz;
		this.underlined = underlined;
		this.length = length;
	}

	@Override
	void draw(Page page) {
		if (underlined) page.addUnderLine(
			startLine,
			startColumn,
			chrsiz.horizontalSize,
			chrsiz.verticalSize,
			length);

		if (!U.isAvailable(value)) return;

		page.addText(
			startLine,
			startColumn,
			chrsiz.horizontalSize,
			chrsiz.verticalSize,
			value);
	}
}
