package org.colombbus.helpgenerator;

import static org.colombbus.helpgenerator.util.WikiPageHelper.*;
import static org.colombbus.helpgenerator.util.HtmlUtils.*;

import org.w3c.dom.*;

public class CommandPageDumper extends AbstractPageDumper {

	private String title;
	private String pageId;

	public void setPageUrl(String pageUrl) {
		this.pageId = extractPageId(pageUrl);
	}

	@Override
	public void buildPage() {
		// http://tangara.colombbus.org/wiki/doku.php?id=logiciel:commandes
		collectCss();

		createDomDocument();
		collectImages();
		extractTitle();
		fixPageLinkUrls();
		generatePage();
	}

	private void extractTitle() {
		NodeList h1List = getDomDocument().getElementsByTagName(H1_E);
		for (int h1Idx = 0; h1Idx < h1List.getLength(); h1Idx++) {
			Element h1 = (Element) h1List.item(h1Idx);
			if (extractTitle(h1))
				return;
		}
		System.err.println("Title not found for page " + pageId); //$NON-NLS-1$
	}

	private boolean extractTitle(Element h1) {
		NodeList anchorList = h1.getElementsByTagName(ANCHOR_E);
		for (int anchorIdx = 0; anchorIdx < anchorList.getLength(); anchorIdx++) {
			Element anchor = (Element) anchorList.item(anchorIdx);
			String textContent = anchor.getTextContent();
			if (textContent != null && textContent.length() > 0) {
				title = textContent;
				return true;
			}
		}
		return false;
	}

	private void generatePage() {
		String filename = objectIdToFilename(pageId);
		generateLocalPage(filename, title);
	}

}
