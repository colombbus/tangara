package org.colombbus.helpgenerator;

import java.io.*;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.colombbus.helpgenerator.model.*;
import org.jdom.*;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.slf4j.*;

import static org.colombbus.helpgenerator.util.WikiPageHelper.*;
import static org.colombbus.helpgenerator.util.HtmlUtils.*;
import static org.colombbus.helpgenerator.util.JDomUtils.*;

public class IndexPageAnalyzer {

	private static final String LEVEL1_CONF = "level1.properties"; //$NON-NLS-1$

	private static final Logger LOG = LoggerFactory.getLogger(IndexPageAnalyzer.class);

	private Properties level1Configuration = new Properties();

	private String remoteHtml;
	private Document document;
	private List<String> objectIdList = new ArrayList<String>();

	private Toc toc = new Toc();
	private TocLevel1 currentLevel1;
	private TocLevel2 currentLevel2;

	private Set<Image> imageSet = new HashSet<Image>();

	public IndexPageAnalyzer() {
		loadLevel1Configuration();
	}

	private void loadLevel1Configuration() {
		InputStream inStream = null;
		try {
			inStream = IndexPageAnalyzer.class.getResourceAsStream(LEVEL1_CONF);
			level1Configuration.load(inStream);
		} catch (Exception ex) {
			String msg = String.format("Missing resource %s", LEVEL1_CONF); //$NON-NLS-1$
			LOG.error(msg);
			throw new IllegalStateException(msg, ex);
		} finally {
			IOUtils.closeQuietly(inStream);
		}
	}

	public void setRemoteHtml(String remoteHtml) {
		this.remoteHtml = remoteHtml;
	}

	public void analyze() {
		try {

			buildDocument();
			collectObjectIdList();
			buildToc();
			collectImages();

		} catch (Exception jdomEx) {
			LOG.error("Analysis failure", jdomEx); //$NON-NLS-1$
			throw new RuntimeException(jdomEx);
		}
	}

	private void buildDocument() throws JDOMException, IOException {
		String pageBody = extractPageBody(remoteHtml);
		String xmlText = String.format("<?xml version=\"1.0\"?>\n<document>\n%s\n</document>", pageBody); //$NON-NLS-1$
		// System.out.println("[[[\n" + xmlText + "\n]]]");

		SAXBuilder builder = new SAXBuilder();
		StringReader xmlTextReader = new StringReader(xmlText);
		document = builder.build(xmlTextReader);
	}

	private void collectObjectIdList() throws JDOMException {
		List<Element> anchors = findAnchorsWithHREF();
		for (Element anchor : anchors) {
			collectObjectId(anchor);
		}
	}

	private void collectObjectId(Element anchor) {
		String hrefUrl = anchor.getAttributeValue(HREF_A);
		if (isObjectUrl(hrefUrl)) {
			String objectId = extractPageId(hrefUrl);
			objectIdList.add(objectId);
		}
	}

	@SuppressWarnings("unchecked")
	private List<Element> findAnchorsWithHREF() throws JDOMException {
		XPath query = XPath.newInstance("//a[@href]"); //$NON-NLS-1$
		return query.selectNodes(document);
	}

	@SuppressWarnings("unchecked")
	private void buildToc() throws JDOMException {
		Element h1Parent = findH1Parent();
		Iterator<Element> firstLevelElementsIter = h1Parent.getChildren().listIterator();
		parseTocElements(firstLevelElementsIter);
	}

	@SuppressWarnings("unchecked")
	private Element findH1Parent() throws JDOMException {
		XPath query = XPath.newInstance("//h1/.."); //$NON-NLS-1$
		List<Element> parents = query.selectNodes(document);
		if (parents.size() != 1) {
			LOG.error("Invalid H1 parent count: " + parents.size()); //$NON-NLS-1$
			throw new IllegalStateException("Invalid XML document"); //$NON-NLS-1$
		}
		return parents.get(0);
	}

	private void parseTocElements(Iterator<Element> elementIter) throws JDOMException {
		while (elementIter.hasNext()) {
			Element tocElement = elementIter.next();
			parseTocElement(tocElement);
		}
	}

	private void parseTocElement(Element element) throws JDOMException {
		if (h1(element)) {
			createLevel1(element);
		} else if (currentLevel1 == null) {
			// do not collect information until first H1 not found
			return;
		} else if (divLevel1(element)) {
			defineLevel1Header(element);
		} else if (paragraph(element)) {
			defineLevel1Footer(element);
		} else if (h2(element)) {
			createLevel2(element);
		} else if (divLevel2(element)) {
			createLevel3Elements(element);
		}
	}

	private void createLevel1(Element h1) {
		currentLevel1 = new TocLevel1();

		String title = h1.getChildText(ANCHOR_E);
		currentLevel1.setTitle(title);

		String header = getLevel1Header(h1);
		currentLevel1.setHeader(header);

		String footer = getLevel1Footer(h1);
		currentLevel1.setFooter(footer);

		toc.addLevel1(currentLevel1);
	}

	private String getLevel1Header(Element h1) {
		String classId = h1.getAttributeValue(CLASS_A);
		String propName = classId + ".level1.header"; //$NON-NLS-1$
		String defaultValue = ""; //$NON-NLS-1$
		return level1Configuration.getProperty(propName, defaultValue);
	}

	private String getLevel1Footer(Element h1) {
		String classId = h1.getAttributeValue(CLASS_A);
		String propName = classId + ".level1.footer"; //$NON-NLS-1$
		String defaultValue = ""; //$NON-NLS-1$
		return level1Configuration.getProperty(propName, defaultValue);
	}

	private void defineLevel1Header(Element element) {
		Element p = element.getChild(P_E);
		String header = elementToString(p);
		currentLevel1.setHeader(header);
	}

	private void defineLevel1Footer(Element element) throws JDOMException {
		// element shall be a paragraph
		Element copy = (Element) element.clone();
		fixStarImageDescendant(copy);
		String footer = elementToString(copy);
		currentLevel1.setFooter(footer);
	}

	@SuppressWarnings("unchecked")
	private static void fixStarImageDescendant(Element element) throws JDOMException {
		XPath query = XPath.newInstance("//a[@title='star.png']"); //$NON-NLS-1$
		List<Element> badStarElementList = query.selectNodes(element);
		for (Element badStarElement : badStarElementList) {
			fixStarImageElement(badStarElement);
		}
	}

	private static void fixStarImageElement(Element badStarElement) {
		Element parent = badStarElement.getParentElement();
		int starIndex = parent.indexOf(badStarElement);
		Element cleanStarElement = new Element(IMG_E);
		cleanStarElement.setAttribute(SRC_A, "star.png"); //$NON-NLS-1$
		cleanStarElement.setAttribute(CLASS_A, "media"); //$NON-NLS-1$
		cleanStarElement.setAttribute(ALT_A, ""); //$NON-NLS-1$
		parent.addContent(starIndex, cleanStarElement);
		badStarElement.detach();
	}

	private void createLevel2(Element h2) {
		String title = h2.getChildText(ANCHOR_E);
		currentLevel2 = new TocLevel2();
		currentLevel2.setTitle(title);
		currentLevel1.addLevel2(currentLevel2);
	}

	private static boolean divLevel2(Element element) {
		if (DIV_E.equals(element.getName()) == false)
			return false;
		String classAttr = element.getAttributeValue(CLASS_A);
		return "level2".equals(classAttr); //$NON-NLS-1$
	}

	private void createLevel3Elements(Element divLevel2) throws JDOMException {
		for (Element objectDiv : findObjectDivs(divLevel2)) {
			createLevel3Element(objectDiv);
		}
	}

	@SuppressWarnings("unchecked")
	private static List<Element> findObjectDivs(Element divLevel2) throws JDOMException {
		XPath query = XPath.newInstance("ul/li/div"); //$NON-NLS-1$
		return query.selectNodes(divLevel2);
	}

	@SuppressWarnings("unchecked")
	private void createLevel3Element(Element objectDiv) {
		List<Element> anchors = objectDiv.getChildren(ANCHOR_E);
		Element objectAnchor = anchors.get(0);
		String objectPageUrl = objectAnchor.getAttributeValue(HREF_A);
		String objectId = extractPageId(objectPageUrl);
		String title = objectAnchor.getText();
		boolean graphical = anchors.size() > 1;

		TocLevel3 level3 = new TocLevel3();
		level3.setTitle(title);
		level3.setObjectId(objectId);
		level3.setGraphical(graphical);
		currentLevel2.addLevel3(level3);
	}

	@SuppressWarnings("unchecked")
	private void collectImages() throws JDOMException {
		XPath query = XPath.newInstance("//img"); //$NON-NLS-1$
		Collection<Element> imgElements = query.selectNodes(document);
		for( Element imgElement : imgElements) {
			String url = imgElement.getAttributeValue(SRC_A);
			Image image = new Image( url );
			imageSet.add(image);
		}
	}

	public Toc getToc() {
		return toc;
	}

	public List<String> getObjectIdList() {
		return objectIdList;
	}

	public Collection<? extends Image> getImageSet() {
		return imageSet;
	}
}
