package jp.ats.nautilus.pdf;

import java.io.IOException;
import java.nio.file.Paths;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.ats.nautilus.common.XPathNode;

class TemplateNode extends Node {

	private final Template template;

	TemplateNode(Template template) {
		this.template = template;
	}

	TemplateNode(XPathNode node) throws IOException {
		this.template = TemplateManager.createTemplate(
			Paths.get(node.selectNode("@file").getNodeValue()),
			Integer.parseInt(node.selectNode("@page").getNodeValue()));
	}

	@Override
	void draw(Report report) {
		report.setTemplate(template);
	}

	@Override
	void appendSelf(Document document, Element parent) {
		Element self = document.createElement("template");
		self.setAttribute(
			"file",
			TemplateManager.getTemplatePath(template)
				.toAbsolutePath()
				.toString());
		self.setAttribute("page", String.valueOf(template.getPageIndex()));
		parent.appendChild(self);
	}
}
