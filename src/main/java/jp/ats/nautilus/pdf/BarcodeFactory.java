package jp.ats.nautilus.pdf;

import com.lowagie.text.pdf.Barcode;

public interface BarcodeFactory {

	void decorate(Barcode barcode);

	Class<? extends Barcode> getBarcodeClass();

	default Barcode create(String barcode) {
		Barcode code;
		try {
			code = getBarcodeClass().newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		decorate(code);

		code.setCode(barcode);

		return code;
	}
}
