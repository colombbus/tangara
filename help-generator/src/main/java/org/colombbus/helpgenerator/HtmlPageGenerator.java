package org.colombbus.helpgenerator;

import org.colombbus.helpgenerator.model.Page;
import org.colombbus.helpgenerator.model.Toc;

interface HtmlPageGenerator {

	/**
	 * Set the table of content of the document
	 *
	 * @param toc
	 *            a non <code>null</code> table of content
	 */
	void setToc(Toc toc);

	/**
	 * Generate the HTML code of a page
	 *
	 * @param page
	 *            a non <code>null</code> page instance
	 * @return the HTML code corresponding to the page
	 */
	String generatePage(Page page);

}