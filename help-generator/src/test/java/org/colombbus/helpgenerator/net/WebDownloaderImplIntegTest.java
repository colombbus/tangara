package org.colombbus.helpgenerator.net;

import static org.junit.Assert.*;

import org.colombbus.helpgenerator.net.WebDownloader;
import org.colombbus.helpgenerator.net.WebDownloaderImpl;
import org.junit.*;

@SuppressWarnings("nls")
public class WebDownloaderImplIntegTest {

	private static final String BASE_URL = "http://tangara.colombbus.org/wiki/";
	private static final String PAGE_1_URL = "doku.php?id=objet:les_objets_de_tangara#multimedia";

	private WebDownloader downloader;

	@Before
	public void setUp() throws Exception {
		downloader = new WebDownloaderImpl();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSetBaseUrl() {
		downloader.setBaseUrl(BASE_URL);
	}

	@Test(expected = NullPointerException.class)
	public void testSetBaseUrl_null() {
		downloader.setBaseUrl(null);
	}

	@Test(expected = IllegalStateException.class)
	public void testdownload_baseUrlNotSet() {
		downloader.download(PAGE_1_URL);
	}

	@Test(expected = RuntimeException.class)
	public void testdownload_badBaseUrl() {
		downloader.setBaseUrl("http://invalid.url/blabla/");
		downloader.download(PAGE_1_URL);
	}

	@Test
	public void testdownload_badPagePath() {
		downloader.setBaseUrl(BASE_URL);
		downloader.download("bad-page.html");
	}

	@Test
	public void testdownload() {
		downloader.setBaseUrl("http://commons.apache.org");
		byte[] pageContent = downloader.download("io");
		assertNotNull(pageContent);
	}
}