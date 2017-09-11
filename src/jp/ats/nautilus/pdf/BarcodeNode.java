package jp.ats.nautilus.pdf;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itextpdf.text.DocumentException;

import jp.ats.nautilus.common.U;
import jp.ats.nautilus.common.XPathNode;

class BarcodeNode extends Node {

	private static final String name = "barcode";

	private final int row, column;

	private final String factoryClass, barcode;

	static boolean isTarget(XPathNode node) {
		return name.equals(node.getNodeName());
	}

	BarcodeNode(int row, int column, String factoryClass, String barcode) {
		Objects.requireNonNull(factoryClass);

		this.row = row;
		this.column = column;
		this.factoryClass = factoryClass.trim();
		this.barcode = barcode;
	}

	BarcodeNode(XPathNode node) {
		row = Integer.parseInt(node.selectNode("@row").getNodeValue());
		column = Integer.parseInt(node.selectNode("@column").getNodeValue());
		factoryClass = node.selectNode("@factory-class").getNodeValue();
		barcode = node.getTextContent();
	}

	@Override
	void draw(Report report) {
		BarcodeFactory factory = U.newInstance(factoryClass);

		try {
			report.drawBarcode(factory, row, column, barcode);
		} catch (DocumentException e) {
			throw new NautilusException(e);
		}
	}

	@Override
	void appendSelf(Document document, Element parent) {
		Element self = document.createElement(name);
		self.setAttribute("row", String.valueOf(row));
		self.setAttribute("column", String.valueOf(column));
		self.setAttribute("factory-class", factoryClass);
		self.setTextContent(barcode);
		parent.appendChild(self);
	}
}
