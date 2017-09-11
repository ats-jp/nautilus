package jp.ats.nautilus.pdf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.ats.nautilus.common.XPathNode;

class UnderlineNode extends Node {

	private final int row, column, horizontalSize, verticalSize, length;

	static boolean isTarget(XPathNode node) {
		return "underline".equals(node.getNodeName());
	}

	UnderlineNode(
		int row,
		int column,
		int horizontalSize,
		int verticalSize,
		int length) {
		this.row = row;
		this.column = column;
		this.horizontalSize = horizontalSize;
		this.verticalSize = verticalSize;
		this.length = length;
	}

	UnderlineNode(XPathNode node) {
		row = Integer.parseInt(node.selectNode("@row").getNodeValue());
		column = Integer.parseInt(node.selectNode("@column").getNodeValue());
		horizontalSize = Integer
			.parseInt(node.selectNode("@horizontal-size").getNodeValue());
		verticalSize = Integer
			.parseInt(node.selectNode("@vertical-size").getNodeValue());
		length = Integer.parseInt(node.selectNode("@length").getNodeValue());
	}

	@Override
	void draw(Report report) {
		report.drawUnderline(row, column, horizontalSize, verticalSize, length);
	}

	@Override
	void appendSelf(Document document, Element parent) {
		Element self = document.createElement("underline");
		self.setAttribute("row", String.valueOf(row));
		self.setAttribute("column", String.valueOf(column));
		self.setAttribute("horizontal-size", String.valueOf(horizontalSize));
		self.setAttribute("vertical-size", String.valueOf(verticalSize));
		self.setAttribute("length", String.valueOf(length));
		parent.appendChild(self);
	}
}
