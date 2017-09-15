package jp.ats.nautilus.pdf;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.util.Matrix;

public enum Rectangle {

	A4_PORTRAIT {

		@Override
		public float getWidth() {
			return PDRectangle.A4.getWidth();
		}

		@Override
		public float getHeight() {
			return PDRectangle.A4.getHeight();
		}

		@Override
		PDPage newPage() {
			return new PDPage(PDRectangle.A4);
		}

		@Override
		void adjust(PDPage page, PDPageContentStream stream) {}
	},

	A4_LANDSCAPE {

		@Override
		public float getWidth() {
			return PDRectangle.A4.getHeight();
		}

		@Override
		public float getHeight() {
			return PDRectangle.A4.getWidth();
		}

		@Override
		PDPage newPage() {
			PDPage page = new PDPage(PDRectangle.A4);
			page.setRotation(90);
			return page;
		}

		@Override
		void adjust(PDPage page, PDPageContentStream stream)
			throws IOException {
			stream.transform(
				new Matrix(0, 1, -1, 0, page.getMediaBox().getWidth(), 0));
		}
	},

	A3_PORTRAIT {

		@Override
		public float getWidth() {
			return PDRectangle.A3.getWidth();
		}

		@Override
		public float getHeight() {
			return PDRectangle.A3.getHeight();
		}

		@Override
		PDPage newPage() {
			return new PDPage(PDRectangle.A3);
		}

		@Override
		void adjust(PDPage page, PDPageContentStream stream) {}
	},

	A3_LANDSCAPE {

		@Override
		public float getWidth() {
			return PDRectangle.A3.getHeight();
		}

		@Override
		public float getHeight() {
			return PDRectangle.A3.getWidth();
		}

		@Override
		PDPage newPage() {
			PDPage page = new PDPage(PDRectangle.A4);
			page.setRotation(90);
			return page;
		}

		@Override
		void adjust(PDPage page, PDPageContentStream stream)
			throws IOException {
			stream.transform(
				new Matrix(0, 1, -1, 0, page.getMediaBox().getWidth(), 0));
		}
	};

	public abstract float getWidth();

	public abstract float getHeight();

	public PDPageContentStream newPageContentStream(
		PDDocument document,
		PDPage page)
		throws IOException {
		PDPageContentStream stream = new PDPageContentStream(
			document,
			page,
			AppendMode.APPEND,
			true);

		adjust(page, stream);

		return stream;
	}

	abstract PDPage newPage();

	abstract void adjust(PDPage page, PDPageContentStream stream)
		throws IOException;
}
