package jp.ats.nautilus.common;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * @author 千葉 哲嗣
 */
public class XPathNode {

	private static final XPathNode[] emptyArray = {};

	private final Node node;

	public XPathNode(InputStream xml) throws IOException, XPathException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			node = builder.parse(xml);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new XPathException(e.getMessage());
		}
	}

	public XPathNode(InputStream xml, EntityResolver resolver)
		throws IOException, XPathException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(resolver);
			node = builder.parse(xml);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new XPathException(e.getMessage());
		}
	}

	private XPathNode(Node context) {
		node = context;
	}

	public XPathNode[] selectNodes(String xpath) throws XPathException {
		try {
			XPath xpathObject = XPathFactory.newInstance().newXPath();

			NodeList list = (NodeList) xpathObject.evaluate(
				xpath,
				node,
				XPathConstants.NODESET);

			if (list == null) return emptyArray;

			int length = list.getLength();
			XPathNode[] nodes = new XPathNode[length];
			for (int i = 0; i < length; i++) {
				nodes[i] = new XPathNode(list.item(i));
			}
			return nodes;
		} catch (XPathExpressionException e) {
			throw new XPathException(e.getMessage());
		}
	}

	public XPathNode selectNode(String xpath) throws XPathException {
		try {
			XPath xpathObject = XPathFactory.newInstance().newXPath();

			Node sub = (Node) xpathObject.evaluate(
				xpath,
				node,
				XPathConstants.NODE);

			if (sub == null) return null;

			return new XPathNode(sub);
		} catch (XPathExpressionException e) {
			throw new XPathException(e.getMessage());
		}
	}

	public String getNodeValue() {
		return node.getNodeValue();
	}

	public String getTextContent() {
		return node.getTextContent();
	}

	public String getNodeName() {
		return node.getNodeName();
	}

	@Override
	public String toString() {
		return U.toString(this);
	}
}
