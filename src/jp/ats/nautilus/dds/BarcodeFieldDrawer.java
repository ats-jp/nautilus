package jp.ats.nautilus.dds;

import jp.ats.nautilus.pdf.Page;

public class BarcodeFieldDrawer extends FieldDrawer {

	private final String barcodeFactoryClass, barcode;

	private final int startLine, startColumn;

	public BarcodeFieldDrawer(
		String barcodeFactoryClass,
		String barcode,
		int startLine,
		int startColumn) {
		this.barcodeFactoryClass = barcodeFactoryClass;
		this.barcode = barcode;
		this.startLine = startLine;
		this.startColumn = startColumn;
	}

	@Override
	void draw(Page page) {
		page.addBarcode(startLine, startColumn, barcodeFactoryClass, barcode);
	}
}
