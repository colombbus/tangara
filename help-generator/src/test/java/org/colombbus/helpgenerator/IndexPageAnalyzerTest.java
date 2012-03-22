package org.colombbus.helpgenerator;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.colombbus.helpgenerator.model.Toc;
import org.colombbus.helpgenerator.model.TocLevel1;
import org.colombbus.helpgenerator.model.TocLevel2;
import org.colombbus.helpgenerator.model.TocLevel3;
import org.junit.*;
import static org.junit.Assert.*;

@SuppressWarnings("nls")
public class IndexPageAnalyzerTest {

	private String htmlContent;
	private IndexPageAnalyzer analyzer;
	private List<String> objectIdList;
	private Toc toc;

	@Before
	public void setUp() throws Exception {
		analyzer = new IndexPageAnalyzer();
		htmlContent = resourceContent("index.html");
	}

	private static String resourceContent(String resource) throws IOException {
		URL url = IndexPageAnalyzerTest.class.getResource(resource);
		return IOUtils.toString(url);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Ignore
	@Test
	public void testSetRemoteHtml() {
		fail("Not yet implemented");
	}

	@Test
	public void testAnalyze() {
		analyzer.setRemoteHtml(htmlContent);
		analyzer.analyze();

		objectIdList = analyzer.getObjectIdList();
		assertNotNull(objectIdList);
		System.out.println("objectIdList.size()=" + objectIdList.size());
		for (String objectId : objectIdList) {
			System.out.println("\t" + objectId);
		}

		toc = analyzer.getToc();
		assertNotNull(toc);
		System.out.println("--- TOC ---");
		for (TocLevel1 level1 : toc.getLevel1List()) {
			System.out.println("\tLevel 1 " + level1.getTitle());
			System.out.println("\t[HEADER=" + level1.getHeader() + "]");

			for (TocLevel2 level2 : level1.getLevel2List()) {
				System.out.println("\t\tLevel 2 " + level2.getTitle());
				for (TocLevel3 level3 : level2.getLevel3List()) {
					System.out.println("\t\t\tLevel 3 " + level3.getTitle() + " / " + level3.getObjectId() + " / " + level3.isGraphical());
				}
			}
			System.out.println("\t[FOOTER=" + level1.getFooter() + "]");
		}
	}

	@Ignore
	@Test
	public void testGetToc() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testGetObjectIdList() {
		fail("Not yet implemented");
	}
}