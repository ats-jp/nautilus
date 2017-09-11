package jp.ats.nautilus.pdf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.ats.nautilus.common.XPathNode;

class TextNode extends Node {

	private final int row, column, horizontalSize, verticalSize;

	private final String text;

	static boolean isTarget(XPathNode node) {
		return "text".equals(node.getNodeName());
	}

	TextNode(
		int row,
		int column,
		int horizontalSize,
		int verticalSize,
		String text) {
		this.row = row;
		this.column = column;
		this.horizontalSize = horizontalSize;
		this.verticalSize = verticalSize;
		this.text = text;
	}

	TextNode(XPathNode node) {
		row = Integer.parseInt(node.selectNode("@row").getNodeValue());
		column = Integer.parseInt(node.selectNode("@column").getNodeValue());
		horizontalSize = Integer
			.parseInt(node.selectNode("@horizontal-size").getNodeValue());
		verticalSize = Integer
			.parseInt(node.selectNode("@vertical-size").getNodeValue());
		text = node.getTextContent();
	}

	@Override
	void draw(Report report) {
		if (report.containsExternalFont(text)) {
			report.drawExternalFontText(
				row,
				column,
				horizontalSize,
				verticalSize,
				text);
		} else {
			report.drawText(row, column, horizontalSize, verticalSize, text);
		}
	}

	@Override
	void appendSelf(Document document, Element parent) {
		Element self = document.createElement("text");
		self.setAttribute("row", String.valueOf(row));
		self.setAttribute("column", String.valueOf(column));
		self.setAttribute("horizontal-size", String.valueOf(horizontalSize));
		self.setAttribute("vertical-size", String.valueOf(verticalSize));
		self.setTextContent(text);
		parent.appendChild(self);
	}
}
