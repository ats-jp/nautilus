package jp.ats.nautilus.pdf;

import com.itextpdf.text.Rectangle;

public enum PageSize {

	/**
	 * A4縦
	 */
	A4_PORTRAIT {

		@Override
		public Rectangle getRectangle() {
			return com.itextpdf.text.PageSize.A4;
		}
	},

	/**
	 * A4横
	 */
	A4_LANDSCAPE {

		@Override
		public Rectangle getRectangle() {
			return com.itextpdf.text.PageSize.A4_LANDSCAPE;
		}
	},

	/**
	 * A3縦
	 */
	A3_PORTRAIT {

		@Override
		public Rectangle getRectangle() {
			return com.itextpdf.text.PageSize.A3;
		}
	},

	/**
	 * A3横
	 */
	A3_LANDSCAPE {

		@Override
		public Rectangle getRectangle() {
			return com.itextpdf.text.PageSize.A3.rotate();
		}
	};

	public abstract Rectangle getRectangle();
}
