package jp.ats.nautilus.pdf;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.krysalis.barcode4j.BarcodeGenerator;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.EAN8Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;

public class Canvas implements AutoCloseable {

	public static final BarcodeFactory BARCODE_39_DEFAULT = new SimpleBarcodeFactory(
		Code39Bean.class,
		200,
		10,
		0.5f);

	public static final BarcodeFactory BARCODE_128_DEFAULT = new SimpleBarcodeFactory(
		Code128Bean.class,
		200,
		10,
		0.5f);

	public static final BarcodeFactory BARCODE_EAN8_DEFAULT = new SimpleBarcodeFactory(
		EAN8Bean.class,
		200,
		10,
		0.5f);

	public static final BarcodeFactory BARCODE_EAN13_DEFAULT = new SimpleBarcodeFactory(
		EAN13Bean.class,
		200,
		10,
		0.5f);

	private static final float inchPoint = 72; // Points per Inch

	private final int rows;

	private final int columns;

	private final float lpi;

	private final float cpi;

	private final float startX;

	private final float startY;

	private final Rectangle rectangle;

	private final PDDocument document;

	private final Map<String, PDType0Font> fontCache = new HashMap<>();

	private final OutputStream output;

	private PDPageContentStream currentStream;

	private PDDocument currentTemplateDocument;

	private PDPage currentTemplatePage;

	private final Map<Integer, PDPage> templatePageCache = new HashMap<>();

	public Canvas(
		int rows,
		int columns,
		float lpi,
		float cpi,
		float marginLeftMM, // A4での長さ
		float marginTopMM, // A4での長さ
		Rectangle rectangle,
		OutputStream output) {
		this.rows = rows;
		this.columns = columns;
		this.lpi = lpi;
		this.cpi = cpi;

		this.rectangle = rectangle;

		this.output = output;

		document = new PDDocument();

		float marginLeftPoint = inchPoint / 25.4f * marginLeftMM;
		float marginTopPoint = inchPoint / 25.4f * marginTopMM;

		startX = marginLeftPoint;
		startY = rectangle.getHeight() - marginTopPoint;
	}

	void setLineWidth(float width) {
		try {
			currentStream.setLineWidth(width);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	void setGrayStroke(float gray) {
		try {
			currentStream.setStrokingColor(gray);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	void line(float fromX, float fromY, float toX, float toY) {
		try {
			currentStream.moveTo(startX + fromX, startY - fromY);
			currentStream.lineTo(startX + toX, startY - toY);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	void stroke() {
		try {
			currentStream.stroke();
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	void setFontAndSize(Font font, float size) {
		String name = font.name();
		PDType0Font pdFont = fontCache.get(name);

		if (pdFont == null) {
			pdFont = font.createPDFont(document);
			fontCache.put(name, pdFont);
		}

		try {
			currentStream.setFont(pdFont, size);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	void showText(
		float horizontalSize,
		float verticalSize,
		float x,
		float y,
		String text) {
		try {
			currentStream.beginText();
			currentStream.setTextMatrix(
				new Matrix(
					horizontalSize,
					0,
					0,
					verticalSize,
					startX + x,
					startY - y));
			currentStream.showText(text);
			currentStream.endText();
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	void barcode(BarcodeFactory factory, float x, float y, String barcode) {
		BarcodeGenerator generator = factory.createBarcodeGenerator();

		factory.decorate(generator);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		BitmapCanvasProvider provider = new BitmapCanvasProvider(
			output,
			"image/x-png",
			factory.dpi(),
			BufferedImage.TYPE_BYTE_BINARY,
			false,
			0);

		generator.generateBarcode(provider, barcode);

		try {
			provider.finish();

			byte[] barcodeBytes = output.toByteArray();

			PDImageXObject image = PDImageXObject
				.createFromByteArray(document, barcodeBytes, null);

			float magnification = factory.magnification();

			float height = image.getHeight() * magnification;
			currentStream.drawImage(
				image,
				new Matrix(
					image.getWidth() * magnification,
					0,
					0,
					height,
					startX + x,
					startY - y - height));
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	void setCharacterSpacing(float space) {
		try {
			currentStream.setCharacterSpacing(space);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	float getFontDescent(Font font, float fontPoint) {
		return fontCache.get(font.name()).getFontDescriptor().getDescent()
			/ 100;
	}

	boolean charExists(Font font, char c) {
		try {
			return fontCache.get(font.name()).hasGlyph(c);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	void setTemplateDocument(byte[] pdf) {
		templatePageCache.clear();
		currentTemplatePage = null;

		try {
			currentTemplateDocument = PDDocument.load(pdf);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	void selectTemplatePage(int pageIndex) {
		currentTemplatePage = templatePageCache.get(pageIndex);

		if (currentTemplatePage == null) {
			currentTemplatePage = currentTemplateDocument.getDocumentCatalog()
				.getPages()
				.get(pageIndex);

			templatePageCache.put(pageIndex, currentTemplatePage);
		}
	}

	void newPage() {
		try {
			if (currentStream != null) {
				currentStream.close();
				currentStream = null;
			}

			PDPage page;
			if (currentTemplatePage == null) {
				page = rectangle.newPage();
			} else {
				page = cloneTemplatePage();
			}

			document.addPage(page);

			currentStream = rectangle.newPageContentStream(document, page);
		} catch (IOException e) {
			throw new DocumentException(e);
		}
	}

	public int getNumberOfPages() {
		return document.getNumberOfPages();
	}

	public void save() {
		try {
			if (currentStream != null) {
				currentStream.close();
				currentStream = null;
			}

			document.save(output);
		} catch (IOException e) {
			e.printStackTrace();
			throw new DocumentException(e);
		}
	}

	@Override
	public void close() {
		try {
			document.close();
		} catch (IOException e) {
			throw new DocumentException(e);
		}
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

	private PDPage cloneTemplatePage() {
		COSDictionary pageDict = currentTemplatePage.getCOSObject();
		COSDictionary newPageDict = new COSDictionary(pageDict);
		newPageDict.removeItem(COSName.ANNOTS);
		return new PDPage(newPageDict);
	}
}
