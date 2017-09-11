package jp.ats.nautilus.pdf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.itextpdf.text.DocumentException;

import jp.ats.nautilus.common.XPathNode;

class QrcodeNode extends Node {

	private final int row, column;

	private final int width;

	private final String encoding;

	private final String qrcode;

	static boolean isTarget(XPathNode node) {
		return "qrcode".equals(node.getNodeName());
	}

	QrcodeNode(int row, int column, int width, String encoding, String qrcode) {
		this.row = row;
		this.column = column;
		this.width = width;
		this.encoding = encoding;
		this.qrcode = qrcode;
	}

	QrcodeNode(XPathNode node) {
		row = Integer.parseInt(node.selectNode("@row").getNodeValue());
		column = Integer.parseInt(node.selectNode("@column").getNodeValue());
		width = Integer.parseInt(node.selectNode("@width").getNodeValue());
		encoding = node.selectNode("@encoding").getNodeValue();
		qrcode = node.getTextContent();
	}

	@Override
	void draw(Report report) {
		try {
			report.drawQrcode(row, column, width, encoding, qrcode);
		} catch (DocumentException e) {
			throw new NautilusException(e);
		}
	}

	@Override
	void appendSelf(Document document, Element parent) {
		Element self = document.createElement("qrcode");
		self.setAttribute("row", String.valueOf(row));
		self.setAttribute("column", String.valueOf(column));
		self.setAttribute("width", String.valueOf(width));
		self.setAttribute("encoding", encoding);
		self.setTextContent(qrcode);
		parent.appendChild(self);
	}
}
