package jp.ats.nautilus.pdf;

import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.Barcode;
import com.itextpdf.text.pdf.Barcode128;
import com.itextpdf.text.pdf.Barcode39;
import com.itextpdf.text.pdf.BarcodeEAN;
import com.itextpdf.text.pdf.BarcodeQRCode;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.qrcode.EncodeHintType;

public class Canvas {

	public static final BarcodeFactory BARCODE_39_DEFAULT = new SimpleBarcodeFactory(
		Barcode39.class,
		0.8f,
		2.0f,
		8f,
		8f,
		20f);

	public static final BarcodeFactory BARCODE_128_DEFAULT = new SimpleBarcodeFactory(
		Barcode128.class,
		0.8f,
		0.0f,
		8f,
		8f,
		20f);

	public static final BarcodeFactory BARCODE_EAN_DEFAULT = new SimpleBarcodeFactory(
		BarcodeEAN.class,
		0.8f,
		0.0f,
		8f,
		8f,
		20f);

	private static final float inchPoint = 72; // Points per Inch

	private final int rows;

	private final int columns;

	private final float lpi;

	private final float cpi;

	private final float startX;

	private final float startY;

	private final Document document;

	private final PdfContentByte pdf;

	public Canvas(
		int rows,
		int columns,
		float lpi,
		float cpi,
		float marginLeftMM, // A4での長さ
		float marginTopMM, // A4での長さ
		Rectangle pageSize,
		OutputStream output)
		throws DocumentException {
		this.rows = rows;
		this.columns = columns;
		this.lpi = lpi;
		this.cpi = cpi;

		float marginLeftPoint = inchPoint / 25.4f * marginLeftMM;
		float marginTopPoint = inchPoint / 25.4f * marginTopMM;

		startX = marginLeftPoint;
		startY = pageSize.getHeight() - marginTopPoint;

		document = new Document(pageSize);

		PdfWriter writer = PdfWriter.getInstance(document, output);

		document.open();

		writer.setFullCompression();

		pdf = writer.getDirectContent();
	}

	void addTemplate(PdfReader reader, int page) {
		pdf.addTemplate(pdf.getPdfWriter().getImportedPage(reader, page), 0, 0);
	}

	void saveState() {
		pdf.saveState();
	}

	void restoreState() {
		pdf.restoreState();
	}

	void setLineWidth(float width) {
		pdf.setLineWidth(width);
	}

	void setGrayStroke(float gray) {
		pdf.setGrayStroke(gray);
	}

	void line(float fromX, float fromY, float toX, float toY) {
		pdf.moveTo(startX + fromX, startY - fromY);
		pdf.lineTo(startX + toX, startY - toY);
	}

	void stroke() {
		pdf.stroke();
	}

	void setFontAndSize(BaseFont font, float size) {
		pdf.setFontAndSize(font, size);
	}

	void showText(
		float horizontalSize,
		float verticalSize,
		float x,
		float y,
		String text) {
		pdf.beginText();
		pdf.setTextMatrix(
			horizontalSize,
			0,
			0,
			verticalSize,
			startX + x,
			startY - y);
		pdf.showText(text);
		pdf.endText();
	}

	void barcode(BarcodeFactory factory, float x, float y, String barcode)
		throws DocumentException {
		Barcode code = factory.create(barcode);

		Image image = code
			.createImageWithBarcode(pdf, BaseColor.BLACK, BaseColor.BLACK);

		float height = image.getHeight();

		pdf.addImage(
			image,
			image.getWidth(),
			0,
			0,
			height,
			startX + x,
			startY - y - height);
	}

	void qrcode(float x, float y, int width, String encoding, String qrcode)
		throws DocumentException {

		Map<EncodeHintType, Object> hint = new ConcurrentHashMap<>();
		if (encoding != null) {
			hint.put(EncodeHintType.CHARACTER_SET, encoding);
		}

		BarcodeQRCode code = new BarcodeQRCode(
			qrcode.trim(),
			width,
			width,
			hint);
		Image image = code.getImage();

		float height = image.getHeight();

		pdf.addImage(
			image,
			image.getWidth(),
			0,
			0,
			height,
			startX + x,
			startY - y - height);
	}

	void setCharacterSpacing(float space) {
		pdf.setCharacterSpacing(space);
	}

	public void newPage() {
		document.newPage();
	}

	public void close() {
		document.close();
	}

	int getRows() {
		return rows;
	}

	int getColumns() {
		return columns;
	}

	float getLPI() {
		return lpi;
	}

	float getCPI() {
		return cpi;
	}

	@Override
	protected void finalize() {
		close();
	}
}
