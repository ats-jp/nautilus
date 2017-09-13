package jp.ats.nautilus.pdf;

import com.lowagie.text.Element;
import com.lowagie.text.pdf.Barcode;

public class SimpleBarcodeFactory implements BarcodeFactory {

	private final Class<? extends Barcode> clazz;

	private final float x;

	private final float n;

	private final float size;

	private final float baseLine;

	private final float barHeight;

	protected SimpleBarcodeFactory(
		Class<? extends Barcode> clazz,
		float x,
		float n,
		float size,
		float baseLine,
		float barHeight) {
		this.clazz = clazz;
		this.x = x;
		this.n = n;
		this.size = size;
		this.baseLine = baseLine;
		this.barHeight = barHeight;
	}

	@Override
	public Class<? extends Barcode> getBarcodeClass() {
		return clazz;
	}

	@Override
	public void decorate(Barcode barcode) {
		barcode.setX(x);
		barcode.setN(n);
		barcode.setSize(size);
		barcode.setTextAlignment(Element.ALIGN_CENTER);
		barcode.setBaseline(baseLine);
		barcode.setBarHeight(barHeight);
	}
}
