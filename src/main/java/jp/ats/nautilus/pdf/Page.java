package jp.ats.nautilus.pdf;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.ats.nautilus.common.U;
import jp.ats.nautilus.pdf.Report.LineWidth;

public class Page implements Comparable<Page> {

	public enum LineDirection {

		HORIZONTAL,

		VERTICAL
	}

	private final List<Node> nodes = U.newLinkedList();

	private final int number;

	Page(int number) {
		this.number = number;
	}

	public void addLine(
		LineDirection direction,
		LineWidth width,
		int row,
		int column,
		int length) {
		nodes.add(new LineNode(direction, width, row, column, length));
	}

	public void addText(
		int row,
		int column,
		int horizontalSize,
		int verticalSize,
		String text) {
		nodes
			.add(new TextNode(row, column, horizontalSize, verticalSize, text));
	}

	public void addUnderLine(
		int row,
		int column,
		int horizontalSize,
		int verticalSize,
		int length) {
		nodes.add(
			new UnderlineNode(
				row,
				column,
				horizontalSize,
				verticalSize,
				length));
	}

	public void addBarcode(
		int row,
		int column,
		String barcodeFactoryClass,
		String barcode) {
		nodes.add(new BarcodeNode(row, column, barcodeFactoryClass, barcode));
	}

	void addNode(Node node) {
		nodes.add(node);
	}

	void draw(Report report) {
		for (Node node : nodes) {
			node.draw(report);
		}
	}

	void appendSelf(Document document, Element parent) {
		Element self = document.createElement("page");
		self.setAttribute("number", String.valueOf(number));
		parent.appendChild(self);

		for (Node node : nodes) {
			node.appendSelf(document, self);
		}
	}

	@Override
	public int compareTo(Page other) {
		return number - other.number;
	}
}
