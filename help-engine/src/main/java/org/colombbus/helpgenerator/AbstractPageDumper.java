package org.colombbus.helpgenerator;


import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.xml.parsers.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.colombbus.helpgenerator.model.*;
import org.w3c.dom.*;
import org.xml.sax.helpers.DefaultHandler;

import static org.colombbus.helpgenerator.util.WikiPageHelper.*;
import static org.colombbus.helpgenerator.util.HtmlUtils.*;

abstract class AbstractPageDumper {
	private static final String EOL = "\n"; //$NON-NLS-1$
	private static final String PAGE_DIV_START = "<div class=\"page\">"; //$NON-NLS-1$
	private static final String PAGE_DIV_STOP = "</div>"; //$NON-NLS-1$

	private String xmlText;
	private String pageBody;
	private String remoteHtml;
	private Set<Css> cssSet = new HashSet<Css>();
	private Set<Image> imageSet = new HashSet<Image>();
	private Document domDocument;
	private Page page;

	public void setRemoteHtml(String remoteHtml) {
		this.remoteHtml = remoteHtml;
		this.pageBody = extractPageBody(remoteHtml);
		this.xmlText = XML_HEADER + PAGE_DIV_START + pageBody + PAGE_DIV_STOP;
	}

	public Set<Css> getCssSet() {
		return cssSet;
	}

	protected void collectCss() {
		String[] lines = StringUtils.split(remoteHtml, EOL);
		for (String line : lines) {
			collectCssFromHtmlLine(line.trim());
		}
	}

	private void collectCssFromHtmlLine(String line) {
		if (isCssLinkLine(line) == false)
			return;

		Matcher matcher = HREF_LINK_EXTRACTOR.matcher(line);
		if (matcher.find()) {
			String link = matcher.group(1);
			Css css = new Css(link);
			Matcher mediaMatcher = MEDIA_EXTRACTOR.matcher(line);
			if( mediaMatcher.find()) {
				String media = matcher.group(1);
				css.setMedia(media);
			}

			cssSet.add(css);
		}
	}

	private static boolean isCssLinkLine(String line) {
		return line.startsWith("<link rel=\"stylesheet\""); //$NON-NLS-1$
	}

	protected void createDomDocument() {
		domDocument = toXmlDocument(xmlText);
	}

	protected Document getDomDocument() {
		return domDocument;
	}

	protected void parseSAXBody(DefaultHandler handler) {
		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			InputStream in = IOUtils.toInputStream(xmlText);
			parser.parse(in, handler);

		} catch (Exception ex) {
			throw new RuntimeException("Fail to parse page document", ex); //$NON-NLS-1$
		}
	}

	protected void generateLocalPage(String filename, String title) {
		String body = toXmlNoHeader(domDocument);
		page = new Page(filename, title, body);
	}

	public Page getPage() {
		return page;
	}

	// <a
	// href="/wiki/lib/exe/detail.php?id=objet%3Adessin&amp;media=objet:information.png"
	// class="media" title="objet:information.png">
	// <img src="/wiki/lib/exe/fetch.php?media=objet:information.png"
	// class="medialeft" align="left" alt="" />
	// </a>
	protected void collectImages() {
		NodeList images = getDomDocument().getElementsByTagName(IMG_E);
		for (int idx = 0; idx < images.getLength(); idx++) {
			Element image = (Element) images.item(idx);
			if (isAnchoredImage(image))
				fixAnchoredImage(image);
		}
	}

	private void fixAnchoredImage(Element imgE) {
		Image image = toImage(imgE);
		imageSet.add(image);
		Element newImgE = createImageElement(image);

		Element anchorE = (Element) imgE.getParentNode();
		Node anchorParent = anchorE.getParentNode();
		anchorParent.replaceChild(newImgE, anchorE);
	}

	private static Image toImage(Element imgE) {
		String remoteUrl = imgE.getAttribute(SRC_A);
		String remoteClass = imgE.getAttribute(CLASS_A);
		String align = imgE.getAttribute(ALIGN_A);
		String alt = imgE.getAttribute(ALT_A);

		Image image = new Image(remoteUrl);
		image.setCssClass(remoteClass);
		image.setAlign(align);
		image.setAlt(alt);

		return image;
	}

	private Element createImageElement(Image image) {
		Element img = getDomDocument().createElement(IMG_E);
		img.setAttribute(CLASS_A, image.getCssClass());
		img.setAttribute(ALT_A, image.getAlt());
		img.setAttribute(SRC_A, image.getLocalUrl());
		img.setAttribute(ALIGN_A, image.getAlign());
		return img;
	}

	protected void fixPageLinkUrls() {
		NodeList aElements = domDocument.getElementsByTagName(ANCHOR_E);
		for (int idx = 0; idx < aElements.getLength(); idx++) {
			Element aElement = (Element) aElements.item(idx);
			if (isPageAnchorElement(aElement))
				fixPageLinkUrl(aElement);
		}
	}

	private static void fixPageLinkUrl(Element anchor) {
		String remoteUrl = anchor.getAttribute(HREF_A);
		String localUrl = toLocalPageUrl(remoteUrl);
		anchor.setAttribute(HREF_A, localUrl);
	}

	public Set<Image> getImageSet() {
		return imageSet;
	}

	public abstract void buildPage();

}
