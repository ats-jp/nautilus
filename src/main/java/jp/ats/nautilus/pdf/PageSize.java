package jp.ats.nautilus.pdf;

import com.lowagie.text.Rectangle;

public enum PageSize {

	/**
	 * A4縦
	 */
	A4_PORTRAIT {

		@Override
		public Rectangle getRectangle() {
			return com.lowagie.text.PageSize.A4;
		}
	},

	/**
	 * A4横
	 */
	A4_LANDSCAPE {

		@Override
		public Rectangle getRectangle() {
			return com.lowagie.text.PageSize.A4.rotate();
		}
	},

	/**
	 * A3縦
	 */
	A3_PORTRAIT {

		@Override
		public Rectangle getRectangle() {
			return com.lowagie.text.PageSize.A3;
		}
	},

	/**
	 * A3横
	 */
	A3_LANDSCAPE {

		@Override
		public Rectangle getRectangle() {
			return com.lowagie.text.PageSize.A3.rotate();
		}
	};

	public abstract Rectangle getRectangle();
}
