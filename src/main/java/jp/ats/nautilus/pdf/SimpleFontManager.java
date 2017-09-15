package jp.ats.nautilus.pdf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SimpleFontManager extends FontManager {

	@Override
	protected Font createFont() {
		return new Font() {

			@Override
			protected InputStream load() throws IOException {
				return new FileInputStream("c:/windows/fonts/msmincho.ttc");
			}

			@Override
			protected String name() {
				return "MS-Mincho";
			}
		};
	}

	@Override
	protected Font createExternalFont() {
		return null;
	}
}
