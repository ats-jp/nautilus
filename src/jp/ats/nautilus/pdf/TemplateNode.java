package jp.ats.nautilus.pdf;

import java.io.File;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.ats.nautilus.common.XPathNode;
import jp.ats.nautilus.pdf.TemplateManager.Template;

class TemplateNode extends Node {

	private final Template template;

	TemplateNode(Template template) {
		this.template = template;
	}

	TemplateNode(XPathNode node) throws IOException {
		this.template = TemplateManager.createTemplate(
			new File(node.selectNode("@file").getNodeValue()),
			Integer.parseInt(node.selectNode("@page").getNodeValue()));
	}

	@Override
	void draw(Report report) {
		report.drawTemplate(template);
	}

	@Override
	void appendSelf(Document document, Element parent) {
		Element self = document.createElement("template");
		self.setAttribute("file", template.getPDFFile().getAbsolutePath());
		self.setAttribute("page", String.valueOf(template.getPage()));
		parent.appendChild(self);
	}
}
