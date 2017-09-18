package jp.ats.nautilus.pdf;

import org.krysalis.barcode4j.BarcodeGenerator;

public class SimpleBarcodeFactory implements BarcodeFactory {

	private final Class<? extends BarcodeGenerator> clazz;

	private final int dpi;

	protected SimpleBarcodeFactory(
		Class<? extends BarcodeGenerator> clazz,
		int dpi) {
		this.clazz = clazz;
		this.dpi = dpi;
	}

	@Override
	public Class<? extends BarcodeGenerator> getBarcodeGeneratorClass() {
		return clazz;
	}

	@Override
	public int dpi() {
		return dpi;
	}
}
