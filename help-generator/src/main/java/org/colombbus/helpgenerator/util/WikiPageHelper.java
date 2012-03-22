package org.colombbus.helpgenerator.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

import javax.xml.parsers.*;

import org.apache.commons.lang3.Validate;
import org.w3c.dom.*;
import static org.colombbus.helpgenerator.util.HtmlUtils.*;

public class WikiPageHelper {
	private static final String PAGE_START = "<!-- wikipage start -->"; //$NON-NLS-1$
	private static final String PAGE_STOP = "<!-- wikipage stop -->"; //$NON-NLS-1$
	public static final String XML_HEADER = "<?xml version=\"1.0\"?>\n"; //$NON-NLS-1$
	public static final Pattern HREF_LINK_EXTRACTOR = Pattern.compile("href=\"([^\"]+)\""); //$NON-NLS-1$
	public static final Pattern MEDIA_EXTRACTOR = Pattern.compile("media=\"([^\"]+)\""); //$NON-NLS-1$

	private WikiPageHelper(){}

	public static String objectIdToFilename(String objectId) {
		//id=objet:photo
		return objectId.replace("id=", "").replace(":", "_").concat(".html"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
	}

	public static String extractPageBody(String pageContent) {
		int startPos = pageContent.indexOf(PAGE_START);
		int stopPos = pageContent.indexOf(PAGE_STOP) + PAGE_STOP.length();
		return pageContent.substring(startPos, stopPos);
	}

	public static Document toXmlDocument(String xmlCode) {
		try {

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			InputStream in = new ByteArrayInputStream(xmlCode.getBytes());
			return builder.parse(in);

		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public static boolean isObjectAnchorElement(Element element) {
		Validate.notNull(element);
		if (isAnchor(element) == false)
			return false;

		Attr href = (Attr) element.getAttributes().getNamedItem(HREF_A);
		if (href == null)
			return false;

		String url = href.getNodeValue();
		return isObjectUrl(url);
	}

	public static boolean isObjectUrl(String url) {
		return url == null ? false : url.startsWith("/wiki/doku.php?id=objet:"); //$NON-NLS-1$
	}

	public static boolean isPageAnchorElement(Element element) {
		Validate.notNull(element);
		if (isAnchor(element) == false)
			return false;

		Attr href = (Attr) element.getAttributes().getNamedItem(HREF_A);
		if (href == null)
			return false;

		String url = href.getNodeValue();
		return isPageUrl(url);
	}

	public static boolean isPageUrl(String url) {
		return url == null ? false : url.startsWith("/wiki/doku.php?id="); //$NON-NLS-1$
	}

	public static String extractPageId(String href) {
		int pageIdBegin = href.lastIndexOf("id=")+3; //$NON-NLS-1$
		int pageIdEnd = href.lastIndexOf("#"); //$NON-NLS-1$
		if( pageIdEnd == -1)
			pageIdEnd = href.length();
		return href.substring(pageIdBegin, pageIdEnd);
	}

	// "/wiki/doku.php?id=objet:pixel"
	public static String toLocalPageUrl(String url) {
		Validate.isTrue(isPageUrl(url));
		String objectName = extractPageId(url);
		return objectName.replace(":", "_").concat(".html"); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
	}
}