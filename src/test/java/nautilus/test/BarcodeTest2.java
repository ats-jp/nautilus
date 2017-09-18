package nautilus.test;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jp.ats.nautilus.pdf.Canvas;
import jp.ats.nautilus.pdf.Rectangle;
import jp.ats.nautilus.pdf.Report;

public class BarcodeTest2 {

	public static void main(String[] args) throws Exception {
		try (OutputStream out = Files
			.newOutputStream(getDesktopPath().resolve("test.pdf"))) {
			Canvas c = new Canvas(
				66,
				120,
				6,
				12,
				0,
				0,
				Rectangle.A4_PORTRAIT,
				out);

			Report report = new Report(c);
			report.newPage();
			report.drawGrid();

			report.drawBarcode39(1, 11, "0123456789");
			report.drawBarcode128(11, 11, "0123456789");
			report.drawBarcodeEAN8(21, 11, "0123456");
			report.drawBarcodeEAN13(31, 11, "0123456789012");

			report.close();
		}
	}

	public static Path getDesktopPath() {
		return Paths.get(System.getProperty("user.home"), "Desktop");
	}
}
