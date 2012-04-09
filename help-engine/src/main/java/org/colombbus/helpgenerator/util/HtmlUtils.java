package org.colombbus.helpgenerator.util;

import java.io.*;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.Validate;
import org.w3c.dom.*;

public class HtmlUtils {

	public static final String ANCHOR_E = "a"; //$NON-NLS-1$
	public static final String H1_E = "h1"; //$NON-NLS-1$
	public static final String H2_E = "h2"; //$NON-NLS-1$
	public static final String IMG_E = "img"; //$NON-NLS-1$
	public static final String DIV_E = "div"; //$NON-NLS-1$
	public static final String P_E = "p"; //$NON-NLS-1$

	public static final String HREF_A = "href"; //$NON-NLS-1$
	public static final String TITLE_A = "title"; //$NON-NLS-1$
	public static final String ALT_A = "alt"; //$NON-NLS-1$
	public static final String ALIGN_A = "align"; //$NON-NLS-1$
	public static final String CLASS_A = "class"; //$NON-NLS-1$
	public static final String SRC_A = "src"; //$NON-NLS-1$

	public static boolean isAnchor(Node node) {
		Validate.notNull(node);
		return ANCHOR_E.equals(node.getNodeName().toLowerCase());
	}


	public static void removeNode(Element node) {
		Node parent = node.getParentNode();
		parent.removeChild(node);
	}

	/**
	 * Convert a document to an XML text without its XML header
	 *
	 * @param document
	 *            a non <code>null</code> document
	 * @return the document in text format without the XML header
	 */
	public static String toXmlNoHeader(Document document) {
		Validate.notNull(document);
		String xmlText = toText(document);
		int headerEndPos = xmlText.indexOf("?>") + 2; //$NON-NLS-1$
		String bodyText = xmlText.substring(headerEndPos);
		return bodyText;
	}

	private static String toText(Document document) {
		try {

			StringWriter stringWriter = new StringWriter();
			writeDocument(document, stringWriter);
			return stringWriter.toString();

		} catch (TransformerException transformEx) {
			throw new RuntimeException(transformEx);
		}
	}

	private static void writeDocument(Document document, Writer writer) throws TransformerException {
		Transformer transformer = newPrettyIndentTransformer();
		StreamResult result = new StreamResult(writer);
		DOMSource source = new DOMSource(document);
		transformer.transform(source, result);
	}

	private static Transformer newPrettyIndentTransformer() throws TransformerConfigurationException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
		return transformer;
	}

	public static boolean isImgElement(Node node) {
		if (Element.class.isInstance(node) == false)
			return false;
		Element element = (Element) node;
		return element.getNodeName().toLowerCase().equals(IMG_E);
	}

	public static boolean isAnchoredImage(Node node) {
		if (isImgElement(node) == false)
			return false;
		Node parent = node.getParentNode();
		return isAnchor(parent);
	}
}
