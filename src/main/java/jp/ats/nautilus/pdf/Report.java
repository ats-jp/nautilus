package jp.ats.nautilus.pdf;

import java.awt.Color;
import java.nio.CharBuffer;

import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.impl.code39.Code39Bean;
import org.krysalis.barcode4j.impl.upcean.EAN13Bean;
import org.krysalis.barcode4j.impl.upcean.EAN8Bean;

import jp.ats.nautilus.common.Constants;

public class Report implements AutoCloseable {

	private static final float lineWidth = 0.1f;

	private static final float boldlineWidth = 0.5f;

	private static final float underlineWidth = 0.1f;

	private static final float[] lineDashPattern = { 2f, 2f };

	private static final float lineDashPatternPhase = 2f;

	private final float fontPointBase;

	private final float fontWidthBase;

	private final float rateX;

	private final Font font;

	private final Font externalFont;

	private final float cellHeight;

	private final float cellWidth;

	private final float fontPoint;

	private final Canvas canvas;

	public static enum LineType {

		NORMAL {

			@Override
			LineProcess getProcess() {
				return LineProcess.LINE;
			}

			@Override
			void line(
				Canvas canvas,
				float fromX,
				float fromY,
				float toX,
				float toY) {
				canvas.line(fromX, fromY, toX, toY);
			}
		},

		BOLD {

			@Override
			LineProcess getProcess() {
				return LineProcess.BOLDLINE;
			}

			@Override
			void line(
				Canvas canvas,
				float fromX,
				float fromY,
				float toX,
				float toY) {
				canvas.line(fromX, fromY, toX, toY);
			}
		},

		DASH {

			@Override
			LineProcess getProcess() {
				return LineProcess.DASHLINE;
			}

			@Override
			void line(
				Canvas canvas,
				float fromX,
				float fromY,
				float toX,
				float toY) {
				canvas.dashLine(
					lineDashPattern,
					lineDashPatternPhase,
					fromX,
					fromY,
					toX,
					toY);
			}
		},

		DOUBLE {

			@Override
			LineProcess getProcess() {
				return LineProcess.LINE;
			}

			@Override
			void line(
				Canvas canvas,
				float fromX,
				float fromY,
				float toX,
				float toY) {
				float distance = 0.5f;
				if (fromX == toX) {
					//垂直線
					canvas.line(fromX - distance, fromY, toX - distance, toY);
					canvas.line(fromX + distance, fromY, toX + distance, toY);
				} else if (fromY == toY) {
					//水平線
					canvas.line(fromX, fromY - distance, toX, toY - distance);
					canvas.line(fromX, fromY + distance, toX, toY + distance);
				} else {
					throw new IllegalStateException(
						"invalid line:"
							+ " fromX="
							+ fromX
							+ ", fromY="
							+ fromY
							+ ", toX="
							+ toX
							+ ", toY="
							+ toY);
				}
			}
		};

		abstract LineProcess getProcess();

		abstract void line(
			Canvas canvas,
			float fromX,
			float fromY,
			float toX,
			float toY);
	}

	/**
	 * どのような描画順序でも、線描画前に startLineDraw, 線描画後に endLineDraw を実行するための enum
	 */
	private static enum LineProcess {

		LINE {

			@Override
			void start(Report report) {
				report.startLineDraw(lineWidth);
			}

			@Override
			void end(Report report) {
				report.endLineDraw();
			}
		},

		BOLDLINE {

			@Override
			void start(Report report) {
				report.startLineDraw(boldlineWidth);
			}

			@Override
			void end(Report report) {
				report.endLineDraw();
			}
		},

		DASHLINE {

			@Override
			void start(Report report) {
				report.startLineDraw(boldlineWidth);
			}

			@Override
			void end(Report report) {
				report.endLineDraw();
			}
		},

		UNDERLINE {

			@Override
			void start(Report report) {
				report.startLineDraw(underlineWidth);
			}

			@Override
			void end(Report report) {
				report.endLineDraw();
			}
		},

		OTHER {

			@Override
			void start(Report report) {}

			@Override
			void end(Report report) {}
		};

		abstract void start(Report report);

		abstract void end(Report report);

		private void prepare(LineProcess current, Report report) {
			if (this != current) {
				current.end(report);
				start(report);
				report.changeLineProcess(this);
			}
		}
	}

	private LineProcess current = LineProcess.OTHER;

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

	public Report(Canvas canvas) {
		this(canvas, new SimpleFontManager());
	}

	public Report(Canvas canvas, FontManager fontManager) {
		this.canvas = canvas;

		fontWidthBase = Canvas.inchPoint / 15; // 1inch / maxCPI
		fontPointBase = fontWidthBase * 2;

		float lpi = canvas.getLPI();
		float cpi = canvas.getCPI();

		cellHeight = Canvas.inchPoint / lpi;
		cellWidth = Canvas.inchPoint / cpi;

		if (cellWidth * 2 > cellHeight) {
			//横長
			fontPoint = cellHeight;
			rateX = lpi * 2 / cpi;
		} else {
			//縦長 or 縦 = 横 * 2
			fontPoint = cellWidth * 2;
			rateX = 1;
		}

		font = fontManager.getFont();
		externalFont = fontManager.getExternalFont();
	}

	public void setTemplatePage(TemplatePage templatePage) {
		LineProcess.OTHER.prepare(current, this);
		canvas.setTemplateDocument(templatePage.getTemplate());
		canvas.selectTemplatePage(templatePage.getPageIndex());
	}

	public void setTemplate(Template template) {
		LineProcess.OTHER.prepare(current, this);
		canvas.setTemplateDocument(template);
	}

	public void selectTemplatePage(int pageIndex) {
		canvas.selectTemplatePage(pageIndex);
	}

	public void drawGrid() {
		LineProcess.OTHER.prepare(current, this);

		canvas.saveState();

		canvas.setLineWidth(lineWidth);

		float gray = 0.8f;
		float dark = 0.6f;

		canvas.setGrayStroke(gray);

		int rows = canvas.getRows();
		int columns = canvas.getColumns();

		for (int i = 0; i <= rows; i++) {
			if (i % 10 == 0) {
				canvas.stroke();
				canvas.setGrayStroke(dark);
			}

			drawHorizontalLine(LineType.NORMAL, i, 0, columns);

			if (i % 10 == 0) {
				canvas.stroke();
				canvas.setGrayStroke(gray);
			}
		}

		for (int i = 0; i <= columns; i++) {
			if (i % 10 == 0) {
				canvas.stroke();
				canvas.setGrayStroke(dark);
			}

			drawVerticalLine(LineType.NORMAL, 1, i, rows);

			if (i % 10 == 0) {
				canvas.stroke();
				canvas.setGrayStroke(gray);
			}
		}

		canvas.stroke();

		canvas.restoreState();
	}

	public void drawHorizontalLine(
		LineType type,
		int startLine,
		int startColumn,
		int length) {
		type.getProcess().prepare(current, this);

		float x = startColumn * cellWidth;
		float y = startLine * cellHeight;
		type.line(canvas, x, y, x + length * cellWidth, y);
	}

	public void drawVerticalLine(
		LineType type,
		int startLine,
		int startColumn,
		int length) {
		type.getProcess().prepare(current, this);

		float x = startColumn * cellWidth;
		float y = (startLine - 1) * cellHeight;
		type.line(canvas, x, y, x, y + length * cellHeight);
	}

	public void drawMixedFontText(
		int startLine,
		int startColumn,
		int horizontalSize,
		int verticalSize,
		String text) {
		LineProcess.OTHER.prepare(current, this);

		CharBuffer buffer = CharBuffer.allocate(text.length());
		boolean isExternalFont = false;
		int position = startColumn;
		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (canvas.charExists(externalFont, chars, i)) {
				if (!isExternalFont && buffer.position() > 0) {
					position = flushText(
						font,
						buffer,
						startLine,
						position,
						horizontalSize,
						verticalSize);
				}

				isExternalFont = true;
				buffer.put(text.charAt(i));
			} else {
				if (isExternalFont && buffer.position() > 0) {
					position = flushText(
						externalFont,
						buffer,
						startLine,
						position,
						horizontalSize,
						verticalSize);
				}

				isExternalFont = false;
				buffer.put(text.charAt(i));
			}
		}

		flushText(
			isExternalFont ? externalFont : font,
			buffer,
			startLine,
			position,
			horizontalSize,
			verticalSize);
	}

	public void drawText(
		int startLine,
		int startColumn,
		int horizontalSize,
		int verticalSize,
		String text) {
		drawText(
			font,
			startLine,
			startColumn,
			horizontalSize,
			verticalSize,
			text);
	}

	public void drawExternalFontText(
		int startLine,
		int startColumn,
		int horizontalSize,
		int verticalSize,
		String text) {
		LineProcess.OTHER.prepare(current, this);

		canvas.saveState();

		canvas.setCharacterSpacing((cellWidth - fontWidthBase) * 2);

		canvas.setFontAndSize(externalFont, fontPointBase);

		//下線のための上昇分
		float rise = (cellHeight - fontPointBase) * verticalSize;

		//外字フォント内に制御文字や空白などが含まれていない場合
		//非表示となってしまうため、先頭だけでもずらすための処置
		int invisibleCharactersOffset = 0;

		char[] chars = text.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (!canvas.charExists(externalFont, chars, i)) {
				invisibleCharactersOffset++;
			} else {
				break;
			}
		}

		canvas.showText(
			horizontalSize,
			verticalSize,
			(startColumn + invisibleCharactersOffset - 1) * cellWidth,
			(startLine + verticalSize - 1) * cellHeight - rise,
			text);

		canvas.restoreState();
	}

	public void drawUnderline(
		int startLine,
		int startColumn,
		int horizontalSize,
		int verticalSize,
		int length) {
		LineProcess.UNDERLINE.prepare(current, this);

		//下線のための上昇分
		float rise = (cellHeight - fontPoint) * verticalSize;

		float x = (startColumn - 1) * cellWidth;
		float y = (startLine + verticalSize - 1) * cellHeight - rise;

		float myHorizontalSize = horizontalSize * rateX;

		float adjust = (cellWidth - fontWidthBase) / 2 * myHorizontalSize;

		float lineLength = cellWidth * myHorizontalSize;

		canvas.line(x + adjust, y, x + lineLength - adjust, y);

		for (int i = 1; i < length; i++) {
			x += cellWidth * myHorizontalSize;

			canvas.line(x + adjust, y, x + lineLength - adjust, y);
		}
	}

	public void drawBarcode(
		BarcodeFactory factory,
		int startLine,
		int startColumn,
		String barcode)
		throws DocumentException {
		LineProcess.OTHER.prepare(current, this);

		canvas.barcode(
			factory,
			(startColumn - 1) * cellWidth,
			(startLine - 1) * cellHeight,
			barcode);
	}

	public void drawBarcode39(int startLine, int startColumn, String barcode)
		throws DocumentException {
		drawBarcode(BARCODE_39_DEFAULT, startLine, startColumn, barcode);
	}

	public void drawBarcode128(int startLine, int startColumn, String barcode)
		throws DocumentException {
		drawBarcode(BARCODE_128_DEFAULT, startLine, startColumn, barcode);
	}

	public void drawBarcodeEAN8(int startLine, int startColumn, String barcode)
		throws DocumentException {
		drawBarcode(BARCODE_EAN8_DEFAULT, startLine, startColumn, barcode);
	}

	public void drawBarcodeEAN13(int startLine, int startColumn, String barcode)
		throws DocumentException {
		drawBarcode(BARCODE_EAN13_DEFAULT, startLine, startColumn, barcode);
	}

	public void drawRectangle(
		int startLine,
		int startColumn,
		int widthLength,
		int heightLength,
		Color color) {
		LineProcess.OTHER.prepare(current, this);

		float x = (startColumn - 1) * cellWidth;
		float y = (startLine - 1) * cellHeight;
		float width = widthLength * cellWidth;
		float height = heightLength * cellHeight;

		canvas.rectangle(x, y, width, height, color);
	}

	public void fill(Color color) {
		canvas.fill(color);
	}

	public void newPage() {
		LineProcess.OTHER.prepare(current, this);
		canvas.newPage();
	}

	public int getNumberOfPages() {
		return canvas.getNumberOfPages();
	}

	@Override
	public void close() {
		LineProcess.OTHER.prepare(current, this);
		canvas.save();
		canvas.close();
	}

	public boolean containsExternalFont(String text) {
		if (externalFont == null) return false;
		return canvas.charExists(externalFont, text);
	}

	private int flushText(
		Font font,
		CharBuffer buffer,
		int startLine,
		int startColumn,
		int horizontalSize,
		int verticalSize) {
		char[] chars = new char[buffer.position()];
		buffer.flip();
		buffer.get(chars);
		buffer.clear();
		String text = new String(chars);
		int length = text.getBytes(Constants.MEASURE_CHARSET).length;
		drawText(
			font,
			startLine,
			startColumn,
			horizontalSize,
			verticalSize,
			text);

		return startColumn + length;
	}

	private void drawText(
		Font font,
		int startLine,
		int startColumn,
		int horizontalSize,
		int verticalSize,
		String text) {
		LineProcess.OTHER.prepare(current, this);

		canvas.saveState();

		canvas.setFontAndSize(font, fontPoint);

		//下線のための上昇分
		float rise = (cellHeight - fontPoint) * verticalSize;

		canvas.showText(
			horizontalSize * rateX,
			verticalSize,
			(startColumn - 1) * cellWidth,
			(startLine + verticalSize - 1) * cellHeight - rise,
			text);

		canvas.restoreState();
	}

	private void startLineDraw(float lineWidth) {
		canvas.saveState();
		canvas.setLineWidth(lineWidth);
	}

	private void endLineDraw() {
		canvas.stroke();
		canvas.restoreState();
	}

	private void changeLineProcess(LineProcess next) {
		current = next;
	}
}
