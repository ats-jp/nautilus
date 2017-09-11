package jp.ats.nautilus.pdf;

import java.io.IOException;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;

import jp.ats.nautilus.pdf.TemplateManager.Template;

public class Report {

	private static final float lineWidth = 0.1f;

	private static final float boldlineWidth = 0.5f;

	private static final float underlineWidth = 0.5f;

	private static final float inchPoint = 72; // Points per Inch

	private final float fontPointBase;

	private final float fontWidthBase;

	private final float rateX;

	private final BaseFont font;

	private final BaseFont externalFont;

	private final float cellHeight;

	private final float cellWidth;

	private final float fontPoint;

	private final Canvas canvas;

	public static enum LineWidth {

		NORMAL {

			@Override
			LineProcess getProcess() {
				return LineProcess.LINE;
			}
		},

		BOLD {

			@Override
			LineProcess getProcess() {
				return LineProcess.BOLDLINE;
			}
		};

		abstract LineProcess getProcess();
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

	private static class DefaultFontManager implements FontManager {

		private final BaseFont font;

		private DefaultFontManager() throws IOException, DocumentException {
			font = BaseFont.createFont(
				"c:/windows/fonts/msmincho.ttc,0",
				BaseFont.IDENTITY_H,
				BaseFont.EMBEDDED);
		}

		@Override
		public BaseFont createFont() {
			return font;
		}

		@Override
		public BaseFont createExternalFont() {
			return null;
		}
	}

	public Report(Canvas canvas) throws IOException, DocumentException {
		this(canvas, new DefaultFontManager());
	}

	public Report(Canvas canvas, FontManager fontManager) {
		this.canvas = canvas;

		fontWidthBase = inchPoint / 15; // 1inch / maxCPI
		fontPointBase = fontWidthBase * 2;

		float lpi = canvas.getLPI();
		float cpi = canvas.getCPI();

		cellHeight = inchPoint / lpi;
		cellWidth = inchPoint / cpi;

		if (cellWidth * 2 > cellHeight) {
			//横長
			fontPoint = cellHeight;
			rateX = lpi * 2 / cpi;
		} else {
			//縦長 or 縦 = 横 * 2
			fontPoint = cellWidth * 2;
			rateX = 1;
		}

		font = fontManager.createFont();
		externalFont = fontManager.createExternalFont();
	}

	public void drawTemplate(Template template) {
		LineProcess.OTHER.prepare(current, this);
		template.draw(canvas);
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

			drawHorizontalLine(LineWidth.NORMAL, i, 0, columns);

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

			drawVerticalLine(LineWidth.NORMAL, 1, i, rows);

			if (i % 10 == 0) {
				canvas.stroke();
				canvas.setGrayStroke(gray);
			}
		}

		canvas.stroke();

		canvas.restoreState();
	}

	public void drawHorizontalLine(
		LineWidth width,
		int startLine,
		int startColumn,
		int length) {
		width.getProcess().prepare(current, this);

		float x = startColumn * cellWidth;
		float y = startLine * cellHeight;
		canvas.line(x, y, x + length * cellWidth, y);
	}

	public void drawVerticalLine(
		LineWidth width,
		int startLine,
		int startColumn,
		int length) {
		width.getProcess().prepare(current, this);

		float x = startColumn * cellWidth;
		float y = (startLine - 1) * cellHeight;
		canvas.line(x, y, x, y + length * cellHeight);
	}

	public void drawText(
		int startLine,
		int startColumn,
		int horizontalSize,
		int verticalSize,
		String text) {
		LineProcess.OTHER.prepare(current, this);

		canvas.setFontAndSize(font, fontPoint);

		//下線のための上昇分
		float rise = (cellHeight - fontPoint) * verticalSize;

		canvas.showText(
			horizontalSize * rateX,
			verticalSize,
			(startColumn - 1) * cellWidth,
			(startLine + verticalSize - 1) * cellHeight
				+ font.getFontDescriptor(BaseFont.DESCENT, fontPoint)
					* verticalSize
				- rise,
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
		for (char c : text.toCharArray()) {
			if (!externalFont.charExists(c)) {
				invisibleCharactersOffset++;
			} else {
				break;
			}
		}

		canvas.showText(
			horizontalSize,
			verticalSize,
			(startColumn + invisibleCharactersOffset - 1) * cellWidth,
			(startLine + verticalSize - 1) * cellHeight
				+ externalFont
					.getFontDescriptor(BaseFont.DESCENT, fontPointBase)
					* verticalSize
				- rise,
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

	public void drawQrcode(
		int startLine,
		int startColumn,
		int width,
		String encoding,
		String qrcode)
		throws DocumentException {
		LineProcess.OTHER.prepare(current, this);

		canvas.qrcode(
			(startColumn - 1) * cellWidth,
			(startLine - 1) * cellHeight,
			width,
			encoding,
			qrcode);
	}

	public void drawBarcode39(int startLine, int startColumn, String barcode)
		throws DocumentException {
		drawBarcode(Canvas.BARCODE_39_DEFAULT, startLine, startColumn, barcode);
	}

	public void drawBarcode128(int startLine, int startColumn, String barcode)
		throws DocumentException {
		drawBarcode(
			Canvas.BARCODE_128_DEFAULT,
			startLine,
			startColumn,
			barcode);
	}

	public void drawBarcodeEAN(int startLine, int startColumn, String barcode)
		throws DocumentException {
		drawBarcode(
			Canvas.BARCODE_EAN_DEFAULT,
			startLine,
			startColumn,
			barcode);
	}

	public void newPage() {
		LineProcess.OTHER.prepare(current, this);
		canvas.newPage();
	}

	public void close() {
		LineProcess.OTHER.prepare(current, this);
		canvas.close();
	}

	public boolean containsExternalFont(String text) {
		if (externalFont == null) return false;

		char[] chars = text.toCharArray();
		for (char c : chars) {
			if (externalFont.charExists(c)) return true;
		}

		return false;
	}

	@Override
	protected void finalize() {
		close();
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