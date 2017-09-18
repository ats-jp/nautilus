package jp.ats.nautilus.pdf;

import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.impl.AbstractBarcodeBean;

public class SimpleBarcodeFactory implements BarcodeFactory {

	private final Class<? extends BarcodeGenerator> clazz;

	private final int dpi;

	private final float heightMM;

	private final float magnification;

	protected SimpleBarcodeFactory(
		Class<? extends BarcodeGenerator> clazz,
		int dpi,
		float heightMM,
		float magnification) {
		this.clazz = clazz;
		this.dpi = dpi;
		this.heightMM = heightMM;
		this.magnification = magnification;
	}

	@Override
	public Class<? extends BarcodeGenerator> getBarcodeGeneratorClass() {
		return clazz;
	}

	@Override
	public int dpi() {
		return dpi;
	}

	@Override
	public float magnification() {
		return magnification;
	}

	@Override
	public void decorate(BarcodeGenerator generator) {
		AbstractBarcodeBean bean = (AbstractBarcodeBean) generator;
		bean.setHeight(heightMM);
	}
}
