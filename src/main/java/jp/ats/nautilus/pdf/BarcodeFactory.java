package jp.ats.nautilus.pdf;

import org.krysalis.barcode4j.BarcodeGenerator;

public interface BarcodeFactory {

	int dpi();

	float magnification();

	void decorate(BarcodeGenerator generator);

	Class<? extends BarcodeGenerator> getBarcodeGeneratorClass();

	default BarcodeGenerator createBarcodeGenerator() {
		try {
			return getBarcodeGeneratorClass().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
