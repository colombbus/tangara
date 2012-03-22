package org.colombbus.helpgenerator;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import static org.colombbus.helpgenerator.util.HtmlUtils.*;
import static org.colombbus.helpgenerator.util.WikiPageHelper.*;

/**
 * SAX handler used to extract the list of the object ids
 */
class ObjectIdListSearchHandler extends DefaultHandler {

	private List<String> objectIdList = new ArrayList<String>();

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// replace "/wiki/doku.php?id=objet:cadre by
		if (ANCHOR_E.equals(qName) == false)
			return;

		String hrefUrl = attributes.getValue(HREF_A);
		if( isObjectUrl(hrefUrl)) {
			String objectId = extractPageId(hrefUrl);
			objectIdList.add(objectId);
		}
	}

	public List<String> getObjectIdList() {
		return objectIdList;
	}
}
