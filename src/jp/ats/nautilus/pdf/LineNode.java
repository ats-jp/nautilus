package jp.ats.nautilus.pdf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.ats.nautilus.common.XPathNode;
import jp.ats.nautilus.pdf.Page.LineDirection;
import jp.ats.nautilus.pdf.Report.LineWidth;

class LineNode extends Node {

	private final LineDirection direction;

	private final LineWidth width;

	private final int row, column, length;

	static boolean isTarget(XPathNode node) {
		return "line".equals(node.getNodeName());
	}

	LineNode(
		LineDirection direction,
		LineWidth width,
		int row,
		int column,
		int length) {
		this.direction = direction;
		this.width = width;
		this.row = row;
		this.column = column;
		this.length = length;
	}

	LineNode(XPathNode node) {
		direction = LineDirection
			.valueOf(node.selectNode("@direction").getNodeValue());
		width = LineWidth.valueOf(node.selectNode("@width").getNodeValue());
		row = Integer.parseInt(node.selectNode("@row").getNodeValue());
		column = Integer.parseInt(node.selectNode("@column").getNodeValue());
		length = Integer.parseInt(node.selectNode("@length").getNodeValue());
	}

	@Override
	void draw(Report report) {
		switch (direction) {
		case HORIZONTAL:
			report.drawHorizontalLine(width, row, column, length);
			break;
		case VERTICAL:
			report.drawVerticalLine(width, row, column, length);
			break;
		default:
			throw new IllegalStateException();
		}
	}

	@Override
	void appendSelf(Document document, Element parent) {
		Element self = document.createElement("line");
		self.setAttribute("direction", direction.name());
		self.setAttribute("width", width.name());
		self.setAttribute("row", String.valueOf(row));
		self.setAttribute("column", String.valueOf(column));
		self.setAttribute("length", String.valueOf(length));
		parent.appendChild(self);
	}
}
