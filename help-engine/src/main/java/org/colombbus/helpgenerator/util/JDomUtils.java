package org.colombbus.helpgenerator.util;

import static org.colombbus.helpgenerator.util.HtmlUtils.*;

import java.io.*;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.slf4j.*;

public class JDomUtils {
	private static final Logger LOG = LoggerFactory.getLogger(JDomUtils.class);

	private JDomUtils() {}

	public static boolean h1(Element element) {
		return H1_E.equals(element.getName());
	}

	public static boolean divLevel1(Element element) {
		if (DIV_E.equals(element.getName()) == false)
			return false;
		String classAttr = element.getAttributeValue(CLASS_A);
		return "level1".equals(classAttr); //$NON-NLS-1$
	}

	public static String elementToString(Element element) {
		try {
			XMLOutputter outputter = new XMLOutputter();
			StringWriter out = new StringWriter();
			outputter.output(element, out);
			return out.toString();
		} catch (IOException ioEx) {
			LOG.error("Strange error on memory stream", ioEx);//$NON-NLS-1$
			throw new IllegalStateException("I/O error on memory stream", ioEx);//$NON-NLS-1$
		}
	}

	public static boolean paragraph(Element element) {
		return P_E.equals(element.getName());
	}
	public static boolean h2(Element element) {
		return H2_E.equals(element.getName());
	}

}
