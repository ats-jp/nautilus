package jp.ats.nautilus.pdf;

import java.io.IOException;
import java.nio.file.Paths;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import jp.ats.nautilus.internal.XPathNode;

class TemplateNode extends Node {

	private final TemplatePage templatePage;

	TemplateNode(TemplatePage templatePage) {
		this.templatePage = templatePage;
	}

	TemplateNode(XPathNode node) throws IOException {
		this.templatePage = TemplateManager.createTemplatePage(
			Paths.get(node.selectNode("@file").getNodeValue()),
			Integer.parseInt(node.selectNode("@page").getNodeValue()));
	}

	@Override
	void draw(Report report) {
		report.setTemplatePage(templatePage);
	}

	@Override
	void appendSelf(Document document, Element parent) {
		Element self = document.createElement("template");
		self.setAttribute(
			"file",
			TemplateManager.getTemplatePath(templatePage)
				.toAbsolutePath()
				.toString());
		self.setAttribute("page", String.valueOf(templatePage.getPageIndex()));
		parent.appendChild(self);
	}
}
