package jp.ats.nautilus.pdf;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.ats.nautilus.common.U;
import jp.ats.nautilus.common.XPathNode;

public class Nautilus {

	private int rows;

	private int columns;

	private float lpi;

	private float cpi;

	private float marginLeftMM;

	private float marginTopMM;

	private Class<? extends FontManager> fontManagerClass;

	private Rectangle rectangle = Rectangle.A4_LANDSCAPE;

	private List<TemplateNode> templates = U.newLinkedList();

	private List<Page> pages = U.newLinkedList();

	private Page currentPage = null;

	private int pageNumber;

	public void setRows(int rows) {
		this.rows = rows;
	}

	public void setColumns(int columns) {
		this.columns = columns;
	}

	public void setLPI(float lpi) {
		this.lpi = lpi;
	}

	public void setCPI(float cpi) {
		this.cpi = cpi;
	}

	public void setMarginLeft(float marginMM) {
		marginLeftMM = marginMM;
	}

	public void setMarginTop(float marginMM) {
		marginTopMM = marginMM;
	}

	public void setFontManagerClass(
		Class<? extends FontManager> fontManagerClass) {
		this.fontManagerClass = fontManagerClass;
	}

	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}

	public void addTemplate(TemplatePage templatePage) {
		templates.add(new TemplateNode(templatePage));
	}

	public Page currentPage() {
		return currentPage;
	}

	public void newPage() {
		Page page = new Page(++pageNumber);
		pages.add(page);
		currentPage = page;
	}

	public void save(OutputStream output) {
		Document document;
		try {
			document = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder()
				.newDocument();
		} catch (ParserConfigurationException e) {
			throw new IllegalStateException(e);
		}

		document.setXmlStandalone(true);

		Element root = document.createElement("report");
		root.setAttribute("rows", String.valueOf(rows));
		root.setAttribute("columns", String.valueOf(columns));
		root.setAttribute("LPI", String.valueOf(lpi));
		root.setAttribute("CPI", String.valueOf(cpi));
		root.setAttribute("margin-left", String.valueOf(marginLeftMM));
		root.setAttribute("margin-top", String.valueOf(marginTopMM));

		if (fontManagerClass != null)
			root.setAttribute("font-manager", fontManagerClass.getName());

		root.setAttribute("page-size", rectangle.name());
		document.appendChild(root);

		for (TemplateNode node : templates) {
			node.appendSelf(document, root);
		}

		for (Page page : pages) {
			page.appendSelf(document, root);
		}

		try {
			Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();

			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");

			/* 警告よけのため、キーの値を直書き
			transformer.setOutputProperty(
				OutputPropertiesFactory.S_KEY_INDENT_AMOUNT,
				"2");
			*/
			transformer.setOutputProperty(
				"{http://xml.apache.org/xalan}indent-amount",
				"2");

			transformer
				.transform(new DOMSource(document), new StreamResult(output));
		} catch (TransformerException e) {
			throw new IllegalStateException(e);
		}
	}

	public void load(InputStream input) {
		XPathNode root;
		try {
			root = new XPathNode(input);

			XPathNode report = root.selectNode("report");

			rows = Integer.parseInt(report.selectNode("@rows").getNodeValue());
			columns = Integer
				.parseInt(report.selectNode("@columns").getNodeValue());
			lpi = Float.parseFloat(report.selectNode("@LPI").getNodeValue());
			cpi = Float.parseFloat(report.selectNode("@CPI").getNodeValue());
			marginLeftMM = Integer
				.parseInt(report.selectNode("@margin-left").getNodeValue());
			marginTopMM = Integer
				.parseInt(report.selectNode("@margin-top").getNodeValue());

			XPathNode fontManagerNode = report.selectNode("@font-manager");
			if (fontManagerNode != null)
				fontManagerClass = forName(fontManagerNode.getNodeValue());

			rectangle = Rectangle
				.valueOf(report.selectNode("@page-size").getNodeValue());

			XPathNode[] templateNodes = report.selectNodes("template");
			for (XPathNode node : templateNodes) {
				templates.add(new TemplateNode(node));
			}

			XPathNode[] pageNodes = report.selectNodes("page");
			for (XPathNode pageNode : pageNodes) {
				Page page = new Page(
					Integer.parseInt(
						pageNode.selectNode("@number").getNodeValue()));

				for (XPathNode node : pageNode.selectNodes("*")) {
					if (LineNode.isTarget(node)) {
						page.addNode(new LineNode(node));
					} else if (TextNode.isTarget(node)) {
						page.addNode(new TextNode(node));
					} else if (UnderlineNode.isTarget(node)) {
						page.addNode(new UnderlineNode(node));
					} else if (BarcodeNode.isTarget(node)) {
						page.addNode(new BarcodeNode(node));
					} else {
						throw new IllegalStateException(
							"不明なタグ " + node.getNodeName());
					}
				}

				pages.add(page);
				currentPage = page;
			}
		} catch (Exception e) {
			throw new NautilusException(e);
		}
	}

	public void draw(OutputStream output) {
		drawInternal(output, false);
	}

	public void drawWithGrid(OutputStream output) {
		drawInternal(output, true);
	}

	private void drawInternal(OutputStream output, boolean drawsGrid) {
		Report report = null;
		try {
			Canvas canvas = new Canvas(
				rows,
				columns,
				lpi,
				cpi,
				marginLeftMM,
				marginTopMM,
				rectangle,
				output);

			if (fontManagerClass != null) {
				report = new Report(canvas, fontManagerClass.newInstance());
			} else {
				report = new Report(canvas);
			}

			if (templates.size() == 0) {
				for (Page page : pages) {
					if (drawsGrid) report.drawGrid();
					page.draw(report);
					report.newPage();
				}
			} else {
				for (Page page : pages) {
					for (TemplateNode template : templates) {
						if (drawsGrid) report.drawGrid();
						template.draw(report);
						page.draw(report);
						report.newPage();
					}
				}
			}
		} catch (Exception e) {
			throw new NautilusException(e);
		} finally {
			if (report != null) report.close();
		}
	}

	@SuppressWarnings("unchecked")
	private static Class<? extends FontManager> forName(String className) {
		try {
			return (Class<? extends FontManager>) Class.forName(className);
		} catch (Exception e) {
			throw new NautilusException(e);
		}
	}
}
