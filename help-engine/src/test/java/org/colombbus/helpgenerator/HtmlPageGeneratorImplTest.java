package org.colombbus.helpgenerator;

import static org.junit.Assert.*;

import org.colombbus.helpgenerator.model.*;
import org.junit.*;

@SuppressWarnings("nls")
public class HtmlPageGeneratorImplTest {
	private HtmlPageGeneratorImpl generator;
	private Toc toc;
	private Page page;
	private TocLevel1 level1;
	private TocLevel2 level2a;
	private TocLevel2 level2b;
	private TocLevel3 level3a1;
	private TocLevel3 level3a2;
	private TocLevel3 level3b1;
	private TocLevel3 level3b2;

	@Before
	public void setUp() throws Exception {
		generator = new HtmlPageGeneratorImpl();
		page = new Page("myfile.html", "Page title", "body of the page");
		initToc();
	}

	private void initToc() {
		toc = new Toc();

		level1 = new TocLevel1();
		level1.setTitle("Title I");
		level1.setHeader("header 1");
		level1.setFooter("footer 1");
		toc.addLevel1(level1);

		level2a = new TocLevel2();
		level2a.setTitle("Title level 2a");
		level1.addLevel2(level2a);

		level2b = new TocLevel2();
		level2b.setTitle("Title level 2b");
		level1.addLevel2(level2b);

		level3a1 = new TocLevel3();
		level3a1.setTitle("Title level 3a1");
		level3a1.setGraphical(true);
		level3a1.setObjectId("objet:myobject3a1");
		level2a.addLevel3(level3a1);

		level3a2 = new TocLevel3();
		level3a2.setTitle("Title level 3a2");
		level3a2.setGraphical(false);
		level3a2.setObjectId("objet:myobject3a2");
		level2a.addLevel3(level3a2);

		level3b1 = new TocLevel3();
		level3b1.setTitle("Title level 3b1");
		level3b1.setGraphical(true);
		level3b1.setObjectId("objet:myobject3b1");
		level2b.addLevel3(level3b1);

		level3b2 = new TocLevel3();
		level3b2.setTitle("Title level 3b2");
		level3b2.setGraphical(true);
		level3b2.setObjectId("objet:myobject3b2");
		level2a.addLevel3(level3b2);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetToc() {
		generator.setToc(toc);
	}

	@Test(expected = NullPointerException.class)
	public void testSetToc_nullArg() {
		generator.setToc(null);
	}

	@Test(expected = IllegalStateException.class)
	public void testGeneratePage_noToc() {
		generator.generatePage(page);
	}

	@Test(expected = NullPointerException.class)
	public void testGeneratePage_nullPage() {
		generator.setToc(toc);
		generator.generatePage(null);
	}

	@Test
	public void testGeneratePage() {
		generator.setToc(toc);
		String html = generator.generatePage(page);
		assertNotNull(html);
		System.out.println("{{{"+html+"}}}");
	}
}
