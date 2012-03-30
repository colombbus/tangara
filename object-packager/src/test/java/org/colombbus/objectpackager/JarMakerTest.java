package org.colombbus.objectpackager;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;

import org.apache.commons.io.FileUtils;
import org.junit.*;

@SuppressWarnings("nls")
public class JarMakerTest {

	private static final File TEST_DATA_BASE_DIR = new File("src/test/data/org.colombbus.objectpackager.JarMaker");

	private JarMaker jarMaker;
	private File targetJarFile;

	@Before
	public void setUp() throws Exception {
		jarMaker = new JarMaker();
		targetJarFile = File.createTempFile("jarmaker-test-", ".jar");
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteQuietly(targetJarFile);
	}

	@Ignore
	@Test
	public void testSetBaseDir() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testAddInputFile() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testWriteTo() {
		fail("Not yet implemented");
	}

	@Test
	public void testComplete_1_6_6_data() throws IOException {
		File sourceDir = new File(TEST_DATA_BASE_DIR, "test1");

		jarMaker.setSourceDir(sourceDir);
		jarMaker.addInputFile(new File(TEST_DATA_BASE_DIR, "test1/a/b/c/sample.txt"));
		jarMaker.addInputFile(new File(TEST_DATA_BASE_DIR, "test1/a/b/d/hello.txt"));
		jarMaker.addInputFile(new File(TEST_DATA_BASE_DIR, "test1/a/b/coucou.txt"));

		jarMaker.writeTo(targetJarFile);

		assertJarContent("a/", "a/b/", "a/b/c/", "a/b/c/sample.txt", "a/b/d/", "a/b/d/hello.txt", "a/b/coucou.txt");
	}

	private void assertJarContent(String... expectedEntries) throws IOException {
		List<String> actualEntries = targetJarEntryNames();
		assertEquals(expectedEntries.length, actualEntries.size());
		for( String expectedEntry : expectedEntries) {
			assertTrue("Missing entry "+expectedEntry, actualEntries.contains(expectedEntry));
		}
	}

	private List<String> targetJarEntryNames() throws IOException {
		List<String> entryNames = new ArrayList<String>();

		JarFile jarFile = null;
		try {

			jarFile = new JarFile(targetJarFile);
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry jarEntry = entries.nextElement();
				String entryName = jarEntry.getName();
				entryNames.add(entryName);
			}

		} finally {
			JarUtil.closeQuietly(jarFile);
		}

		return entryNames;
	}

}
