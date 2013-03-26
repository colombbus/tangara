package org.colombbus.helpgenerator;

import org.w3c.dom.*;
import static org.colombbus.helpgenerator.util.WikiPageHelper.*;
import static org.colombbus.helpgenerator.util.HtmlUtils.*;

public class ObjectPageDumper extends AbstractPageDumper {

	private String title;
	private String objectId;

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	@Override
	public void buildPage() {
		collectCss();

		createDomDocument();
		collectImages();
		extractTitle();
		fixPageLinkUrls();
		generatePage();
	}

	private void extractTitle() {
		NodeList h1List = getDomDocument().getElementsByTagName(H1_E);
		for( int h1Idx = 0; h1Idx < h1List.getLength(); h1Idx++) {
			Element h1 = (Element)h1List.item(h1Idx);
			if( extractTitle( h1) )
				return;
		}
		System.err.println("Title not found in page of object "+objectId); //$NON-NLS-1$
	}

	private boolean extractTitle(Element h1) {
		String textContent = h1.getTextContent();
		if( textContent !=null && textContent.length()>0) {
			title = "Objet "+textContent;
			return true;
		}
		return false;
	}

	private void generatePage() {
		String filename = objectIdToFilename(objectId);
		generateLocalPage(filename, title);
	}

}
