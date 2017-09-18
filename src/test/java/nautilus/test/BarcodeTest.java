package nautilus.test;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

public class BarcodeTest {

	public static void main(String[] args) throws Exception {
		Path path = getDesktopPath().resolve("barcode.png");

		Code128Bean bean = new Code128Bean();

		bean.setBarHeight(5);

		int dpi = 200;

		try (OutputStream output = Files.newOutputStream(path)) {
			BitmapCanvasProvider canvas = new BitmapCanvasProvider(
				output,
				"image/x-png",
				dpi,
				BufferedImage.TYPE_BYTE_BINARY,
				false,
				0);

			bean.generateBarcode(canvas, "0123456789");
			canvas.finish();
		}
	}

	public static Path getDesktopPath() {
		return Paths.get(System.getProperty("user.home"), "Desktop");
	}
}
