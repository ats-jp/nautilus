package jp.ats.nautilus.pdf;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

abstract class Node {

	abstract void draw(Report report);

	abstract void appendSelf(Document document, Element parent);
}
